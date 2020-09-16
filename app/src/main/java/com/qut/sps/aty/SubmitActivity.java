package com.qut.sps.aty;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.util.ActivityCollector;
import com.qut.sps.util.BaseActivity;
import com.qut.sps.util.Constant;
import com.qut.sps.util.LoginUtil;
import com.qut.sps.util.MyApplication;
import com.qut.sps.util.MyCountDownTimer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class SubmitActivity extends BaseActivity implements View.OnClickListener{

    private Button codeButton;

    private EditText codeText;

    private String oldCode;

    private String tel;

    private Map<String,String> map = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);

        init();

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        String image = intent.getStringExtra("image");
        String name = intent.getStringExtra("name");
        String start = intent.getStringExtra("start");
        String end = intent.getStringExtra("end");
        String place = intent.getStringExtra("place");
        tel = intent.getStringExtra("tel");
        String description = intent.getStringExtra("description");

        map.put("id",id);
        map.put("image",image);
        map.put("name",name);
        map.put("start",start);
        map.put("end",end);
        map.put("place",place);
        map.put("tel",tel);
        map.put("description",description);

        getCode(tel);
    }

    private void init(){
        codeButton = (Button) findViewById(R.id.button);
        codeText = (EditText) findViewById(R.id.competition_code);
        Button submitButton = (Button) findViewById(R.id.submit);
        codeButton.setOnClickListener(this);
        submitButton.setOnClickListener(this);

        Button titleBack = (Button) findViewById(R.id.title_back);
        titleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView titleName = (TextView) findViewById(R.id.title_name);
        titleName.setText("手机验证");

        ImageView titleImage = (ImageView) findViewById(R.id.title_right_img);
        titleImage.setVisibility(View.GONE);
    }

    /**
     * 验证码获取方法
     * @param number 要接受验证码的手机号
     */
    private void getCode(final String number){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String,String> map = LoginUtil.getCode(number);
                Message msg = new Message();
                msg.what = 1;
                msg.obj = map;
                handler.sendMessage(msg);
            }
        }).start();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Map<String,String> codeMap = (Map<String, String>) msg.obj;
                    if("000".equals(codeMap.get("code"))){
                        Toast.makeText(SubmitActivity.this, "请求超时，请稍后重试", Toast.LENGTH_SHORT).show();
                    }else if (codeMap.get("message").equals("发送成功")){
                        MyCountDownTimer timer = new MyCountDownTimer(60000,1000);
                        timer.setBtn_djs(codeButton);
                        timer.start();
                        oldCode = codeMap.get("code");
                        Toast.makeText(SubmitActivity.this,"发送成功",Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(SubmitActivity.this,"发送失败,请稍后重试",Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button:
                getCode(tel);
                break;
            case R.id.submit:
                String code = codeText.getText().toString().trim();
                if (!code.equals("")) {
                    if (code.equals(oldCode)){
                       new Thread(new Runnable() {
                           @Override
                           public void run() {
                               doPost(map);
                           }
                       }).start();
                    }else {
                        Toast.makeText(SubmitActivity.this,"验证码错误",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(SubmitActivity.this,"请填写手机验证码",Toast.LENGTH_SHORT).show();
                }
        }
    }

    private String doPost(Map<String, String> map) {

        OkHttpClient mOkHttpClient = new OkHttpClient();
        String requestUrl = Constant.SPS_URL+"CompetitionCreatServlet";
        String result = "error";

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.addFormDataPart("image", map.get("image"),
                RequestBody.create(MediaType.parse("image/jpeg"), new File(map.get("image"))));


        builder.addFormDataPart("competitionName",map.get("name"));
        builder.addFormDataPart("userId",map.get("id"));
        builder.addFormDataPart("startTime",map.get("start"));
        builder.addFormDataPart("endTime",map.get("end"));
        builder.addFormDataPart("place",map.get("place"));
        builder.addFormDataPart("tel",map.get("tel"));
        builder.addFormDataPart("description",map.get("description"));

        RequestBody requestBody = builder.build();

        Request.Builder reqBuilder = new Request.Builder();
        Request request = reqBuilder
                .url(requestUrl)
                .post(requestBody)
                .build();
        try{
            Response response = mOkHttpClient.newCall(request).execute();

            String responseText = response.body().string();

            if (response.isSuccessful()) {

                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        showResponse(responseText);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void showResponse(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyApplication.getContext(),result,Toast.LENGTH_SHORT).show();
            }
        });
        if (result.equals("创建成功")){
            Intent intent = new Intent(SubmitActivity.this, MainActivity.class);
            startActivity(intent);
            ActivityCollector.finishAll();
        }else {
            finish();
        }
    }
}
