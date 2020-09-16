package com.qut.sps.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.adapter.CollectVideoAdapter;
import com.qut.sps.adapter.FriendsAndTeamAdapter;
import com.qut.sps.aty.ChatRoomActivity;
import com.qut.sps.aty.GroupChatRoomActivity;
import com.qut.sps.db.Groups;
import com.qut.sps.db.MyFriends;
import com.qut.sps.util.Constant;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.util.MyApplication;

import org.json.JSONArray;
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
 * Created by lenovo on 2017/8/4.
 */

public class MyFragment extends Fragment {

    public static String FRIEND_LIST = "1";
    public static String GROUP_LIST = "2";

    private SwipeRefreshLayout refreshLayout;

    private View rootView;
    private LinearLayout mMyFriendLayoutView;
    private LinearLayout mMyBuildTeamLayoutView;
    private LinearLayout mMyJoinedTeamLayoutView;
    private LinearLayout mMyCollectionLayoutView;
    private LinearLayout mMyDownloadLayoutView;

    private RecyclerView mMyFriendsRecyclerView;
    private RecyclerView mMyBuildTeamRecyclerView;
    private RecyclerView mMyJoinedTeamRecyclerView;
    private RecyclerView mMyCollectionRecyclerView;

    private FriendsAndTeamAdapter myFriendAdapter;
    private FriendsAndTeamAdapter myJoinedTeamAdapter;
    private FriendsAndTeamAdapter myBuildTeamAdapter;
    private CollectVideoAdapter adapter;

    private ImageView chooseMyFriendsView;
    private ImageView chooseMyBuildTeamView;
    private ImageView chooseMyJoinedTeamView;
    private ImageView chooseMyCollectionView;

    private boolean chooseMyFriends = false;
    private boolean chooseMyBuildTeam = false;
    private boolean chooseMyJoinedTeam = false;
    private boolean chooseMyCollection = false;
    private boolean chooseMyDownload = false;

    private List<Map<String,String>> myFriendList = new ArrayList<>();
    private List<Map<String,String>> myBuildTeamList = new ArrayList<>();
    private List<Map<String,String>> myJoinedTeamList = new ArrayList<>();
    private List<Map<String,String>> videoList = new ArrayList<>();

    private String requestAddress; //服务器请求地址

    public static String userId;

    private UpdateFriendReceiver friendReceiver;
    private UpdateGroupReceiver groupReceiver;

    private Handler mhandler = new Handler(){              //接受删除收藏消息并处理
        @Override
        public void handleMessage(Message msg) {
            if(CollectVideoAdapter.isDelete) {
                deleteCollection();
                CollectVideoAdapter.isDelete = false;
            }
            mhandler.sendEmptyMessageDelayed(0,1000);      //每隔一秒再发送一条消息 这样就能实时更新
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        userId = preferences.getString(Constant.CURRENT_USER_ID,null);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.UPDATE_FRIEND);
        friendReceiver = new UpdateFriendReceiver();
        getActivity().registerReceiver(friendReceiver,intentFilter);

        IntentFilter filter = new IntentFilter();
        intentFilter.addAction(Constant.UPDATE_GROUP);
        groupReceiver = new UpdateGroupReceiver();
        getActivity().registerReceiver(groupReceiver,filter);

        if(rootView != null){//已经存在
            initView();//初始化布局控件
            initEvents();//初始化各个控件的点击事件
            return rootView;
        }
        rootView = inflater.inflate(R.layout.my_fragment,container,false);
        initView();
        initEvents();
        mhandler.sendEmptyMessage(0);
        return rootView;
    }

