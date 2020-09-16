package com.qut.sps.aty;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.qut.sps.R;
import com.qut.sps.adapter.FriendsAndTeamAdapter;
import com.qut.sps.db.Groups;
import com.qut.sps.db.MyFriends;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.view.MyFragment;

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


public class InviteFriendActivity extends AppCompatActivity {

    private Button inviteFriendButton;

    private ImageView clearSearchContent;

    private EditText editContent;

    private List<Map<String,String>> searchList = new ArrayList<>();
    private List<Map<String,String>> friendList = new ArrayList<>();
    private List<Integer> friendPositionList = new ArrayList<>();
    private List<Integer> searchPositionList = new ArrayList<>();

    private FriendsAndTeamAdapter searchAdapter;
    private FriendsAndTeamAdapter friendAdapter;

    private RelativeLayout friendListLayout;

    private String EMGroupId;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friend);

        EMGroupId = getIntent().getStringExtra("EMGroupId");
        type = getIntent().getIntExtra("type",0);

        initView();
        initEvent();
    }

    /**
     * 初始化各个点击事件
     */
    private void initEvent() {
        searchAdapter.setOnItemClickListener(new FriendsAndTeamAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position, String id) {
                boolean flag = true;
                for(int previous : searchPositionList){
                    if (previous == position){
                        flag = false;
                    }
                }
                if (flag){
                    searchPositionList.add(position);
                }else {
                    searchPositionList.remove(position);
                }
            }

            @Override
            public void OnItemCheckClick(View view, int position, boolean isChecked) {

            }
        });

        editContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() > 0){
                    friendListLayout.setVisibility(View.GONE);
                    clearSearchContent.setVisibility(View.VISIBLE);
                    searchMyFriend(editable.toString());
                    searchAdapter.notifyDataSetChanged();
                }else {
                    friendListLayout.setVisibility(View.VISIBLE);
                    clearSearchContent.setVisibility(View.GONE);
                    searchList.clear();
                    searchAdapter.notifyDataSetChanged();
                }
            }
        });

        friendAdapter.setOnItemClickListener(new FriendsAndTeamAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position, String id) {

            }

            @Override
            public void OnItemCheckClick(View view, int position, boolean isChecked) {
                boolean flag = true;
                int i = 0;
                int  m = 0;
                for(int previous:friendPositionList){
                    if (previous == position){
                        flag = false;
                        m = i;
                    }
                    i++;
                }
                if (flag){
                    friendPositionList.add(position);
                }else {
                    friendPositionList.remove(m);
                }
            }
        });

        clearSearchContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editContent.setText("");
            }
        });

        inviteFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchPositionList.size() > 0 || friendPositionList.size()> 0){
                    List<String> inviteList = getFriendAccountList();
                    sendInvite(inviteList);
                    finish();
                }else {
                    Toast.makeText(InviteFriendActivity.this,"请先选择好友！",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 发送群邀请好友
     * @param inviteList
     */
    private void sendInvite(final List<String> inviteList) {
        final String[] members = new String[inviteList.size()];
        int i = 0;
        for (String member:inviteList){
            members[i++] = member;
        }
        if (type == Groups.BUILD_TEAM){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        EMClient.getInstance().groupManager().addUsersToGroup(EMGroupId,members);
                        for (String memberAccount:inviteList){
                            JoinInGroup(memberAccount,EMGroupId);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(InviteFriendActivity.this,"邀请入群成功！",Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        EMClient.getInstance().groupManager().inviteUser(EMGroupId,members,null);
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(InviteFriendActivity.this,"你暂时没有权限！",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).start();
        }
    }

    private void JoinInGroup(String memberAccount, String emGroupId) {
        String url = HttpUtil.SPS_URL+"JoinInGroupServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("EMGroupId",emGroupId)
                .add("memberAccount",memberAccount)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.body().string().equals("OK")){
                    Log.d("ERROR","加入群服务器失败！");
                }
            }
        });
    }

    /**
     * 得到群邀请所有好友的账号
     * @return
     */
    private List<String> getFriendAccountList() {
        List<String> inviteList = new ArrayList<>();
        if (searchPositionList.size() > 0){
            for (int position:searchPositionList){
                String friendId = searchList.get(position).get("id");
                inviteList.add(getFriendAccount(friendId));
            }
        }else if (friendPositionList.size() > 0){
            for (int position:friendPositionList){
                String friendId = friendList.get(position).get("id");
                inviteList.add(getFriendAccount(friendId));
            }
        }
        return inviteList;
    }

    /**
     * 得到单个好友的账号
     * @param friendId
     * @return
     */
    private String getFriendAccount(String friendId) {
        List<MyFriends> myFriendsList = DataSupport.where("friendId = ?",friendId)
                .find(MyFriends.class);
        String account = null;
        for (MyFriends myFriend : myFriendsList){
            account = myFriend.getAccount();
        }
        return account;
    }

    /**
     * 动态搜索好友
     * @param content
     */
    private void searchMyFriend(String content) {
        searchList.clear();
        Map<String,String> map;
        String sql = "select friendId,nickName,account,note,iconUrl from MyFriends where note like '"+
                content+"%' or nickName like '"+content+"%' or account like '"+content+"%'";
        Cursor cursor = DataSupport.findBySQL(sql);
        if (cursor.moveToFirst()){
            do {
                map = new HashMap<>();
                map.put("iconUrl",cursor.getString(cursor.getColumnIndex("iconurl")));
                map.put("id", String.valueOf(cursor.getInt(cursor.getColumnIndex("friendid"))));
                String note = cursor.getString(cursor.getColumnIndex("note"));
                if (!note.equals("null")){
                    map.put("name",note);
                }else {
                    String nickName = cursor.getString(cursor.getColumnIndex("nickname"));
                    if (!nickName.equals("null")){
                        map.put("name",nickName);
                    }else {
                        map.put("name",cursor.getString(cursor.getColumnIndex("account")));
                    }
                }
                searchList.add(map);
            }while (cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * 初始化布局控件
     */
    private void initView() {
        Button backButton = (Button) findViewById(R.id.title_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ImageView titleRightView = (ImageView) findViewById(R.id.title_right_img);
        titleRightView.setVisibility(View.GONE);

        inviteFriendButton = (Button) findViewById(R.id.title_right_btn);
        inviteFriendButton.setVisibility(View.VISIBLE);
        inviteFriendButton.setText("邀请");

        TextView titleText = (TextView) findViewById(R.id.title_name);
        titleText.setText("邀请好友");

        editContent = (EditText) findViewById(R.id.edit_search_friend);
        clearSearchContent = (ImageView) findViewById(R.id.clear_search_friend);

        RecyclerView searchRecyclerView = (RecyclerView) findViewById(R.id.result_of_search_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(InviteFriendActivity.this);
        searchRecyclerView.setLayoutManager(layoutManager);
        searchAdapter = new FriendsAndTeamAdapter(searchList);
        searchRecyclerView.setAdapter(searchAdapter);
        searchAdapter.flag = true;

        RecyclerView myFriendRecyclerView = (RecyclerView) findViewById(R.id.my_friend_list_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(InviteFriendActivity.this);
        myFriendRecyclerView.setLayoutManager(linearLayoutManager);
        loadMyFriend();
        friendAdapter = new FriendsAndTeamAdapter(friendList);
        myFriendRecyclerView.setAdapter(friendAdapter);
        friendAdapter.flag = true;

        friendListLayout = (RelativeLayout) findViewById(R.id.friend_list_layout);
    }

    /**
     * 加载好友列表
     */
    private void loadMyFriend() {
        List<MyFriends> myFriendList = DataSupport.where("userId=?", MyFragment.userId)
                .find(MyFriends.class);
        Map<String,String > map;
        for (MyFriends friend:myFriendList){
            map = new HashMap<>();
            map.put("iconUrl",friend.getIconUrl());
            map.put("id", String.valueOf(friend.getFriendId()));
            if(!friend.getNote().equals("null")){
                map.put("name",friend.getNote());
            }else if (!friend.getNickName().equals("null")){
                map.put("name",friend.getNickName());
            }else {
                map.put("name",friend.getAccount());
            }
            friendList.add(map);
        }
    }

    /**
     * 启动群邀请好友
     * @param context
     * @param EMGroupId
     * @param type //创建的群还是加入的群
     */
    public static void startInviteFriendActivity(Context context,String EMGroupId,int type){
        Intent intent = new Intent(context,InviteFriendActivity.class);
        intent.putExtra("EMGroupId",EMGroupId);
        intent.putExtra("type",type);
        context.startActivity(intent);
    }
}
