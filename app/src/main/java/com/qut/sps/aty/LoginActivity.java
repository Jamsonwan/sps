package com.qut.sps.aty;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.db.Groups;
import com.qut.sps.util.ActivityCollector;
import com.qut.sps.util.BaseActivity;
import com.qut.sps.util.Constant;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.util.LoginUtil;
import com.qut.sps.util.MD5;
import com.qut.sps.util.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener{

    private TextInputLayout account;

    private TextInputLayout password;

    private String aot;

    private String userId;

    private String pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        account = (TextInputLayout) findViewById(R.id.account);
        password = (TextInputLayout) findViewById(R.id.pwd);

        account.setHint("手机号");
        password.setHint("密码");

        Button sign_in_button = (Button) findViewById(R.id.sign_in_button);
        TextView forgetPwd = (TextView) findViewById(R.id.forgetPwd);
        TextView tiaozhuan = (TextView) findViewById(R.id.tiaozhuan);
        TextView registerText = (TextView) findViewById(R.id.register_Text);

        account.setOnClickListener(this);
        password.setOnClickListener(this);
        sign_in_button.setOnClickListener(this);
        forgetPwd.setOnClickListener(this);
        tiaozhuan.setOnClickListener(this);
        registerText.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                aot = account.getEditText().getText().toString();
                pwd = MD5.EncoderByMd5(password.getEditText().getText().toString());
                if (!aot.equals("")) {
                   account.setErrorEnabled(false);
                    if (aot.length() == 11) {
                        account.setErrorEnabled(false);
                        if(!pwd.equals("")){
                            password.setErrorEnabled(false);
                            sign_in("",aot, pwd);
                        }else {
                            password.setErrorEnabled(true);
                            password.setError("请输入密码");
                        }
                    } else {
                        account.setErrorEnabled(true);
                        account.setError("您输入的手机号格式不正确");
                    }
                }else {
                    account.setErrorEnabled(true);
                    account.setError("请输入手机号");
                }
                break;
            case R.id.forgetPwd:
               myIntent(ForgetpwdActivity.class);
                break;
            case R.id.tiaozhuan:
                myIntent(MainActivity.class);
                ActivityCollector.finishAll();
                break;
            case R.id.register_Text:
                myIntent(RegisterActivity.class);
                break;
        }
    }
    public void  myIntent(Class<?> cls) {
        Intent intent = new Intent(LoginActivity.this,cls);
        startActivity(intent);
    }
    private void sign_in(final String nickName,final String account, final String pwd){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String,String> map = LoginUtil.signIn(nickName,account, pwd);
                Message msg = new Message();
                msg.what = 1;
                msg.obj = map;
                handler.sendMessage(msg);
            }
        }).start();

    }
    public void signin(String account, final String id){
        queryMyBuildTeamFromServer();
        queryMyJoinedTeamFromServer();
        myIntent(MainActivity.class);
        ActivityCollector.finishAll();
    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Map<String,String> map = (Map<String, String>) msg.obj;
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
                    try {
                        if("000".equals(map.get("code"))){
                            Toast.makeText(LoginActivity.this, "请求超时，请稍后重试", Toast.LENGTH_SHORT).show();
                        }else if("200".equals(map.get("code"))) {
                            userId = map.get("message");
                            Log.d("用户id",userId);
                            editor.putString(Constant.CURRENT_USER_ID, userId);
                            editor.putString(Constant.CURRENT_ACCOUNT,aot);
                            editor.putString(Constant.MD5_PASSWORD,pwd);
                            editor.apply();
                            signin(aot,userId);
                        }else {
                            Toast.makeText(LoginActivity.this,map.get("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

            }
        }
    };


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
}

