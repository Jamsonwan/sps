package com.qut.sps.aty;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
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
import com.qut.sps.db.MyFriends;
import com.qut.sps.db.UsersInfo;
import com.qut.sps.util.BaseActivity;
import com.qut.sps.util.Constant;
import com.qut.sps.util.MyApplication;
import com.qut.sps.view.MyFragment;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomActivity extends BaseActivity{

    private String friendIcon;
    private String friendId;
    private String friendNote="";
    private String usersIcon;

    public String chatToName;
    private NewMessageReceiver newMessageReceiver;

    private ImageView titleRightView;

    private Button back;

    private TextView titleTextView;
    private EditText messageContent;
    private Button sendMessage;

    private RecyclerView recyclerView;

    private ChatRoomAdapter adapter;
    private List<Map<String,String>> messageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        IntentFilter intentFilter =  new IntentFilter();
        intentFilter.addAction(Constant.NEW_MESSAGE);
        newMessageReceiver = new NewMessageReceiver();
        registerReceiver(newMessageReceiver,intentFilter);

        initView();
        friendId = getIntent().getStringExtra("id");
        initFriendInfo(friendId);

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
        editor.putString(Constant.CURRENT_CHAT_ID,chatToName);
        editor.apply();

        usersIcon = getUsersIcon(MainActivity.userAccount);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancelAll();

        loadMessage(friendId);
        initEvent(friendId);
    }


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
     *启动好友聊天室，需要知道好友的id
     * @param context
     * @param id
     */
    public static void startChatRoomActivity(Context context,String id){
        Intent intent = new Intent(context,ChatRoomActivity.class);
        intent.putExtra("id",id);
        context.startActivity(intent);
    }

    /**
     * 初始化点击事件
     * @param friendId
     */
    private void initEvent(final String friendId) {
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
                    sendMessageAction(friendId);
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
                Intent intent = new Intent(ChatRoomActivity.this,FriendInforActivity.class);
                intent.putExtra("friendId",friendId);
                startActivity(intent);
            }
        });
    }

    /**
     * 用于发送消息。并在聊天界面显示
     * 用的时候把friendIcon改成userIcon
     * @param friendId
     */
    private void sendMessageAction(String friendId){
        String content = messageContent.getText().toString();
        Map<String,String> map = new HashMap<>();
        map.put("content", content);
        map.put("iconUrl",usersIcon);
        map.put("type", String.valueOf(FriendMessage.SENDMESSAGE));
        messageList.add(map);
        adapter.notifyItemInserted(messageList.size()-1);
        recyclerView.scrollToPosition(messageList.size()-1);
        messageContent.setText("");
        saveFriendMessage(friendId, content,usersIcon,FriendMessage.SENDMESSAGE);

        EMMessage emMessage = EMMessage.createTxtSendMessage(content,chatToName);
        emMessage.setChatType(EMMessage.ChatType.Chat);
        EMClient.getInstance().chatManager().sendMessage(emMessage);
    }

    /**
     * 用于保存和好友的聊天记录到数据库，其中暗含存入了userId
     * @param friendId 与好友聊天的friendId
     * @param content 与好友聊天的内容
     * @param iconUrl 图片的内容/
     * @param type 是发送还是接收
     */
    private void saveFriendMessage(String friendId,String content,String iconUrl,int type) {
        FriendMessage friendMessage = new FriendMessage();
        friendMessage.setUserId(Integer.parseInt(MyFragment.userId));
        friendMessage.setFriendId(Integer.parseInt(friendId));
        friendMessage.setContent(content);
        friendMessage.setType(type);
        friendMessage.setIconUrl(iconUrl);
        friendMessage.save();
    }

    /**
     * 初始化显示界面
     */
    private void initView() {
        back = (Button) findViewById(R.id.title_back);
        titleTextView = (TextView) findViewById(R.id.title_name);

        titleRightView = (ImageView) findViewById(R.id.title_right_img);
        titleRightView.setImageResource(R.drawable.friend_info);

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
     * 初始化好友的基本信息
     * @param friendId
     */
    private void initFriendInfo(String friendId){
        String friendNickName = null;
        List<MyFriends> myFriendsList = DataSupport.where("friendId=?",friendId).find(MyFriends.class);
        for (MyFriends myFriends:myFriendsList){
            chatToName = myFriends.getAccount();
            friendIcon = myFriends.getIconUrl();
            friendNote = myFriends.getNote();
            friendNickName = myFriends.getNickName();
        }
        if (!friendNote.equals("null")){
            titleTextView.setText(friendNote);
        }else if (!friendNickName.equals("null")){
            titleTextView.setText(friendNickName);
        }else {
            titleTextView.setText(chatToName);
        }
    }

    /**
     * 从本地加载聊天记录
     * @param friendId
     */
    private void loadMessage(String friendId) {
        messageList.clear();
        Map<String,String> map;
        List<FriendMessage> friendMessageList = DataSupport.where("friendId=? and userId=?",friendId, MyFragment.userId).find(FriendMessage.class);
        for (FriendMessage friendMessage:friendMessageList){
            map = new HashMap<>();
            map.put("content",friendMessage.getContent());
            map.put("iconUrl",friendMessage.getIconUrl());
            map.put("type", String.valueOf(friendMessage.getType()));
            messageList.add(map);
        }
        if (messageList.size() > 0){
           adapter.notifyDataSetChanged();
        }
    }

    /**
     * 显示接受到的消息
     * @param content
     */
    private void showReceivedMessage(final String content){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Map<String,String> map = new HashMap<>();
                map.put("content",content);
                map.put("iconUrl",friendIcon);
                map.put("type", String.valueOf(FriendMessage.RECEIVEMESSAGE));
                messageList.add(map);
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size()-1);
                saveFriendMessage(friendId,content,friendIcon,FriendMessage.RECEIVEMESSAGE);
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        initFriendInfo(friendId);
        loadMessage(friendId);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
        editor.putString(Constant.CURRENT_CHAT_ID,null);
        editor.apply();
        if (newMessageReceiver != null){
            unregisterReceiver(newMessageReceiver);
            newMessageReceiver = null;
        }
    }

    class NewMessageReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String chatName = intent.getStringExtra(Constant.CHAT_TO_NAME);
            if (chatToName.equals(chatName)){
                String content = intent.getStringExtra(Constant.MESSAGE_CONTENT);
                showReceivedMessage(content);
            }
        }
    }
}