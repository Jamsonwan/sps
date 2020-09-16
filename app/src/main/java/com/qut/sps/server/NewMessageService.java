package com.qut.sps.server;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.hyphenate.EMContactListener;
import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMucSharedFile;
import com.hyphenate.chat.EMTextMessageBody;
import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.db.FriendMessage;
import com.qut.sps.db.GroupMessage;
import com.qut.sps.db.Groups;
import com.qut.sps.db.MyFriends;
import com.qut.sps.db.NewInfoMessage;
import com.qut.sps.db.NotifyMessage;
import com.qut.sps.util.Constant;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.util.MyApplication;
import com.qut.sps.view.MyFragment;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NewMessageService extends Service {
    public NewMessageService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        EMClient.getInstance().contactManager().setContactListener(new MyListener());
        EMClient.getInstance().chatManager().addMessageListener(new MyListener());
        EMClient.getInstance().groupManager().addGroupChangeListener(new MyListener());
        super.onCreate();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        EMClient.getInstance().logout(true);
    }

    /**
     * 添加新的提醒消息
     * @param lastEMMessage
     */
    private void addNewNotifyMessage(EMMessage lastEMMessage) {
        String id;
        String iconUrl;
        String name;
        Map<String,String> map;
        String EMId;
        String type;
        if (lastEMMessage.getChatType().name().equals(EMMessage.ChatType.GroupChat.name())){
            map = getGroupInfo(lastEMMessage.getTo());
            EMId = lastEMMessage.getTo();
            id = map.get("groupId");
            iconUrl = map.get("iconUrl");
            name = map.get("name");
            type = MyFragment.GROUP_LIST;
        }else {
            map = getFriendInfo(lastEMMessage.getFrom());
            id = map.get("friendId");
            iconUrl = map.get("iconUrl");
            name = map.get("name");
            EMId = lastEMMessage.getFrom();
            type = MyFragment.FRIEND_LIST;
        }
        NotifyMessage notifyMessage = new NotifyMessage();
        notifyMessage.setContent(((EMTextMessageBody)lastEMMessage.getBody()).getMessage());
        notifyMessage.setIsRead(false);
        notifyMessage.setIconUrl(iconUrl);
        notifyMessage.setFriendOrGroupId(id);
        notifyMessage.setEMId(EMId);
        notifyMessage.setName(name);
        notifyMessage.setType(type);
        notifyMessage.setTime(lastEMMessage.getMsgTime());
        notifyMessage.setUserId(MainActivity.userId);
        notifyMessage.save();
    }

    /**
     * 保存新的消息
     * @param EMId
     * @param emMessage
     */
    private void saveNewMessage(String EMId,EMMessage emMessage) {
        String lastContent;
        if (emMessage.getChatType().name().equals(EMMessage.ChatType.GroupChat.name())){
            saveGroupsMessage(emMessage.getFrom(),emMessage);
            lastContent = ((EMTextMessageBody)emMessage.getBody()).getMessage();
        }else {
            saveFriendMessage(emMessage.getFrom(),((EMTextMessageBody)emMessage.getBody()).getMessage());
            lastContent = ((EMTextMessageBody)emMessage.getBody()).getMessage();
        }

        NotifyMessage notifyMessage = new NotifyMessage();
        notifyMessage.setTime(emMessage.getMsgTime());
        notifyMessage.setContent(lastContent);
        notifyMessage.setIsRead(false);
        notifyMessage.updateAll("EMId=?",EMId);
    }

    /**
     * 保存新的好友消息
     * @param friendAccount
     * @param content
     */
    private void saveFriendMessage(String friendAccount, String content) {
        Map<String,String> map = getFriendInfo(friendAccount);
        String friendId= map.get("friendId");
        String iconUrl = map.get("iconUrl");

        FriendMessage friendMessage = new FriendMessage();
        friendMessage.setContent(content);
        friendMessage.setUserId(Integer.parseInt(MainActivity.userId));
        friendMessage.setIconUrl(iconUrl);
        friendMessage.setType(FriendMessage.RECEIVEMESSAGE);
        friendMessage.setFriendId(Integer.parseInt(friendId));
        friendMessage.save();
    }

    /**
     * 得到好友的基本信息
     * @param friendAccount
     * @return
     */
    private Map<String,String> getFriendInfo(String friendAccount) {
        Map<String,String> map = new HashMap<>();
        List<MyFriends> myFriendsList = DataSupport.where("account = ?",friendAccount).find(MyFriends.class);
        for(MyFriends myFriends:myFriendsList){
            map.put("friendId", String.valueOf(myFriends.getFriendId()));
            map.put("iconUrl",myFriends.getIconUrl());
            if (!myFriends.getNote().equals("null")){
                map.put("name",myFriends.getNote());
            }else if (!myFriends.getNickName().equals("null")){
                map.put("name",myFriends.getNickName());
            }else {
                map.put("name",myFriends.getAccount());
            }
        }
        return map;
    }

    /**
     * 保存群聊信息
     * @param userName
     * @param emMessage
     */
    private void saveGroupsMessage(String userName,EMMessage emMessage) {
        final String groupId = getGroupInfo(emMessage.getTo()).get("groupId");
        final String content = ((EMTextMessageBody)emMessage.getBody()).getMessage();
        String url = HttpUtil.SPS_URL+"GetMemberIconAndNoteServlet";

        if (groupId == null) return;
        RequestBody requestBody = new FormBody.Builder()
                .add("account",userName)
                .add("groupId",groupId)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseDate = response.body().string();
                String memberIconUrl = null;
                String memberNote = null;
                if (!TextUtils.isEmpty(responseDate)) {
                    try {
                        JSONObject object = new JSONObject(responseDate);
                        memberIconUrl = "usersIcon/"+object.getString("iconUrl");
                        memberNote = object.getString("note");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                GroupMessage groupMessage = new GroupMessage();
                groupMessage.setIconUrl(memberIconUrl);
                groupMessage.setNote(memberNote);
                groupMessage.setType(FriendMessage.RECEIVEMESSAGE);
                groupMessage.setGroupId(Integer.parseInt(groupId));
                groupMessage.setContent(content);
                groupMessage.setUserId(Integer.parseInt(MyFragment.userId));
                groupMessage.save();
            }
        });
    }

    /**
     * 得到群的基本信息
     * @param EMGroupId
     * @return
     */
    private Map<String,String> getGroupInfo(String EMGroupId) {
        Map<String,String> map = new HashMap<>();
        List<Groups> groupsList = DataSupport.where("EMGroupId=?",EMGroupId).find(Groups.class);
        for (Groups groups:groupsList){
            map.put("groupId", String.valueOf(groups.getGroupId()));
            map.put("name",groups.getName());
            map.put("iconUrl",groups.getIconUrl());
        }
        return map;
    }

    /**
     * 用于判断是不是新的好友发送消息
     * @param EMId
     * @return
     */
    private boolean isNewUnreadMessage(String EMId) {
        List<NotifyMessage> notifyMessageList = DataSupport.where("EMId=?",EMId).find(NotifyMessage.class);
        return notifyMessageList.size() > 0;
    }



    public class MyListener implements EMContactListener,EMMessageListener,EMGroupChangeListener {

        private Intent intent = new Intent(MyApplication.getContext(),MainActivity.class);
        private PendingIntent pendingIntent = PendingIntent.getActivity(MyApplication.getContext(),0,intent,0);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(MyApplication.getContext())
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setContentTitle("舞吧")
                .setContentText("您有一条新消息")
                .setSmallIcon(R.drawable.logo_white)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.logo_white))
                .setContentIntent(pendingIntent)
                .setLights(Color.GREEN,1000,1000)
                .setAutoCancel(true)
                .build();

        /**
         * 联系人增加
         * @param s 好友账号
         */
        @Override
        public void onContactAdded(String s) {
            Intent intent  = new Intent(Constant.UPDATE_FRIEND);
            intent.putExtra(Constant.UPDATE_FRIEND_TYPE,Constant.NEW_FRIEND);
            intent.putExtra(Constant.FRIEND_ACCOUNT,s);
            sendBroadcast(intent);
        }

        /**
         * 被好友删除时
         * @param s 被删除好友账户
         */
        @Override
        public void onContactDeleted(String s) {
            Intent intent = new Intent(Constant.UPDATE_FRIEND);
            intent.putExtra(Constant.UPDATE_FRIEND_TYPE,Constant.DELETE_FRIEND);
            intent.putExtra(Constant.FRIEND_ACCOUNT,s);
            sendBroadcast(intent);

            Vibrator vibrator =( Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(new long[] { 0,200,200,200},-1);
            Intent intent1 = new Intent(Constant.NEW_NOTICE);
            sendBroadcast(intent1);
            saveContactInfo(s,s+"删除你为好友", NewInfoMessage.NO_NEED_CHOOSE);

            DataSupport.deleteAll(NotifyMessage.class,"EMId = ?",s);
        }

        /**
         * 收到申请加为好友邀请
         * @param s 对方账号
         * @param s1 申请原因
         */
        @Override
        public void onContactInvited(final String s, final String s1) {
            Vibrator vibrator =( Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(new long[] { 0,200,200,200},-1);
            Intent intent = new Intent(Constant.NEW_NOTICE);
            sendBroadcast(intent);
            saveContactInfo(s,s1,NewInfoMessage.NEED_CHOOSE);
        }

        /**
         * 好友邀请被通过
         * @param s 对方账号
         */
        @Override
        public void onFriendRequestAccepted(String s) {
        }

        /**
         * 好友邀请被拒绝
         * @param s 对方账号
         */
        @Override
        public void onFriendRequestDeclined(String s) {
            Vibrator vibrator =( Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(new long[] { 0,200,200,200},-1);
            Intent intent = new Intent(Constant.NEW_NOTICE);
            sendBroadcast(intent);
            saveContactInfo(s,s+"拒绝了你的请求",NewInfoMessage.NO_NEED_CHOOSE);
        }

        /**
         * 收到好友消息
         * @param list
         */
        @Override
        public void onMessageReceived(List<EMMessage> list) {
            int i = 0;
            boolean flag = false;
            for (final EMMessage emMessage:list){
                final String userName;

                if (!emMessage.getChatType().name().equals("Chat")){
                    userName = emMessage.getTo();
                    flag = true;
                }else {
                    userName = emMessage.getFrom();
                }
                final String messageFrom = emMessage.getFrom();
                final String content = ((EMTextMessageBody)emMessage.getBody()).getMessage();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                String currentAccount = preferences.getString(Constant.CURRENT_CHAT_ID,null);

                if (currentAccount != null && currentAccount.equals(userName)){
                    Intent intent = new Intent(Constant.NEW_MESSAGE);
                    intent.putExtra(Constant.CHAT_TO_NAME,userName);
                    intent.putExtra(Constant.MESSAGE_CONTENT,content);
                    intent.putExtra(Constant.MESSAGE_FROM,messageFrom);
                    sendBroadcast(intent);
                    if (!isNewUnreadMessage((userName))){
                        addNewNotifyMessage(emMessage);
                    }
                }else {
                    if (i == 0){
                        notificationManager.notify(1,notification);
                    }
                    if (!isNewUnreadMessage(userName)){
                        if (flag){
                            saveGroupsMessage(messageFrom,emMessage);
                        }else {
                            saveFriendMessage(userName,content);
                        }
                        addNewNotifyMessage(emMessage);
                    }else {
                        saveNewMessage(userName,emMessage);
                    }
                    Intent intent = new Intent(Constant.NEW_MESSAGE);
                    sendBroadcast(intent);
                    i++;
                }
            }
        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> list) {

        }

        @Override
        public void onMessageRead(List<EMMessage> list) {

        }

        @Override
        public void onMessageDelivered(List<EMMessage> list) {

        }

        @Override
        public void onMessageChanged(EMMessage emMessage, Object o) {

        }


        /**
         * 收到加入群邀请
         * @param s 环信群id
         * @param s1 群名称
         * @param s2 邀请者
         * @param s3 邀请原因
         */
        @Override
        public void onInvitationReceived(String s, String s1, String s2, String s3) {
            Vibrator vibrator =( Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(new long[] { 0,200,200,200},-1);
            Intent intent = new Intent(Constant.NEW_NOTICE);
            sendBroadcast(intent);
            if (s3 == null || "null".equals(s3)){
                s3 = " 邀请你加入"+s1;
            }
            saveGroupNewInfo(s,s2,s3,NewInfoMessage.NEED_CHOOSE);
        }

        /**
         *收到请求加入群
         * @param s 群id
         * @param s1 群名称
         * @param s2 申请人
         * @param s3 原因
         */
        @Override
        public void onRequestToJoinReceived(String s, String s1, String s2, String s3) {
            Vibrator vibrator =( Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(new long[] { 0,200,200,200},-1);
            Intent intent = new Intent(Constant.NEW_NOTICE);
            sendBroadcast(intent);
            saveGroupNewInfo(s,s2,s3,NewInfoMessage.NEED_CHOOSE);
        }

        /**
         * 收到加入群被同意
         * @param s 环信群id
         * @param s1 群名称
         * @param s2 接收者
         */
        @Override
        public void onRequestToJoinAccepted(String s, String s1, String s2) {
            Intent intent = new Intent(Constant.UPDATE_GROUP);
            intent.putExtra(Constant.UPDATE_GROUP_TYPE,Constant.NEW_GROUP);
            intent.putExtra(Constant.EM_GROUP_ID,s);
            sendBroadcast(intent);
        }

        /**
         * 加入群被拒绝
         * @param s 环信群Id
         * @param s1 群名称
         * @param s2 拒绝者
         * @param s3 原因
         */
        @Override
        public void onRequestToJoinDeclined(String s, String s1, String s2, String s3) {
            Vibrator vibrator =( Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(new long[] { 0,200,200,200},-1);
            Intent intent = new Intent(Constant.NEW_NOTICE);
            sendBroadcast(intent);
            if (null == s3 || "".equals(s3)){
                s3 =" 拒绝你加入"+"该群";
            }
            s2=s2+"群主";
            saveGroupNewInfo(s,s2,s3,NewInfoMessage.NO_NEED_CHOOSE);
        }

        /**
         * 邀请的好友同意加入
         * @param s 群id
         * @param s1 邀请者
         * @param s2 原因
         */
        @Override
        public void onInvitationAccepted(String s, String s1, String s2) {

        }

        /**
         * 邀请的好友拒绝加入
         * @param s 群id
         * @param s1 邀请者
         * @param s2 原因
         */
        @Override
        public void onInvitationDeclined(String s, String s1, String s2) {
            Vibrator vibrator =( Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(new long[] { 0,200,200,200},-1);
            Intent  intent = new Intent(Constant.NEW_NOTICE);
            sendBroadcast(intent);
            if (s2 == null || "null".equals(s2)){
                s2 = "的邀请被拒绝其他管理员或者群主拒绝。";
            }
            saveGroupNewInfo(s,s1,s2,NewInfoMessage.NO_NEED_CHOOSE);
        }

        /**
         * 被移除某个群
         * @param s 群id
         * @param s1 群名称
         */
        @Override
        public void onUserRemoved(String s, String s1) {
            Intent intent = new Intent(Constant.UPDATE_GROUP);
            intent.putExtra(Constant.UPDATE_GROUP_TYPE,Constant.DELETE_GROUP);
            intent.putExtra(Constant.EM_GROUP_ID,s);
            sendBroadcast(intent);

            Vibrator vibrator =( Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(new long[] { 0,200,200,200},-1);
            Intent intent1 = new Intent(Constant.NEW_NOTICE);
            sendBroadcast(intent1);
            saveGroupNewInfo(s,s1,"你已被移除群。"+s1,NewInfoMessage.NO_NEED_CHOOSE);

            DataSupport.deleteAll(NotifyMessage.class,"EMId = ?",s);
        }

        /**
         * 群被解散
         * @param s 群id；
         * @param s1 群名称
         */
        @Override
        public void onGroupDestroyed(String s, String s1) {
            Intent intent = new Intent(Constant.UPDATE_GROUP);
            intent.putExtra(Constant.UPDATE_GROUP_TYPE,Constant.DELETE_GROUP);
            intent.putExtra(Constant.EM_GROUP_ID,s);
            sendBroadcast(intent);

            Vibrator vibrator =( Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(new long[] { 0,200,200,200},-1);
            Intent intent1 = new Intent(Constant.NEW_NOTICE);
            sendBroadcast(intent1);
            saveGroupNewInfo(s,s1,s1+"已被群主解散。",NewInfoMessage.NO_NEED_CHOOSE);
            DataSupport.deleteAll(NotifyMessage.class,"EMId = ?",s);
        }

        /**
         * 接受群邀请自动加入
         * @param s 群id
         * @param s1 邀请者
         * @param s2 邀请原因
         */
        @Override
        public void onAutoAcceptInvitationFromGroup(String s, String s1, String s2) {

        }

        @Override
        public void onMuteListAdded(String s, List<String> list, long l) {

        }

        @Override
        public void onMuteListRemoved(String s, List<String> list) {

        }

        @Override
        public void onAdminAdded(String s, String s1) {

        }

        @Override
        public void onAdminRemoved(String s, String s1) {

        }

        @Override
        public void onOwnerChanged(String s, String s1, String s2) {

        }

        /**
         * 新成员加入群
         * @param s 群id
         * @param s1 成员id
         */
        @Override
        public void onMemberJoined(String s, String s1) {
        }

        /**
         * 成员退出群
         * @param s 群id
         * @param s1 群成员
         */
        @Override
        public void onMemberExited(String s, String s1) {
        }

        @Override
        public void onAnnouncementChanged(String s, String s1) {

        }

        @Override
        public void onSharedFileAdded(String s, EMMucSharedFile emMucSharedFile) {

        }

        @Override
        public void onSharedFileDeleted(String s, String s1) {

        }

        /**
         * 保存新消息
         * @param IconUrl
         * @param infoFrom
         * @param otherInfo
         * @param isNeedChoose
         * @param EMGroupId
         * @param type
         */
        private void saveNewInfoMessage(String IconUrl,String infoFrom,String otherInfo,String isNeedChoose
                ,String EMGroupId,String type){
            NewInfoMessage newInfoMessage = new NewInfoMessage();
            if (IconUrl != null){
                newInfoMessage.setIconUrl(IconUrl);
            }
            newInfoMessage.setIsNeedChoose(isNeedChoose);
            newInfoMessage.setTime(System.currentTimeMillis());
            if (EMGroupId != null){
                newInfoMessage.setEMGroupId(EMGroupId);
            }
            newInfoMessage.setUserId(MainActivity.userId);
            newInfoMessage.setOtherInfo(otherInfo);
            newInfoMessage.setInfoFrom(infoFrom);
            newInfoMessage.setType(type);
            newInfoMessage.save();
        }

        /**
         * 保存联系人新信息
         * @param account
         * @param reason
         */
        private void saveContactInfo(final String account, String reason, final String isNeedChoose){
            String url = HttpUtil.SPS_URL + "GetUserIconServlet";

            if (reason == null){
                reason = account +"拒绝了你的请求！";
            }
            final String otherInfo = reason;
            RequestBody requestBody = new FormBody.Builder()
                    .add("account",account)
                    .build();
            HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    Log.d("ERROR","获取好友头像失败！");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseData = response.body().string();
                    String iconUrl = null;
                    if (!TextUtils.isEmpty(responseData)){
                        try {
                            JSONObject object =new JSONObject(responseData);
                            iconUrl = object.getString("iconUrl");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        saveNewInfoMessage(iconUrl,account,otherInfo,isNeedChoose,null,MyFragment.FRIEND_LIST);
                    }
                }
            });
        }

        private void saveGroupNewInfo(final String EMGroupId, final String infoFrom, final String reason, final String isNeedChoose){
            String url = HttpUtil.SPS_URL + "GetGroupIconServlet";
            RequestBody requestBody = new FormBody.Builder()
                    .add("EMGroupId",EMGroupId)
                    .build();
            HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseData = response.body().string();
                    if (!TextUtils.isEmpty(responseData)){
                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            String iconUrl = "groupsIcon/"+jsonObject.getString("iconUrl");
                            saveNewInfoMessage(iconUrl,infoFrom,infoFrom+" "+reason,isNeedChoose,EMGroupId,MyFragment.GROUP_LIST);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        Log.d("ERROR","获取群名称和图像失败！");
                    }
                }
            });
        }
    }
}