    /**
     * 初始化各个控件的点击事件
     */
    private void initEvents() {
        initEventMyFriend();
        initEventMyBuildTeam();
        initEventMyJoinedTeam();
        initEventMyCollection();
        //initEventMyDownload();
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryFriendsFormServer();
                queryMyBuildTeamFromServer();
                queryMyJoinedTeamFromServer();
            }
        });
    }

    /**
     * 为我的收藏设置所有已收藏的视频
     */
    private void setVideoForCollection(){
        final RequestBody requestBody = new FormBody.Builder()
                .add("signal","collect")
                .add("userId", MainActivity.userId)
                .build();
        requestAddress = HttpUtil.SPS_URL+"CollectServlet";
        HttpUtil.sendOkHttpRequest(requestAddress,requestBody,new Callback(){
            @Override
            public void onFailure(Call call, IOException e){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),"ERROR",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response)throws IOException {
                final String responseText = response.body().string();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        videoList.clear();
                        handleMessageForCollectedVideo(responseText);
                    }
                });
            }
        });
    }

    /**
     * 处理已收藏视频的返回结果
     * @param responseData
     */
    private void handleMessageForCollectedVideo(String responseData){
        if(!TextUtils.isEmpty(responseData)){
            try{
                JSONArray jsonArray = new JSONArray(responseData);
                videoList.clear();
                Map<String,String> map;
                for(int i = 0;i<jsonArray.length();i++){
                    map = new HashMap<>();
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String date = jsonObject.getString("date");
                    String imageUrl = HttpUtil.SPS_SOURCE_URL + jsonObject.getString("imageUrl");
                    String videoName = jsonObject.getString("videoName");
                    String id = jsonObject.getString("id");
                    map.put("date", date);
                    map.put("imageUrl", imageUrl);
                    map.put("videoName", videoName);
                    map.put("id", id);
                    videoList.add(map);
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }


    /**
     * 删除收藏的视频
     */
    private void deleteCollection(){
        String videoId = CollectVideoAdapter.deleteId;
        RequestBody requestBody = new okhttp3.FormBody.Builder()
                .add("signal","deletecollection")
                .add("userId", MainActivity.userId)
                .add("videoId", videoId)
                .build();
        requestAddress = HttpUtil.SPS_URL+"CollectServlet";
        HttpUtil.sendOkHttpRequest(requestAddress,requestBody,new Callback(){
            @Override
            public void onFailure(Call call, IOException e){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyApplication.getContext(),"网络加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response)throws IOException {
                final String responseText = response.body().string();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleMessageForDeleteCollection(responseText);
                    }
                });
            }
        });
    }

    /**
     * 为删除收藏视频请求处理服务器返回结果
     * @param responseData
     */
    private void handleMessageForDeleteCollection(final String responseData){
        if(!TextUtils.isEmpty(responseData)){
            adapter.notifyDataSetChanged();
        }
    }


    /**
     * 点击我的收藏事件
     */
    private void initEventMyCollection() {
        mMyCollectionLayoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!chooseMyCollection){
                    if (userId != null){
                        setVideoForCollection();
                    }
                    mMyCollectionRecyclerView.setVisibility(View.VISIBLE);
                    chooseMyCollectionView.setImageResource(R.drawable.rise);
                }else {
                    mMyCollectionRecyclerView.setVisibility(View.GONE);
                    chooseMyCollectionView.setImageResource(R.drawable.shink);
                }
                chooseMyCollection = !chooseMyCollection;
            }
        });
    }

    /**
     * 点击我加入的团队事件
     */
    private void initEventMyJoinedTeam() {
        mMyJoinedTeamLayoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!chooseMyJoinedTeam){
                    mMyJoinedTeamRecyclerView.setVisibility(View.VISIBLE);
                    chooseMyJoinedTeamView.setImageResource(R.drawable.rise);
                }else {
                    mMyJoinedTeamRecyclerView.setVisibility(View.GONE);
                    chooseMyJoinedTeamView.setImageResource(R.drawable.shink);
                }
                chooseMyJoinedTeam = !chooseMyJoinedTeam;
            }
        });
    }

    /**
     *点击我创建的团队事件
     */
    private void initEventMyBuildTeam() {
       mMyBuildTeamLayoutView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if (!chooseMyBuildTeam){
                   mMyBuildTeamRecyclerView.setVisibility(View.VISIBLE);
                   chooseMyBuildTeamView.setImageResource(R.drawable.rise);
               }else {
                   mMyBuildTeamRecyclerView.setVisibility(View.GONE);
                   chooseMyBuildTeamView.setImageResource(R.drawable.shink);
               }
               chooseMyBuildTeam = !chooseMyBuildTeam;
           }
       });
    }

    /**
     * 点击我的好友后的事件
     */
    private void initEventMyFriend() {
        mMyFriendLayoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!chooseMyFriends){
                    mMyFriendsRecyclerView.setVisibility(View.VISIBLE);
                    chooseMyFriendsView.setImageResource(R.drawable.rise);
                }else {
                    mMyFriendsRecyclerView.setVisibility(View.GONE);
                    chooseMyFriendsView.setImageResource(R.drawable.shink);
                }
                chooseMyFriends = !chooseMyFriends;
            }
        });
    }

    /**
     * 初始化各个控件
     */
    private void initView() {

        if (userId != null){
            queryFromMyFriend();
            LoadMyBuildTeam();
            LoadMyJoinedTeam();
            setVideoForCollection();
        }

        mMyFriendLayoutView = rootView.findViewById(R.id.my_friends_choose);
        mMyFriendsRecyclerView = rootView.findViewById(R.id.my_friends_recycler_view);
        chooseMyFriendsView = rootView.findViewById(R.id.my_friends_choose_img);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MyApplication.getContext());
        mMyFriendsRecyclerView.setLayoutManager(layoutManager);
        myFriendAdapter = new FriendsAndTeamAdapter(myFriendList);
        mMyFriendsRecyclerView.setAdapter(myFriendAdapter);
        mMyFriendsRecyclerView.setNestedScrollingEnabled(false);

        myFriendAdapter.setOnItemClickListener(new FriendsAndTeamAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position, String id) {
                ChatRoomActivity.startChatRoomActivity(getContext(),id);
            }

            @Override
            public void OnItemCheckClick(View view, int position, boolean isChecked) {

            }
        });
        mMyBuildTeamLayoutView = rootView.findViewById(R.id.my_build_team_choose);
        mMyBuildTeamRecyclerView = rootView.findViewById(R.id.my_build_team_recycler_view);
        chooseMyBuildTeamView = rootView.findViewById(R.id.my_build_team_choose_img);
        LinearLayoutManager buildLayoutManager = new LinearLayoutManager(MyApplication.getContext());
        mMyBuildTeamRecyclerView.setLayoutManager(buildLayoutManager);
        myBuildTeamAdapter = new FriendsAndTeamAdapter(myBuildTeamList);
        mMyBuildTeamRecyclerView.setAdapter(myBuildTeamAdapter);
        mMyBuildTeamRecyclerView.setNestedScrollingEnabled(false);
        myBuildTeamAdapter.setOnItemClickListener(new FriendsAndTeamAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position, String id) {
                GroupChatRoomActivity.starGroupChatRoomActivity(getContext(),id, Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            @Override
            public void OnItemCheckClick(View view, int position, boolean isChecked) {

            }
        });

        mMyJoinedTeamLayoutView = rootView.findViewById(R.id.my_join_team_choose);
        mMyJoinedTeamRecyclerView = rootView.findViewById(R.id.my_join_team_recycler_view);
        chooseMyJoinedTeamView = rootView.findViewById(R.id.my_join_team_choose_img);
        LinearLayoutManager joinedLayoutManager = new LinearLayoutManager(MyApplication.getContext());
        mMyJoinedTeamRecyclerView.setLayoutManager(joinedLayoutManager);
        myJoinedTeamAdapter = new FriendsAndTeamAdapter(myJoinedTeamList);
        mMyJoinedTeamRecyclerView.setAdapter(myJoinedTeamAdapter);
        mMyJoinedTeamRecyclerView.setNestedScrollingEnabled(false);
        myJoinedTeamAdapter.setOnItemClickListener(new FriendsAndTeamAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position, String id) {
                GroupChatRoomActivity.starGroupChatRoomActivity(getContext(),id, Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            @Override
            public void OnItemCheckClick(View view, int position, boolean isChecked) {

            }
        });


        mMyCollectionLayoutView = rootView.findViewById(R.id.my_collection_choose);
        mMyCollectionRecyclerView = rootView.findViewById(R.id.my_collection_recycler_view);
        LinearLayoutManager collectionManager = new LinearLayoutManager(MyApplication.getContext());
        mMyCollectionRecyclerView.setLayoutManager(collectionManager);
        chooseMyCollectionView = rootView.findViewById(R.id.my_collection_choose_img);
        adapter = new CollectVideoAdapter(videoList);
        mMyCollectionRecyclerView.setAdapter(adapter);
        mMyCollectionRecyclerView.setNestedScrollingEnabled(false);

        refreshLayout = rootView.findViewById(R.id.refresh);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary);
        //mMyDownloadLayoutView = rootView.findViewById(R.id.my_download_choose);
        //mMyDownloadRecyclerView = rootView.findViewById(R.id.my_download_recycler_view);
        //chooseMyDownloadView = rootView.findViewById(R.id.my_download_choose_img);
    }

    /**
     * 加载加入的团队
     */
    private void LoadMyJoinedTeam() {
        if(queryMyJoinedTeamFromLocal()){
            return;
        }else{
            queryMyJoinedTeamFromServer();
        }
    }

    /**
     * 从服务器加载加入的团队
     */
    private void queryMyJoinedTeamFromServer() {
        String url = HttpUtil.SPS_URL + "QueryMyJoinedTeamServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("userId",userId)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyApplication.getContext(),"加载加入的群失败！请稍后重试！",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                if (handleMyTeamResponse(responseData,Groups.JOINED_TEAM)){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            queryMyJoinedTeamFromLocal();
                            myJoinedTeamAdapter.notifyDataSetChanged();
                        }
                    });
                }else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myJoinedTeamAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    /**
     * 从本地数据库查询加入的团队
     * @return
     */
    private boolean queryMyJoinedTeamFromLocal() {
        List<Groups> groupsList = DataSupport.where("userId=? and type = ?",userId,
                String.valueOf(Groups.JOINED_TEAM)).find(Groups.class);
        Map<String,String> map;
        if (groupsList.size() > 0){
            myJoinedTeamList.clear();
            for (Groups groups:groupsList){
                map = new HashMap<>();
                map.put("iconUrl",groups.getIconUrl());
                map.put("id", String.valueOf(groups.getGroupId()));
                map.put("name",groups.getName());
                myJoinedTeamList.add(map);
            }
            return true;
        }

        return false;
    }

    /**
     * 加载我创建的团队
     */
    private void LoadMyBuildTeam() {
        if(queryMyBuildTeamFromLocal()){
            return;
        }else{
           queryMyBuildTeamFromServer();
        }
    }

    /**
     * 从服务器查找我创建的团队
     */
    private void queryMyBuildTeamFromServer() {
        String url = HttpUtil.SPS_URL+"QueryMyBuildTeamServlet";
        final RequestBody requestBody = new FormBody.Builder()
                .add("userId",userId)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyApplication.getContext(),"加载创建群失败！",Toast.LENGTH_SHORT).show();
                    }
                });
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (refreshLayout.isRefreshing()){
                            refreshLayout.setRefreshing(false);
                        }
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                if (handleMyTeamResponse(responseData,Groups.BUILD_TEAM)){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            queryMyBuildTeamFromLocal();
                            myBuildTeamAdapter.notifyDataSetChanged();
                        }
                    });
                }else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myBuildTeamAdapter.notifyDataSetChanged();
                        }
                    });
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (refreshLayout.isRefreshing()){
                            refreshLayout.setRefreshing(false);
                        }
                    }
                });
            }
        });
    }

    /**
     * 处理服务器返回的我的团队的基本信息
     * @param responseData
     * @param type
     * @return
     */
    private boolean handleMyTeamResponse(String responseData,int type) {
        Groups groups;
        DataSupport.deleteAll(Groups.class,"type=?", String.valueOf(type));
        if (!TextUtils.isEmpty(responseData)){
            try {
                JSONArray jsonArray = new JSONArray(responseData);
                for (int i = 0 ;i < jsonArray.length();i++){
                    JSONObject object = jsonArray.getJSONObject(i);
                    groups = new Groups();
                    groups.setGroupId(Integer.parseInt(object.getString("groupId")));
                    groups.setIconUrl("groupsIcon/"+object.getString("iconUrl"));
                    groups.setName(object.getString("name"));
                    groups.setDescription(object.getString("description"));
                    groups.setType(type);
                    groups.setUserId(Integer.parseInt(userId));
                    groups.setEMGroupId(object.getString("EMGroupId"));
                    groups.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * 从本地数据库去查找我创建的团队
     * @return
     */
    private boolean queryMyBuildTeamFromLocal() {
        Map<String,String> map;
        List<Groups> groupsList = DataSupport.where("userId = ? and type = ?",userId,
                String.valueOf(Groups.BUILD_TEAM)).find(Groups.class);
        if (groupsList.size() > 0){
            myBuildTeamList.clear();
            for (Groups group : groupsList){
                map = new HashMap<>();
                map.put("iconUrl",group.getIconUrl());
                map.put("id", String.valueOf(group.getGroupId()));
                map.put("name",group.getName());
                myBuildTeamList.add(map);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onResume(){
        super.onResume();
        if (userId != null){
            queryFriendsFormServer();
            queryMyBuildTeamFromServer();
            queryMyJoinedTeamFromServer();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (friendReceiver != null){
            getActivity().unregisterReceiver(friendReceiver);
            friendReceiver = null;
        }
        if (groupReceiver != null){
            getActivity().unregisterReceiver(groupReceiver);
            groupReceiver = null;
        }
    }

    /**
     * 从本地数据库加载好友列表
     * @return
     */
    private boolean queryFromMyFriend(){
        boolean result = false;
        myFriendList.clear();
        Map<String,String > map;
        List<MyFriends> myFriendsList = DataSupport.where("userId=?",userId).find(MyFriends.class);
        if (myFriendsList.size() > 0){
            for (MyFriends myFriend:myFriendsList){
                map = new HashMap<>();
                map.put("iconUrl",myFriend.getIconUrl());
                map.put("id",String.valueOf(myFriend.getFriendId()));
                if (!myFriend.getNote().equals("null")){
                    map.put("name",myFriend.getNote());
                }else if(!myFriend.getNickName().equals("null")){
                    map.put("name",myFriend.getNickName());
                }else {
                    map.put("name",myFriend.getAccount());
                }
                myFriendList.add(map);
            }
            result = true;
        }
        return result;
    }

    /**
     * 从服务器加载好友列表
     */
    private void queryFriendsFormServer() {
        String url = HttpUtil.SPS_URL+"QueryMyFriendsServlet";
        final RequestBody requestBody = new FormBody.Builder()
                .add("userId",userId)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),"加载失败，请检查您的网络，或稍后再试！",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                if(!TextUtils.isEmpty(responseData)){
                    loadFriendList(responseData);
                }else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myFriendList.clear();
                        }
                    });
                }
            }
        });
    }

    /**
     * 用于判读其好友列表是否为空
     * @param responseData
     */
    private void loadFriendList(String responseData){
        if(handleMyFriendResponse(responseData)){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (queryFromMyFriend()){
                        myFriendAdapter.notifyDataSetChanged();
                    }
                }
            });
        }else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyApplication.getContext(),"加载失败!",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * 好友列表不为空进行解析数据并保存到本地
     * @param responseData
     * @return
     */
    private boolean handleMyFriendResponse(String responseData){
        DataSupport.deleteAll(MyFriends.class,"userId=?",MainActivity.userId);
        try {
            JSONArray jsonArray = new JSONArray(responseData);
            for (int i =0; i < jsonArray.length();i++){
                JSONObject object = jsonArray.getJSONObject(i);
                MyFriends myFriends = new MyFriends();
                myFriends.setAccount(object.getString("account"));
                myFriends.setFriendId(Integer.valueOf(object.getString("friendId")));
                myFriends.setIconUrl("usersIcon/"+object.getString("iconUrl"));
                myFriends.setNickName(object.getString("nickName"));
                myFriends.setNote(object.getString("note"));
                myFriends.setUserId(Integer.valueOf(userId));
                myFriends.save();
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    class UpdateGroupReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            queryMyJoinedTeamFromServer();
            queryMyBuildTeamFromServer();
        }
    }
    /**
     * 接受更新好友列表
     */
    class UpdateFriendReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            queryFriendsFormServer();
        }
    }
}
