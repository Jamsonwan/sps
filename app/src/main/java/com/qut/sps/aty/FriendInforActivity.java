package com.qut.sps.aty;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.db.FriendMessage;
import com.qut.sps.db.MyFriends;
import com.qut.sps.db.NotifyMessage;
import com.qut.sps.util.ActivityCollector;
import com.qut.sps.util.BaseActivity;
import com.qut.sps.util.Constant;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.util.MyApplication;
import com.qut.sps.view.MyFragment;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;


public class FriendInforActivity extends BaseActivity {

    private CircleImageView friendIconView;
    private ImageView backgroundView;

    private TextView friendAccountView;
    private TextView friendNickNameView;
    private TextView friendSexView;
    private TextView friendAgeView;
    private TextView friendTelView;
    private TextView friendAddressView;
    private TextView friendProfessionView;

    private EditText friendNoteView;
    private Button saveChangedNote;

    private String previousNote;
    private String friendId;
    private String friendAccount;

    private Button deleteMessageButton;
    private Button deleteFriendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_friend_infor);
        friendId = getIntent().getStringExtra("friendId");
        initView();
        previousNote = getPreviousNote();
        initEvent();
    }

    /**
     * 得到以前的好友备注
     * @return
     */
    private String getPreviousNote() {
        String note = null;
        List<MyFriends> myFriendsList = DataSupport.where("friendId = ? and userId =?",friendId,MyFragment.userId)
                .find(MyFriends.class);
        for (MyFriends myFriends:myFriendsList){
            note = myFriends.getNote();
        }
        return note;
    }

    /**
     * 初始化点击事件
     */
    private void initEvent() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        friendAccount = preferences.getString(Constant.CURRENT_CHAT_ID,null);

        friendNoteView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() > 10){
                    friendNoteView.setError("好友备注名过长！");
                }
                if (!editable.toString().equals(previousNote)){
                    saveChangedNote.setVisibility(View.VISIBLE);
                }
            }
        });
        saveChangedNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(! friendNoteView.getText().toString().equals(previousNote) &&
                        friendNoteView.getText().toString().length() < 20 &&
                        friendNoteView.getText().toString().length() > 0){
                    saveNoteOnServer();
                    upDateLocalDB();
                    finish();
                }else {
                    finish();
                }
            }
        });

        deleteMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               DeleteMyFriendMessage(friendId,friendAccount);
                Toast.makeText(FriendInforActivity.this,"清除聊天记录成功！",Toast.LENGTH_SHORT).show();
            }
        });

        deleteFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FriendInforActivity.this);
                builder.setCancelable(false);
                builder.setTitle("提醒");
                builder.setMessage("确定要删除该好友？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DeleteMyFriend(friendId,friendAccount);
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.show();
            }
        });
    }

    /**
     * 向服务器请求删除好友
     * @param friendId
     * @param friendAccount
     */
    private void DeleteMyFriend(String friendId, final String friendAccount) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().contactManager().deleteContact(friendAccount,false);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    Log.d("删除联系人失败，",e.getMessage());
                }
            }
        }).start();

        DataSupport.deleteAll(MyFriends.class,"userId=? and friendId=?",MainActivity.userId,friendId);
        DataSupport.deleteAll(NotifyMessage.class,"userId=? and EMId = ?",MainActivity.userId,friendAccount);

        String url = HttpUtil.SPS_URL + "DeleteFriendServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("friendId",friendId)
                .add("userId",MainActivity.userId)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(FriendInforActivity.this,"请检查网络后重试！",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                if (!TextUtils.isEmpty(responseData)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MyApplication.getContext(),"删除好友成功！",Toast.LENGTH_SHORT).show();
                        }
                    });
                    Intent intent = new Intent(MyApplication.getContext(),MainActivity.class);
                    startActivity(intent);
                    ActivityCollector.finishAll();
                }else {
                    Toast.makeText(FriendInforActivity.this,"发生未知错误！",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 删除好友聊天记录
     * @param friendId
     */
    private void DeleteMyFriendMessage(String friendId,String friendAccount) {
        DataSupport.deleteAll(FriendMessage.class,"friendId = ? and userId = ?",friendId, MainActivity.userId);
        if (friendAccount != null){
            DataSupport.deleteAll(NotifyMessage.class,"userId=? and EMId = ?",MainActivity.userId,friendAccount);
        }
    }

    /**
     * 跟新本地数据库
     */
    private void upDateLocalDB() {
        MyFriends myFriend = new MyFriends();
        myFriend.setNote(friendNoteView.getText().toString());
        myFriend.updateAll("userId = ? and friendId = ?",MyFragment.userId,friendId);

        NotifyMessage notifyMessage = new NotifyMessage();
        notifyMessage.setName(friendNoteView.getText().toString());
        notifyMessage.updateAll("userId=? and EMId = ?",MainActivity.userId,friendAccount);
    }

    /**
     * 保存好友备注到服务器
     */
    private void saveNoteOnServer() {
        String url = HttpUtil.SPS_URL + "SaveFriendNoteServlet";
        final RequestBody requestBody = new FormBody.Builder()
                .add("friendId",friendId)
                .add("userId",MyFragment.userId)
                .add("note",friendNoteView.getText().toString())
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(FriendInforActivity.this,"修改失败！请稍后重试！",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                if (! TextUtils.isEmpty(responseData)){
                    try {
                        JSONObject object = new JSONObject(responseData);
                        if (! object.getString("result").equals("OK")){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(FriendInforActivity.this,"修改失败！请稍后重试！",Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(FriendInforActivity.this,"修改失败！请稍后重试！",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(FriendInforActivity.this,"修改失败！请稍后重试！",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 返回按钮
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return  true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 初始化界面的一些控件
     */
    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.friend_info_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        CollapsingToolbarLayout collapsingToolbar = ( CollapsingToolbarLayout) findViewById(R.id.my_friend_collapsing_toolbar);
        collapsingToolbar.setTitle("好友资料");

        backgroundView = (ImageView) findViewById(R.id.my_friend_background_img);
        loadBackground();

        friendIconView = (CircleImageView) findViewById(R.id.friend_info_icon);

        saveChangedNote = (Button) findViewById(R.id.save_note_change);

        friendAccountView = (TextView) findViewById(R.id.friend_account);
        friendNickNameView = (TextView) findViewById(R.id.friend_nick_name);
        friendSexView = (TextView) findViewById(R.id.friend_sex);
        friendAgeView = (TextView) findViewById(R.id.friend_age);
        friendTelView = (TextView) findViewById(R.id.friend_tel);
        friendAddressView = (TextView) findViewById(R.id.friend_address);
        friendProfessionView = (TextView) findViewById(R.id.friend_profession);
        friendNoteView = (EditText) findViewById(R.id.friend_note);

        deleteFriendButton = (Button) findViewById(R.id.delete_friend);
        deleteMessageButton = (Button) findViewById(R.id.delete_message);

        loadFriendInfo();
    }

    /**
     * 加载好友的基本信息
     */
    private void loadFriendInfo() {
        String url = HttpUtil.SPS_URL+"QueryMyFriendInfoServlet";
        final RequestBody requestBody = new FormBody.Builder()
                .add("userId", MyFragment.userId)
                .add("friendId",friendId)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       Toast.makeText(FriendInforActivity.this,"加载失败！请检查网络后重试！",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                if(!TextUtils.isEmpty(responseData)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject object = new JSONObject(responseData);

                                friendAccountView.setText(object.getString("account"));
                                friendAccountView.setTextColor(Color.BLACK);

                                if ( ! object.getString("nickName").equals("null")){
                                    friendNickNameView.setText(object.getString("nickName"));
                                    friendNickNameView.setTextColor(Color.BLACK);
                                }

                                if ( ! object.getString("note").equals("null")){
                                    friendNoteView.setText(object.getString("note"));
                                    friendNoteView.setTextColor(Color.BLACK);
                                }

                                if (! object.getString("age").equals("null")){
                                    friendAgeView.setText(object.getString("age"));
                                    friendAgeView.setTextColor(Color.BLACK);
                                }

                                if (!object.getString("address").equals("null")){
                                    friendAddressView.setText(object.getString("address"));
                                    friendAddressView.setTextColor(Color.BLACK);
                                }

                                if (! object.getString("sex").equals("null")){
                                    friendSexView.setText(object.getString("sex"));
                                    friendSexView.setTextColor(Color.BLACK);
                                }

                                if (! object.getString("tel").equals("null")){
                                    friendTelView.setText(object.getString("tel"));
                                    friendTelView.setTextColor(Color.BLACK);
                                }

                                if (! object.getString("profession").equals("null")){
                                    friendProfessionView.setText(object.getString("profession"));
                                    friendProfessionView.setTextColor(Color.BLACK);
                                }
                                Glide.with(FriendInforActivity.this).load(Constant.SPS_SOURCE_URL+"/"+object.getString("iconUrl"))
                                        .into(friendIconView);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }
    /**
     * 加载必应图片作为背景
     */
    private void loadBackground() {
        HttpUtil.sendOkHttpRequest(HttpUtil.GUOLIN_PIC,new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String backgroundPic = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(FriendInforActivity.this).load(backgroundPic).into(backgroundView);
                    }
                });
            }
        });
    }
}
