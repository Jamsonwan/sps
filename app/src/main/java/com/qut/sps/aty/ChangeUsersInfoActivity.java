package com.qut.sps.aty;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.adapter.PersonalAdapter;
import com.qut.sps.adapter.PersonalData;
import com.qut.sps.db.UsersInfo;
import com.qut.sps.util.Constant;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.util.Utility;

import org.json.JSONException;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

;

public class ChangeUsersInfoActivity extends AppCompatActivity {

    private List<PersonalData> pdList=new ArrayList<>();
    private  String account1;
    private  String nick1;
    private  String sex1;
    private  String age1;
    private  String tel1;
    private  String address1;
    private  String profession1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_users_info);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.change_user_info_collapsing_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        collapsingToolbarLayout.setTitle("个人信息");

        initView();

        adjustStatusBar();

        try {
            queryData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        ImageView groundImg=(ImageView)findViewById(R.id.background_view);
        Glide.with(this).load(R.drawable.backgroundimg).into(groundImg);

        PersonalAdapter adapter=new PersonalAdapter(ChangeUsersInfoActivity.this,R.layout.person_data,pdList);
        ListView listView=(ListView)findViewById(R.id.list1);
        listView.setAdapter(adapter);
    }

    /**
     * 调整状态栏透明度
     */
    private void adjustStatusBar() {

        if(Build.VERSION.SDK_INT>= 21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * 查询个人信息
     * @throws IOException
     */
    private void queryData() throws IOException {
        List<UsersInfo> userInfoList = DataSupport.where("account=?", MainActivity.userAccount).find(UsersInfo.class);
        if(userInfoList.size()!=0){
            for (UsersInfo userInfo : userInfoList) {
                account1 = userInfo.getAccount();
                nick1 = userInfo.getNickName();
                sex1=userInfo.getSex();
                age1= String.valueOf(userInfo.getAge());
                tel1=userInfo.getTel();
                address1= userInfo.getAddress();
                profession1= userInfo.getProfession() ;
            }
        }else{
            queryFromServer();
        }

        if(nick1.equals("null")){
            nick1="未设置";
        }
        if(sex1.equals("null")){
            sex1="未设置";
        }
        if(age1.equals("null")){
            age1="未设置";
        }
        if(tel1.equals("null")){
            tel1="未设置";
        }
        if(address1.equals("null")){
            address1="未设置";
        }
        if(profession1.equals("null")){
            profession1="未设置";
        }

        PersonalData account=new PersonalData("账号",account1);
        pdList.add(account);
        PersonalData nickName=new PersonalData("昵称",nick1);
        pdList.add(nickName);
        PersonalData sex=new PersonalData("性别",sex1);
        pdList.add(sex);
        PersonalData age=new PersonalData("年龄",age1);
        pdList.add(age);
        PersonalData tel=new PersonalData("联系方式",tel1);
        pdList.add(tel);
        PersonalData address=new PersonalData("地址",address1);
        pdList.add(address);
        PersonalData profession=new PersonalData("职业",profession1);
        pdList.add(profession);

    }

    /**
     * 根据传入的地址从服务器上查询个人信息
     * @throws IOException
     */
    private void queryFromServer() throws IOException {
        String url = Constant.SPS_URL+"GetUsersInforServlet";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(ChangeUsersInfoActivity.this, "查询失败", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result;
                try {
                    result = Utility.handleUsersInfor(responseText);//进行json数据解析
                    if (result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    queryData();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}

