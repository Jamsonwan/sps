package com.qut.sps.aty;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.util.Constant;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.util.MyApplication;

import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ChangePasswordActivity extends AppCompatActivity {

    private EditText oldPassword;
    private EditText newPassword;
    private EditText affirmPassword;
    private Button submit;
    private Button back;
    private String oldPwd;
    private String newPwd;
    private String affirmPwd;
    private static final String  PASSWORD = "^[a-zA-Z0-9]{6,12}$";
    private Pattern pattern = Pattern.compile(PASSWORD);
    private Matcher matcher;


    public boolean validatePassword(String password) {
        matcher = pattern.matcher(password);
        return matcher.matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        initView();

        init();

    }

    private void init() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassord();
            }
        });
        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                (ChangePasswordActivity.this).finish();
            }
        });
    }

    private void initView() {
        oldPassword = (EditText) findViewById(R.id.old_password);
        newPassword = (EditText) findViewById(R.id.new_password);
        affirmPassword = (EditText) findViewById(R.id.affirm_password);

        submit = (Button) findViewById(R.id.title_right_btn);
        submit.setVisibility(View.VISIBLE);
        submit.setText("提交");
        ImageView imageView = (ImageView) findViewById(R.id.title_right_img);
        imageView.setVisibility(View.GONE);
        TextView titleText = (TextView) findViewById(R.id.title_name);
        titleText.setText("修改密码");
        back=(Button) findViewById(R.id.title_back);


    }


    /**
     * 修改密码
     */

    private void changePassord() {

        oldPwd = oldPassword.getText().toString().trim();
        newPwd = newPassword.getText().toString().trim();
        affirmPwd = affirmPassword.getText().toString().trim();

        if (!validatePassword(newPwd)) {
            popWarning();
        } else if (newPwd.length()==0){
            newPassword.setError("密码不得为空");
            newPassword.requestFocus();
        }else{
            updatePwd();
        }
    }

    /**
     * 符合条件的情况下修改密码
     */
    private void updatePwd() {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String myPwd = pref.getString(Constant.MD5_PASSWORD,null);
        if(myPwd == null){
            return;
        }
        boolean rs;
        MD5 md5 = new MD5();
        //myPwd = md5.EncoderByMd5(myPwd);
        rs = md5.checkpassword(oldPwd, myPwd);
        final String newPwd1 = md5.EncoderByMd5(newPwd);

        if (rs) {
            try {
                if (md5.checkpassword(affirmPwd, newPwd1)) {
                    RequestBody requestBody = new FormBody.Builder()
                            .add("pwd", newPwd1)
                            .add("id", MainActivity.userId)//id到时候要重写
                            .build();
                    String url = Constant.SPS_URL + "ChangPwdServlet";
                    HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            req();
                        }

                        private void req() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ChangePasswordActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String responseText = response.body().string();
                            handMessage(responseText);//进行json数据解析
                        }

                        private void handMessage(String responseText) {
                            if (!TextUtils.isEmpty(responseText)) {
                                try {
                                    JSONObject object = new JSONObject(responseText);
                                    String result = object.getString("result");
                                    showResponse(result);

                                    SharedPreferences.Editor editor1 = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
                                    editor1.putString(Constant.MD5_PASSWORD,newPwd1);
                                    editor1.apply();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        private void showResponse(final String response) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MyApplication.getContext(), response, Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                        }
                    });
                } else {
                    affirmPassword.setError("两次密码不正确");
                    affirmPassword.requestFocus();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            oldPassword.setError("旧密码不正确");
            oldPassword.requestFocus();
        }
    }

    /**
     * 弹出提示框
     */
    private void popWarning() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(ChangePasswordActivity.this);
        dialog.setTitle("注意咯~~~");
        dialog.setIcon(R.drawable.img6);
        dialog.setMessage("        密码不规范");
        dialog.setCancelable(false);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog.show();
    }

    /**
     * MD5类主要实现MD5加密和密码比较
     */
    public class MD5 {
        public String EncoderByMd5(String str) {
            if (TextUtils.isEmpty(str)) {
                return "";
            }
            MessageDigest md5 = null;
            try {
                md5 = MessageDigest.getInstance("MD5");
                byte[] bytes = md5.digest(str.getBytes());
                String result = "";
                for (byte b : bytes) {
                    String temp = Integer.toHexString(b & 0xff);
                    if (temp.length() == 1) {
                        temp = "0" + temp;
                    }
                    result += temp;
                }
                return result;
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return "";
        }

        public boolean checkpassword(String newpasswd, String oldpasswd) {
            if (EncoderByMd5(newpasswd).equals(oldpasswd)) {
                return true;
            } else {
                return false;
            }
        }
    }
}


