package com.qut.sps.aty;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.adapter.ChatRoomAdapter;
import com.qut.sps.db.FriendMessage;
import com.qut.sps.db.GroupMessage;
import com.qut.sps.db.Groups;
import com.qut.sps.db.UsersInfo;
import com.qut.sps.util.BaseActivity;
import com.qut.sps.util.Constant;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.util.MyApplication;
import com.qut.sps.view.MyFragment;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by lenovo on 2017/8/12.
 */

public class GroupChatRoomActivity extends BaseActivity{

    private NewGroupMessageReceiver receiver;

    private String groupId;
    private String userIcon;
    private ImageView titleRightView;

    private String chatToName;

    private Button back;

    private TextView titleTextView;
    private EditText messageContent;
    private Button sendMessage;

    private RecyclerView recyclerView;

    private ChatRoomAdapter adapter;
    private List<Map<String,String>> messageList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        groupId = getIntent().getStringExtra("id");

        initView();
        initGroupInfo();

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
        editor.putString(Constant.CURRENT_CHAT_ID,chatToName);
        editor.apply();

        userIcon = getUsersIcon(MainActivity.userAccount);

        loadGroupMessage();
        initEvent();

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancelAll();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.NEW_MESSAGE);
        receiver = new NewGroupMessageReceiver();
        registerReceiver(receiver,intentFilter);
    }

    /**
     * 得到用户的头像路径
     * @param userAccount
     * @return
     */
    private String getUsersIcon(String usersAccount){
        List<UsersInfo> usersInfoList = DataSupport.where("account=?",usersAccount).find(UsersInfo.class);
        for (UsersInfo usersInfo:usersInfoList){
            String iconUrl = usersInfo.getIconUrl();
            int index = iconUrl.lastIndexOf("/");
            if(index >= 0){
                iconUrl = iconUrl.substring(index);
                return "usersIcon"+iconUrl;
            }else {
                return "usersIcon/"+iconUrl;
            }

        }
        return null;
    }

    /**
     * 初始化群聊的基本信息
     */
    private void initGroupInfo() {
        List<Groups> groupsList = DataSupport.where("groupId=? and userId=?",groupId,MyFragment.userId)
                .find(Groups.class);
        for (Groups group:groupsList){
            titleTextView.setText(group.getName());
            chatToName = group.getEMGroupId();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
        editor.putString(Constant.CURRENT_CHAT_ID,null);
        editor.apply();
        if (receiver != null){
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGroupMessage();
    }

    /**
     * 从本地数据库中加载群聊天记录
     */
    private void loadGroupMessage() {
        List<GroupMessage> groupMessageList = DataSupport.where("groupId=? and userId =?",groupId,MyFragment.userId)
                .find(GroupMessage.class);
        Map<String,String> map;
        messageList.clear();
        for (GroupMessage groupMessage:groupMessageList){
            map = new HashMap<>();
            map.put("iconUrl",groupMessage.getIconUrl());
            map.put("type", String.valueOf(groupMessage.getType()));
            map.put("content",groupMessage.getContent());
            map.put("note",groupMessage.getNote());
            map.put("messageType", MyFragment.GROUP_LIST);
            messageList.add(map);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 启动该活动
     * @param context
     * @param id 群id
     * @param flag 启动类型
     */
    public static void starGroupChatRoomActivity(Context context,String id,int flag){
        Intent intent = new Intent(context,GroupChatRoomActivity.class);
        intent.putExtra("id",id);
        intent.addFlags(flag);
        context.startActivity(intent);
    }

    /**
     * 初始化各个控件的点击事件
     */
    private void initEvent() {
        messageContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0){
                    sendMessage.setBackgroundResource(R.color.colorSoftBlue);
                    sendMessage.setClickable(true);
                }else {
                    sendMessage.setBackgroundResource(R.drawable.shape);
                    sendMessage.setClickable(false);
                }
            }
        });
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sendMessage.isClickable()){
                    sendMessageAction(groupId);
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        titleRightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupChatRoomActivity.this,GroupInfoActivity.class);
                intent.putExtra("groupId",groupId);
                startActivityForResult(intent,GroupInfoActivity.EXIT_DISMISS_GROUP);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case GroupInfoActivity.EXIT_DISMISS_GROUP:
                if (resultCode == RESULT_OK){
                    finish();
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 发送消息，并保存在本地数据库
     * @param groupId
     */
    private void sendMessageAction(String groupId) {
        String content = messageContent.getText().toString();
        Map<String,String> map = new HashMap<>();
        map.put("content",content);
        map.put("iconUrl",userIcon);
        map.put("type", String.valueOf(FriendMessage.SENDMESSAGE));
        map.put("messageType",MyFragment.GROUP_LIST);
        String userNote = "我";
        map.put("note", userNote);
        messageList.add(map);
        adapter.notifyItemInserted(messageList.size()-1);
        recyclerView.scrollToPosition(messageList.size()-1);
        messageContent.setText("");

        EMMessage emMessage = EMMessage.createTxtSendMessage(content,chatToName);
        emMessage.setChatType(EMMessage.ChatType.GroupChat);
        EMClient.getInstance().chatManager().sendMessage(emMessage);

        saveGroupMessage(groupId,content,userIcon, userNote,FriendMessage.SENDMESSAGE);
    }

    /**
     *
     * 保存消息到本地数据库
     * @param groupId
     * @param content
     * @param icon
     * @param note
     * @param type
     */
    private void saveGroupMessage(String groupId, String content, String icon,String note, int type) {
        GroupMessage groupMessage = new GroupMessage();
        groupMessage.setIconUrl(icon);
        groupMessage.setUserId(Integer.parseInt(MyFragment.userId));
        groupMessage.setContent(content);
        groupMessage.setGroupId(Integer.parseInt(groupId));
        groupMessage.setType(type);
        groupMessage.setNote(note);
        groupMessage.save();
    }

    /**
     * 初始化界面的控件
     */
    private void initView() {
        back = (Button) findViewById(R.id.title_back);
        titleTextView = (TextView) findViewById(R.id.title_name);
        titleRightView = (ImageView) findViewById(R.id.title_right_img);
        messageContent = (EditText) findViewById(R.id.edit_message);
        sendMessage = (Button) findViewById(R.id.btn_send_message);

        recyclerView = (RecyclerView) findViewById(R.id.chat_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ChatRoomAdapter(messageList);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
    }

    /**
     * 显示接收的信息，并保存到本地数据库
     * @param content
     * @param userName
     */
    private void showReceivedMessage(final String content,String userName) {
        String url = HttpUtil.SPS_URL+"GetMemberIconAndNoteServlet";
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
                if(!TextUtils.isEmpty(responseDate)){
                    try {
                        JSONObject object = new JSONObject(responseDate);
                        memberIconUrl = object.getString("iconUrl");
                        memberNote = object.getString("note");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                final String iconUrl = "usersIcon/"+memberIconUrl;
                final String note = memberNote;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Map<String,String> map = new HashMap<>();
                        map.put("content",content);
                        map.put("iconUrl",iconUrl);
                        map.put("note",note);
                        map.put("type", String.valueOf(FriendMessage.RECEIVEMESSAGE));
                        map.put("messageType",MyFragment.GROUP_LIST);
                        messageList.add(map);
                        adapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(messageList.size()-1);
                        saveGroupMessage(groupId,content,iconUrl,note,FriendMessage.RECEIVEMESSAGE);
                    }
                });
            }
        });

    }

    class NewGroupMessageReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String chatTo=intent.getStringExtra(Constant.CHAT_TO_NAME);
            if (chatToName.equals(chatTo)){
                String content= intent.getStringExtra(Constant.MESSAGE_CONTENT);
                String userName = intent.getStringExtra(Constant.MESSAGE_FROM);
                showReceivedMessage(content,userName);
            }
        }
    }
}
