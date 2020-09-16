package com.qut.sps.aty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;

import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.db.Groups;
import com.qut.sps.db.MyFriends;
import com.qut.sps.util.ActivityCollector;
import com.qut.sps.util.BaseActivity;
import com.qut.sps.util.Constant;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.util.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WelcomeActivity extends BaseActivity {

    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
              | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_welcome);

        SharedPreferences pref1 = getSharedPreferences("init",MODE_PRIVATE);
        final String isInit = pref1.getString("init",null);
        if (isInit != null){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
            userId = preferences.getString(Constant.CURRENT_USER_ID,null);
            if (userId != null){
                queryMyBuildTeamFromServer();
                queryMyJoinedTeamFromServer();
                queryFriendsFormServer();
            }
        }
        Handler handler = new Handler();
        //当计时结束,跳转至主界面
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isInit != null && userId != null) {
                    Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                    if(getIntent().getBundleExtra("launchBundle")!=null){
                        intent.putExtra("launchBundle",getIntent().getBundleExtra("launchBundle"));
                    }
                    startActivity(intent);
                    ActivityCollector.finishAll();
                }else {
                    Intent intent1 = new Intent(WelcomeActivity.this,InitActivity.class);
                    startActivity(intent1);
                    WelcomeActivity.this.finish();
                }
            }
        }, 2000);
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
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                handleMyTeamResponse(responseData,Groups.JOINED_TEAM);
            }
        });
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
              e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                handleMyTeamResponse(responseData,Groups.BUILD_TEAM);
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
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                if(!TextUtils.isEmpty(responseData)){
                   handleMyFriendResponse(responseData);
                }
            }
        });
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
}
