package com.qut.sps.aty;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.qut.sps.R;
import com.qut.sps.util.HttpUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ShowAddFriendActivity extends AppCompatActivity {

    private ImageView back;
    private ImageView bingPicImg;
    private CircleImageView circleImageView;
    private ListView listView;
    private Button button;
    private ProgressDialog progressDialog;

    private String addName;
    private String addAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_show_add_friend);


        initView();
        initLisenter();

    }

    private void initLisenter() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend(view);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initView() {
        back = (ImageView)findViewById(R.id.back_friend);
        circleImageView = (CircleImageView)findViewById(R.id.show_icon);
        listView = (ListView)findViewById(R.id.y_friend_info);
        button = (Button)findViewById(R.id.real_add);

        bingPicImg = (ImageView)findViewById(R.id.bing_pic_img);
        //加载必应每日一图
        loadBingPic();
        //显示除头像以外的个人信息
        Intent intent = getIntent();
        Map<String, String> message = (Map<String, String>) intent.getSerializableExtra("info");
        String [] data={"账号       "+message.get("账号"),"昵称       "+message.get("昵称"),"性别       "+message.get("性别"),
                "年龄       "+message.get("年龄"),"职业       "+message.get("职业"),"所在地    "+message.get("地址")};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,data);
        listView.setAdapter(adapter);
        //显示头像
        loadIcon(message.get("头像"));

        //保存待添加好友的账号,昵称
        addName=message.get("昵称");
        addAccount = message.get("账号");
        //判断是否已为好友，是则不显示加好友按钮
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> usernames = null;
                boolean flag=true;
                try {
                    usernames = EMClient.getInstance().contactManager().getAllContactsFromServer();
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
                if (usernames == null){
                    flag = true;
                }else {
                    for(String account:usernames){
                        if(account.equals(addAccount)){
                            flag=false;
                            break;
                        }
                    }
                }
                if(flag){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            button.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }).start();

    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";

        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(ShowAddFriendActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    private void loadIcon(String icon){
        final String requestIcon = HttpUtil.SPS_SOURCE_URL+"usersIcon/"+icon;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(ShowAddFriendActivity.this).load(requestIcon).into(circleImageView);
            }
        });
    }

    public void addFriend(View view){

        progressDialog = new ProgressDialog(this);
        String stri = "正在发送请求...";
        progressDialog.setMessage(stri);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        new Thread(new Runnable() {
            public void run() {

                try {
                    //demo use a hardcode reason here, you need let user to input if you like
                    String s = "我是"+EMClient.getInstance().getCurrentUser();
                    EMClient.getInstance().contactManager().addContact(addAccount, s);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s1 ="发送成功!";
                            Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s2 = "添加请求失败!";
                            Toast.makeText(getApplicationContext(), s2 + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                finish();
            }
        }).start();

    }
}

