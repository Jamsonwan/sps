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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.util.HttpUtil;

import java.io.IOException;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShowAddGroupActivity extends AppCompatActivity {

    private ImageView back;
    private ImageView bingPicImg;
    private CircleImageView circleImageView;
    private ListView listView;
    private Button button;

    Map<String,String> message;

    private String groupid;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_show_add_group);

        initView();
        initEvent();
    }

    private void initView() {
        back = (ImageView)findViewById(R.id.back_group);
        bingPicImg = (ImageView)findViewById(R.id.bing_pic_img_2);
        circleImageView = (CircleImageView)findViewById(R.id.y_show_icon);
        listView = (ListView)findViewById(R.id.y_group_info);
        button = (Button)findViewById(R.id.real_add_group);
        progressBar = (ProgressBar)findViewById(R.id.loading);


        //加载必应每日一图
        loadBingPic();
        //显示除头像,团队名称以外的团队信息
        Intent intent = getIntent();
        message = (Map<String,String>)intent.getSerializableExtra("info");
        String [] data = {"团名     "+message.get("团名"),"团长     "+message.get("团长"),"团队介绍   "+message.get("团队介绍")};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,data);
        listView.setAdapter(adapter);
        //显示头像，团名
        loadIcon(message.get("头像"));
        showGroupDetail();
        groupid=message.get("EMGroupId");
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
                        Glide.with(ShowAddGroupActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    private void initEvent() {
        //申请加入团队
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToGroup(view);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    //join the group
    public void addToGroup(View view){
        String st1 = "正在发送请求...";
        final String st2 = "申请加入 "+message.get("团名");
        final String st3 = "请求已发送，请耐心等待团长审核";
        final String st5 = "加入团队失败!";
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(st1);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    //if group is membersOnly，you need apply to join
                    EMClient.getInstance().groupManager().applyJoinToGroup(groupid, st2);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(ShowAddGroupActivity.this, st3, Toast.LENGTH_SHORT).show();
                            button.setEnabled(false);
                        }
                    });
                } catch (final HyphenateException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(ShowAddGroupActivity.this, st5+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void showGroupDetail() {
        progressBar.setVisibility(View.INVISIBLE);

        //get group detail, and you are not in, then show join button
        String url = HttpUtil.SPS_URL+"CheckIfAlreadyInGroupServlet";
        final RequestBody requestBody = new FormBody.Builder()
                .add("groupId",message.get("id"))
                .add("userId", MainActivity.userId)//获取当前用户Id
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ShowAddGroupActivity.this,"验证失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if(responseText.equals("不存在")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            button.setVisibility(View.VISIBLE);
                            button.setEnabled(true);
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ShowAddGroupActivity.this,"你已是该团队成员!",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void loadIcon(String icon){
        final String requestIcon = HttpUtil.SPS_SOURCE_URL+"groupsIcon/"+icon;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(ShowAddGroupActivity.this).load(requestIcon).into(circleImageView);
            }
        });
    }
}
