package com.qut.sps.aty;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.qut.sps.R;
import com.qut.sps.adapter.FriendsAndTeamAdapter;
import com.qut.sps.db.UsersInfo;
import com.qut.sps.server.JudeLeaderService;
import com.qut.sps.util.Constant;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.util.MyApplication;
import com.qut.sps.view.MyFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LetDanceActivity extends AppCompatActivity {

    public static final int START_STATE = 1;

    private Button startDance;
    private  Button titleBack;
    private TextView leaderName;
    private CircleImageView leaderIcon;
    private FriendsAndTeamAdapter adapter;

    private PopupWindow popupWindow;

    private DanceStateReceiver receiver;

    private String EMGroupId;

    private String account;
    private static String leaderAccount;
    private boolean isJoined = false;
    private  Intent bindIntent;

    private List<Map<String,String>> danceList = new ArrayList<>();

    private Button joinDanceBtn;
    private LinearLayout joinDance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_let_dance);

        EMGroupId = getIntent().getStringExtra("EMGroupId");
        String userId = getIntent().getStringExtra("userId");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        account = sharedPreferences.getString(Constant.CURRENT_ACCOUNT,null);
        getUserAccount(userId);


        bindIntent = new Intent(this,JudeLeaderService.class);
        bindIntent.putExtra("EMGroupId",EMGroupId);
        startService(bindIntent);
        initView();
        initEvent();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.qut.sps.LOCAL_BROADCAST");
        receiver = new DanceStateReceiver();
        registerReceiver(receiver,intentFilter);
    }

    private void initEvent() {
        leaderIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupWindow();
            }
        });

        joinDanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isJoined){
                    joinDance.setVisibility(View.GONE);
                    sendJoinDance(EMGroupId,account);
                    isJoined = !isJoined;
                }
            }
        });


        startDance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (account.equals(leaderAccount)){
                    sendStartDance(EMGroupId,leaderAccount);
                    JudeLeaderService.manager.cancel(JudeLeaderService.pendingIntent);
                    stopService(bindIntent);
                    if(receiver != null){
                        unregisterReceiver(receiver);
                        receiver = null;
                    }
                    DanceActivity.startDanceActivity(LetDanceActivity.this,EMGroupId,account,leaderAccount);
                    finish();
                }else{
                    AlertDialog.Builder dialog = new AlertDialog.Builder(LetDanceActivity.this);
                    dialog.setCancelable(false);
                    dialog.setTitle("提醒");
                    dialog.setMessage("请先点击 + 成为领舞者！");
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    dialog.show();
                }
            }
        });

        titleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (account.equals(leaderAccount) || isJoined){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(LetDanceActivity.this);
                    dialog.setCancelable(false);
                    dialog.setTitle("提醒");
                    dialog.setMessage("退出将取消本次舞会");
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(isJoined) {
                                sendExitDance(account);
                            }else {
                                sendCancelDance(EMGroupId);
                            }
                            JudeLeaderService.manager.cancel(JudeLeaderService.pendingIntent);
                            stopService(bindIntent);
                            finish();
                        }
                    });
                    dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    dialog.show();
                }else{
                    JudeLeaderService.manager.cancel(JudeLeaderService.pendingIntent);
                    stopService(bindIntent);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (receiver != null){
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if (account.equals(leaderAccount) || isJoined) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(LetDanceActivity.this);
                dialog.setCancelable(false);
                dialog.setTitle("提醒");
                dialog.setMessage("退出将取消本次舞会");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (isJoined) {
                            sendExitDance(account);
                        } else {
                            sendCancelDance(EMGroupId);
                        }
                        JudeLeaderService.manager.cancel(JudeLeaderService.pendingIntent);
                        stopService(bindIntent);
                        finish();
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dialog.show();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 加载没有领舞者
     */
    private void initNoLeaderEvent() {
        joinDance.setVisibility(View.GONE);
        leaderIcon.setClickable(true);
        leaderIcon.setImageResource(R.drawable.dashed);
        leaderName.setText("");
        startDance.setVisibility(View.VISIBLE);
        loadJoinedMember(EMGroupId);
        leaderAccount="";
        isJoined = false;
    }

    /**
     * 开始本次舞会
     * @param EMGroupId
     * @param leaderAccount
     */
    private void sendStartDance(String EMGroupId, String leaderAccount) {
        String url = HttpUtil.SPS_URL + "StartDanceServlet";
        final RequestBody requestBody = new FormBody.Builder()
                .add("EMGroupId",EMGroupId)
                .add("leaderAccount",leaderAccount)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyApplication.getContext(),"请检查网络后重试！",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body().string().equals("OK")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MyApplication.getContext(), "开始舞会！", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("ERROR6",6+"");
                            Toast.makeText(MyApplication.getContext(), "发生未知错误！", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 退出本次舞会
     * @param account
     */
    private void sendExitDance(String account) {
        String url = HttpUtil.SPS_URL + "ExitDanceServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("memberAccount",account)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyApplication.getContext(),"请检查网络后重试！",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.body().string().equals("OK")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MyApplication.getContext(),"退出舞会成功！",Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("ERROR5",5+"");
                            Toast.makeText(MyApplication.getContext(),"发生未知错误！",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 加载参与本次舞会的成员
     * @param EMGroupId
     */
    private void loadJoinedMember(String EMGroupId) {
        String url = HttpUtil.SPS_URL + "GetJoinMemberServlet";
        final RequestBody requestBody = new FormBody.Builder()
                .add("EMGroupId",EMGroupId)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LetDanceActivity.this,"请检查网络后重试！",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseData = response.body().string();
                if (!TextUtils.isEmpty(responseData)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONArray jsonArray = new JSONArray(responseData);
                                Map<String,String> map;
                                danceList.clear();
                                for (int i = 0;i < jsonArray.length();i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    map = new HashMap<>();
                                    map.put("name",object.getString("name"));
                                    map.put("iconUrl","usersIcon/"+object.getString("iconUrl"));
                                    map.put("id",object.getString("id"));
                                    map.put("type", MyFragment.GROUP_LIST);
                                    danceList.add(map);
                                }
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            danceList.clear();
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    /**
     * 发送加入本次舞会
     * @param EMGroupId
     * @param account
     */
    private void sendJoinDance(String EMGroupId,String account) {
        String url = HttpUtil.SPS_URL + "JoinDanceServlet";
        final RequestBody requestBody = new FormBody.Builder()
                .add("EMGroupId",EMGroupId)
                .add("memberAccount",account)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LetDanceActivity.this,"请检查网络后重试！",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final  String responseData = response.body().string();
                if (!TextUtils.isEmpty(responseData)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject object = new JSONObject(responseData);
                                Map<String,String> map = new HashMap<>();
                                map.put("name",object.getString("name"));
                                map.put("id",object.getString("id"));
                                map.put("iconUrl",object.getString("iconUrl"));
                                map.put("type",MyFragment.GROUP_LIST);
                                danceList.add(map);
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("ERROR4",4+"");
                            Toast.makeText(LetDanceActivity.this,"发生未知错误！",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


    /**
     * 一个广播监听类
     */
    class DanceStateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean haveLeader = intent.getBooleanExtra("haveLeader", false);
            int currentState = intent.getIntExtra("currentState", 0);
            leaderAccount = intent.getStringExtra("leaderAccount");
            if (haveLeader){
                if (currentState == START_STATE){
                    JudeLeaderService.manager.cancel(JudeLeaderService.pendingIntent);
                    stopService(bindIntent);
                    if (receiver != null){
                        unregisterReceiver(receiver);
                        receiver = null;
                    }
                    Toast.makeText(MyApplication.getContext(),"开始舞会！",Toast.LENGTH_SHORT).show();
                    DanceActivity.startDanceActivity(LetDanceActivity.this,EMGroupId,account,leaderAccount);
                    finish();
                }
                initHaveLeaderEvent();
            }else{
                initNoLeaderEvent();
            }
        }
    }
    /**
     * 存在领舞者并且处于准备状态
     */
    private void initHaveLeaderEvent() {

        initLeaderView(EMGroupId);
        if (account.equals(leaderAccount)){
            startDance.setVisibility(View.VISIBLE);
            joinDance.setVisibility(View.GONE);
            leaderIcon.setClickable(true);
        }else {
            startDance.setVisibility(View.GONE);
            if (isJoined){
                joinDance.setVisibility(View.GONE);
            }else{
                joinDance.setVisibility(View.VISIBLE);
            }
            leaderIcon.setClickable(false);
        }
        loadJoinedMember(EMGroupId);
    }

    /**
     * 初始化领舞者的基本信息
     * @param EMGroupId
     */
    private void initLeaderView(String EMGroupId) {
        String url = HttpUtil.SPS_URL + "GetLeaderInfoServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("EMGroupId",EMGroupId)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LetDanceActivity.this,"请检查网络后重试！",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                if (!TextUtils.isEmpty(responseData)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject object = new JSONObject(responseData);
                                leaderName.setText(object.getString("name"));
                                String iconUrl = HttpUtil.SPS_SOURCE_URL +"usersIcon/"+object.getString("iconUrl");
                                Glide.with(LetDanceActivity.this).load(iconUrl).into(leaderIcon);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("ERROR3",3+"");
                            Toast.makeText(LetDanceActivity.this,"发生未知错误！",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 得到用户的账户
     * @param userId
     */
    private void getUserAccount(String userId){
        List<UsersInfo> usersInfoList = DataSupport.where("userId = ?",userId)
                .find(UsersInfo.class);
        for (UsersInfo usersInfo : usersInfoList){
            account = usersInfo.getAccount();
        }
    }

    /**
     * 初始化一些控件
     */
    private void initView() {
        titleBack= (Button) findViewById(R.id.title_back);

        TextView titleText = (TextView) findViewById(R.id.title_name);
        titleText.setText("舞吧");

        joinDance = (LinearLayout) findViewById(R.id.join_dance_layout);
        joinDanceBtn = (Button) findViewById(R.id.join_dance);

        ImageView titleRightView = (ImageView) findViewById(R.id.title_right_img);
        titleRightView.setVisibility(View.GONE);

        startDance = (Button) findViewById(R.id.title_right_btn);
        startDance.setText("开始");

        leaderName = (TextView) findViewById(R.id.leader_name);
        leaderIcon = (CircleImageView) findViewById(R.id.leader_icon);

        RecyclerView danceRecyclerView = (RecyclerView) findViewById(R.id.let_dance_recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        danceRecyclerView.setLayoutManager(manager);
        adapter = new FriendsAndTeamAdapter(danceList);
        danceRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new FriendsAndTeamAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position, String id) {

            }

            @Override
            public void OnItemCheckClick(View view, int position, boolean isChecked) {

            }
        });
    }

    /**
     * 没有领舞者，点击领舞者图片，弹出
     */
    private void showPopupWindow(){

        View contentView = LayoutInflater.from(LetDanceActivity.this).inflate(R.layout.pop_group_member_list,null);
        popupWindow = new PopupWindow(contentView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,true);
        Button popChooseButton = contentView.findViewById(R.id.pop_delete_member);
        Button cancelButton = contentView.findViewById(R.id.pop_cancel);

        View rootView = LayoutInflater.from(LetDanceActivity.this).inflate(R.layout.activity_group_member_list,null);
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM,0,0);

        popChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!account.equals(leaderAccount)){
                    sendBecomeLeader(EMGroupId,account);
                    popupWindow.dismiss();
                    leaderAccount = account;
                }else{
                    sendCancelDance(EMGroupId);
                    popupWindow.dismiss();
                    leaderAccount = "";
                }
            }
        });
        if (!account.equals(leaderAccount)){
            popChooseButton.setText("我要领舞");
        }else{
            popChooseButton.setText("取消领舞者");
        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }

    /**
     * 取消本次舞会
     * @param EMGroupId
     */
    private void sendCancelDance(String EMGroupId) {
        String url = HttpUtil.SPS_URL + "CancelDanceServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("EMGroupId",EMGroupId)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyApplication.getContext(),"请检查网络后重试！",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body().string().equals("OK")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MyApplication.getContext(),"取消舞会成功！",Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("ERROR2",2+"");
                            Toast.makeText(MyApplication.getContext(),"发生未知错误！",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 发送成为领舞者
     * @param EMGroupId //环信群id
     * @param account //客户账号
     */
    private void sendBecomeLeader(String EMGroupId, String account) {
        String url = HttpUtil.SPS_URL + "BecomeLeaderServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("EMGroupId",EMGroupId)
                .add("leaderAccount",account)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LetDanceActivity.this,"请检查网络后重试",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                if (!TextUtils.isEmpty(responseData)){
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           try {
                               JSONObject object = new JSONObject(responseData);
                               leaderName.setText(object.getString("name"));
                               Glide.with(LetDanceActivity.this).load(HttpUtil.SPS_SOURCE_URL +"usersIcon/"
                                       +object.getString("iconUrl")).into(leaderIcon);
                               Toast.makeText(LetDanceActivity.this,"成为领舞成功！",Toast.LENGTH_SHORT).show();
                           } catch (JSONException e) {
                               e.printStackTrace();
                           }
                       }
                   });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("ERROR1",1+"");
                            Toast.makeText(LetDanceActivity.this,"发生未知错误！",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 启动LetDance活动
     * @param context
     * @param userId //用户id
     * @param EMGroupId //群环信id
     */
    public static void startLetDanceActivity(Context context,String userId,String EMGroupId){
        Intent intent = new Intent(context,LetDanceActivity.class);
        intent.putExtra("userId",userId);
        intent.putExtra("EMGroupId",EMGroupId);
        context.startActivity(intent);
    }
}
