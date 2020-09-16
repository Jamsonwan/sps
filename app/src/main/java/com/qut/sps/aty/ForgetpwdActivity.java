package com.qut.sps.aty;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.qut.sps.R;
import com.qut.sps.util.BaseActivity;
import com.qut.sps.util.LoginUtil;
import com.qut.sps.util.MD5;
import com.qut.sps.util.MyCountDownTimer;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgetpwdActivity extends BaseActivity implements View.OnClickListener{

    private EditText numberText;

    private EditText code1;

    private EditText pwdText;

    private Button code_button;

    private Button submit_button;

    private String number;

    private String code;


    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgetpwd);
        numberText = (EditText) findViewById(R.id.numberText);
        code1 = (EditText) findViewById(R.id.code_fgd);
        pwdText = (EditText) findViewById(R.id.pwdText);
        code_button = (Button) findViewById(R.id.code_button);
        submit_button = (Button) findViewById(R.id.submit_button);

        code_button.setOnClickListener(this);
        submit_button.setOnClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
        @Override
    public void onClick(View view) {

            number = numberText.getText().toString();
            String pwd = pwdText.getText().toString().trim();
            switch (view.getId()){
                case R.id.code_button:
                    if(!number.equals("")){
                        if (number.length() == 11){
                            Register_in(number,"null");
                        }else {
                            Toast.makeText(ForgetpwdActivity.this,"请填写正确的手机号码",Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(ForgetpwdActivity.this,"请填写手机号",Toast.LENGTH_SHORT).show();
                    }


                    break;
                case R.id.submit_button:
                    String oldCode = code1.getText().toString().trim();
                    if(!oldCode.equals("")) {
                        if (oldCode.equals(code)) {
                            if (validatePassword(pwd)) {
                                String pass = MD5.EncoderByMd5(pwd);
                                updatePassword(number, pass);
                            }else {
                                Toast.makeText(ForgetpwdActivity.this, "密码不符合规范", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ForgetpwdActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(ForgetpwdActivity.this, "验证码不能为空", Toast.LENGTH_SHORT).show();
                    }
            }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private void updatePassword(final String account, final String password){
       new Thread(new Runnable() {
           @Override
           public void run() {
               Map<String,String> map = LoginUtil.updatePassword(account,password);
               Message msg = new Message();
               msg.what = 3;
               msg.obj = map;
               handler.sendMessage(msg);
           }
       }).start();
    }
    private void getCode(final String number){
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
    private void Register_in(final String account, final String password){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                Map<String,String> map = LoginUtil.signUp("null",account,password);
                msg.what = 1;
                msg.obj = map;
                handler.sendMessage(msg);
            }
        }).start();

    }

    private              Handler handler  = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Map<String, String> msgMap = (Map<String, String>) msg.obj;
                    if ("000".equals(msgMap.get("code"))){
                        Toast.makeText(ForgetpwdActivity.this, "请求超时，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                    if ("400".equals(msgMap.get("code"))) {
                        Toast.makeText(ForgetpwdActivity.this, "该账号尚未注册", Toast.LENGTH_SHORT).show();
                    } else {
                        getCode(number);
                    }
                    break;
                case 2:
                    Map<String, String> codeMap = (Map<String, String>) msg.obj;
                    if("000".equals(codeMap.get("code"))){
                        Toast.makeText(ForgetpwdActivity.this, "请求超时，请稍后重试", Toast.LENGTH_SHORT).show();
                    }else if (codeMap.get("message").equals("发送成功")) {
                        MyCountDownTimer timer = new MyCountDownTimer(60000, 1000);
                        timer.setBtn_djs(code_button);
                        timer.start();
                        code = codeMap.get("code");
                        Toast.makeText(ForgetpwdActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ForgetpwdActivity.this, "发送失败,请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 3:
                    Map<String,String> upMap = (Map<String, String>) msg.obj;
                    if ("修改成功".equals(upMap.get("message"))){
                        Toast.makeText(ForgetpwdActivity.this,"修改成功",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ForgetpwdActivity.this,LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        Toast.makeText(ForgetpwdActivity.this,"修改失败",Toast.LENGTH_SHORT).show();
                    }
            }
        }
    };
    private static final String  PASSWORD = "^[a-zA-Z0-9]{6,12}$";
    private              Pattern pattern  = Pattern.compile(PASSWORD);
    private Matcher matcher;


    public boolean validatePassword(String password) {
        matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
