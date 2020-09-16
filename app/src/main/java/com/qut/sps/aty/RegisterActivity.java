package com.qut.sps.aty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.qut.sps.R;
import com.qut.sps.util.BaseActivity;
import com.qut.sps.util.Constant;
import com.qut.sps.util.LoginUtil;
import com.qut.sps.util.MD5;
import com.qut.sps.util.MyApplication;
import com.qut.sps.util.MyCountDownTimer;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private EditText nickName;

    private EditText account;

    private EditText password;

    private EditText againPwd;

    private EditText codeText;

    private Button register_in_button;

    private Button getCode_button;

    private String number;

    private String oldCode = "";

    private static final String  PASSWORD = "^[a-zA-Z0-9]{6,12}$";
    private Pattern pattern = Pattern.compile(PASSWORD);
    private Matcher matcher;


    public boolean validatePassword(String password) {
        matcher = pattern.matcher(password);
        return matcher.matches();
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();

    }
    private void init(){
        nickName = (EditText) findViewById(R.id.nickText);
        account = (EditText) findViewById(R.id.number);
        password = (EditText) findViewById(R.id.pwd);
        againPwd = (EditText) findViewById(R.id.againPwd);
        codeText = (EditText) findViewById(R.id.code_reg);
        getCode_button = (Button) findViewById(R.id.getCode_button);
        register_in_button = (Button) findViewById(R.id.register_in_button);
        getCode_button.setOnClickListener(this);
        register_in_button.setOnClickListener(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    @Override
    public void onClick(View view) {
        number = this.account.getText().toString().trim();
        String nickName = this.nickName.getText().toString().trim();
        String password = this.password.getText().toString().trim();
        String code = codeText.getText().toString().trim();
        switch (view.getId()) {
            case R.id.getCode_button:
                if (!number.equals(null)) {
                    if (number.length() == 11) {
                        Register_in(nickName,number, "null");
                    } else {
                        Toast.makeText(RegisterActivity.this, "手机号格式不正确", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(RegisterActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.register_in_button:

                if (!code.equals(null)) {
                    if (oldCode.equals(code)) {
                        if (againPwd.getText().toString().trim() != null && password != null) {
                            if (validatePassword(password)) {
                                if (!againPwd.getText().toString().equals(password)) {
                                    Toast.makeText(RegisterActivity.this, "两次密码输入不一致", Toast.LENGTH_SHORT).show();
                                } else {
                                    String pwd = MD5.EncoderByMd5(password);
                                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
                                    editor.putString(Constant.MD5_PASSWORD,pwd);
                                    editor.apply();
                                    Register_in(nickName,number,pwd);
                                }
                            } else {
                                Toast.makeText(RegisterActivity.this, "密码不符合规范", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "验证码输入错误", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "验证码不能为空", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private void getCode(final String number){
        final String[] result = {null};
        new Thread(new Runnable() {
            @Override
            public void run() {
               Map<String,String> map = LoginUtil.getCode(number);
                Message msg = new Message();
                msg.what = 2;
                msg.obj = map;
                handler.sendMessage(msg);
            }
        }).start();
    }


    /**
     * 注册方法
     * @param account 用户名
     * @param password 密码
     * @return 是否注册成功
     */
    private void Register_in(final String nickName, final String account, final String password){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                Map<String,String> map = LoginUtil.signUp(nickName,account,password);
                msg.what = 1;
                msg.obj = map;
                handler.sendMessage(msg);
            }
        }).start();
    }

    private  void signup(final String account){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 3;
                msg.arg1 = 0;
                try {
                    EMClient.getInstance().createAccount(account,"123456");
                    msg.arg1 = 1;
                    handler.sendMessage(msg);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    Log.e("注册失败",e.getErrorCode()+e.getMessage());
                }
            }
        }).start();

    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Map<String,String> msgMap = (Map<String, String>) msg.obj;
                    if ("000".equals(msgMap.get("code"))){
                        Toast.makeText(RegisterActivity.this,"请求超时，请稍后重试",Toast.LENGTH_SHORT).show();
                    }else if ("400".equals(msgMap.get("code"))){
                         getCode(number);
                    }else if ("200".equals(msgMap.get("code"))){
                        signup(number);
                    }else {
                        Toast.makeText(RegisterActivity.this,"该账号已注册",Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2:
                    Map<String,String> codeMap = (Map<String, String>) msg.obj;
                    if("000".equals(codeMap.get("code"))){
                        Toast.makeText(RegisterActivity.this, "请求超时，请稍后重试", Toast.LENGTH_SHORT).show();
                    }else if (codeMap.get("message").equals("发送成功")){
                        MyCountDownTimer timer = new MyCountDownTimer(60000,1000);
                        timer.setBtn_djs(getCode_button);
                        timer.start();
                        oldCode = codeMap.get("code");
                        Toast.makeText(RegisterActivity.this,"发送成功",Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(RegisterActivity.this,"发送失败,请稍后重试",Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 3:
                    int choice = msg.arg1;
                    if (choice == 1){
                        Toast.makeText(RegisterActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        Toast.makeText(RegisterActivity.this,"注册失败",Toast.LENGTH_SHORT).show();
                    }
            }
        }
    };

}