package com.qut.sps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.qut.sps.aty.AboutUsActivity;
import com.qut.sps.aty.AddFriendActivity;
import com.qut.sps.aty.AddGroupActivity;
import com.qut.sps.aty.ChangeUsersInfoActivity;
import com.qut.sps.aty.CreateGroupActivity;
import com.qut.sps.aty.GetVersionActivity;
import com.qut.sps.aty.LoginActivity;
import com.qut.sps.aty.Notify;
import com.qut.sps.aty.ReflectActivity;
import com.qut.sps.aty.SettingActivity;
import com.qut.sps.aty.UserIconActivity;
import com.qut.sps.aty.WeatherMainActivity;
import com.qut.sps.db.UsersInfo;
import com.qut.sps.server.NewMessageService;
import com.qut.sps.server.VisitService;
import com.qut.sps.util.Constant;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.util.MyApplication;
import com.qut.sps.util.Utility;
import com.qut.sps.view.ClothesFragment;
import com.qut.sps.view.DanceBarFragment;
import com.qut.sps.view.VideoFragment;

import org.json.JSONException;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity  {

    public static String userId;
    public static String userAccount;

    private TabLayout mTabLayout;

    private ImageView addMenu;


    private ViewPager mViewPager;

    private  CircleImageView slideMenuButton ;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private LinearLayout setting;
    private LinearLayout weather;
    private LinearLayout exit;
    private CircleImageView headIcon;
    private TextView userNickNameView;

    private LinearLayout addFriend;
    private LinearLayout addGroup;
    private LinearLayout createGroup;

    private Intent clockIntent;
    private Intent intentMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adjustStatusBar();
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        userId = preferences.getString(Constant.CURRENT_USER_ID,null);
        userAccount = preferences.getString(Constant.CURRENT_ACCOUNT,null);

        clockIntent=new Intent(this, VisitService.class);
        intentMessage = new Intent(this, NewMessageService.class);


        if(userId != null){
            startService(intentMessage);
            login(userAccount);
            startService(clockIntent);
        }
        initViews();
        initEvents();
        init();
    }

    /**
     * 登录环信
     */
    private void login(String userAccount) {
        EMClient.getInstance().login(userAccount, "123456", new EMCallBack() {
            @Override
            public void onSuccess() {
                EMClient.getInstance().groupManager().loadAllGroups();
                EMClient.getInstance().chatManager().loadAllConversations();
                try {
                    EMClient.getInstance().groupManager().getJoinedGroupsFromServer();
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(int i, final String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,s,Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
    }

    /**
     * 加号点击事件监听
     */
    private void listener_addMenu() {
        addMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(userId == null){
                    Toast.makeText(MainActivity.this,"请先登录！",Toast.LENGTH_SHORT).show();
                }else {
                    View root =MainActivity.this.getLayoutInflater().inflate(R.layout.popup_menu,null);
                    final PopupWindow popup = new PopupWindow(root,WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
                    popup.setFocusable(true);
                    popup.setOutsideTouchable(true);
                    view.setFocusableInTouchMode(true);
                    popup.setBackgroundDrawable(getResources().getDrawable(R.drawable.mypop));
                    popup.showAsDropDown(addMenu,-addMenu.getWidth(),30);
                    backgroundAlpha(0.7f);
                    //点击空白处，将透明度改回来
                    popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            backgroundAlpha(1f);
                            popup.dismiss();
                        }
                    });
                   // popup.setOnDismissListener(new poponDismissListener(popup));


                    addFriend = root.findViewById(R.id.add_friend);
                    addGroup = root.findViewById(R.id.add_group);
                    createGroup = root.findViewById(R.id.create_group);
                    addFriend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent_af = new Intent(MainActivity.this, AddFriendActivity.class);
                            startActivity(intent_af);
                            popup.dismiss();
                        }
                    });
                    addGroup.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(MainActivity.this, AddGroupActivity.class);
                            startActivity(intent);
                            popup.dismiss();
                        }
                    });
                    createGroup.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent_cg = new Intent(MainActivity.this, CreateGroupActivity.class);
                            startActivity(intent_cg);
                            popup.dismiss();
                        }
                    });
                }
            }
        });
    }

    /**
     * 潘攀控件初始化
     */
    private void init() {
        if(userId != null){
            initUserIconInMainAty();
        }
        slideMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.users_information:
                        if (userId == null){
                            Toast.makeText(MainActivity.this,"请先登录!",Toast.LENGTH_SHORT).show();
                            break;
                        }
                        intent = new Intent(MainActivity.this,ChangeUsersInfoActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.notify_message:
                        if (userId == null){
                            Toast.makeText(MainActivity.this,"请先登录!",Toast.LENGTH_SHORT).show();
                            break;
                        }
                        intent = new Intent(MainActivity.this, Notify.class);
                        startActivity(intent);
                        break;
                    case R.id.check_update:
                        intent = new Intent(MainActivity.this, GetVersionActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.about_us:
                        intent = new Intent(MainActivity.this, AboutUsActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.reflect:
                        intent = new Intent(MainActivity.this, ReflectActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.change_account:
                        intent = new Intent(MainActivity.this, LoginActivity.class);
                        if (userId != null){
                            EMClient.getInstance().logout(true);
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
                            editor.clear();
                            editor.apply();
                            VisitService.alarmManager.cancel(VisitService.pi);
                        }

                        startActivity(intent);
                        finish();
                        break;
                }
                return true;
            }
        });
        headIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userId == null){
                    Toast.makeText(MainActivity.this,"请先登录!",Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent(MainActivity.this, UserIconActivity.class);
                    startActivity(intent);
                }

            }
        });


        setting.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (null == userId){
                    Toast.makeText(MainActivity.this,"请先登录！",Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        weather.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,WeatherMainActivity.class);
                startActivity(intent);
            }
        });

        exit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (userId!= null){
                    EMClient.getInstance().logout(true);
                }
                finish();
            }
        });
    }

    /**
     * 初始化头像
     */
    private void initUserIconInMainAty() {

        List<UsersInfo> nickOnMain = DataSupport.select("iconUrl").where("account=?",userAccount).find(UsersInfo.class);
        String myIcon = null;
        if (nickOnMain.size() > 0) {
            for (UsersInfo usersInfo : nickOnMain) {
                myIcon = usersInfo.getIconUrl();
            }
            Glide.with(this).load(HttpUtil.SPS_SOURCE_URL + myIcon).into(slideMenuButton);
        }else{
            queryFromServiceOfIcon(userId);
        }
    }

    /**
     * 查询用户的头像
     */
    private void queryFromServiceOfIcon(String userId) {
        String url=Constant.SPS_URL+"GetUsersInforServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("id",userId)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody,new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this, "查询失败", Toast.LENGTH_SHORT).show();
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
                                initUserIconInMainAty();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (userId != null){
            initUserInfo();
            initUserIconInMainAty();
        }
    }

    /**
     * 初始化用户昵称和头像
     */
    private void initUserInfo() {
        List<UsersInfo> nickAndIcon = DataSupport.select("nickName", "iconUrl").where("account=?", userAccount).find(UsersInfo.class);
        String myNick = null;
        String myIcon = null;
        if(nickAndIcon.size()>0){
            for(UsersInfo usersInfo: nickAndIcon){
                myNick=usersInfo.getNickName();
                myIcon=usersInfo.getIconUrl();
            }
            Glide.with(this).load(HttpUtil.SPS_SOURCE_URL+myIcon).into(headIcon);
            if(myNick.equals("null")){
                userNickNameView.setText("");
            }else{
                userNickNameView.setText(myNick);
            }
        } else{
            queryFromServiceOfIconAndNick(userId);
        }
    }

    /**
     * 从服务器查找信息
     */
    public void queryFromServiceOfIconAndNick(String userId) {
        String url=Constant.SPS_URL+"GetUsersInforServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("id",userId)
                .build();

        HttpUtil.sendOkHttpRequest(url,requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this, "查询失败", Toast.LENGTH_SHORT).show();
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
                                initUserInfo();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 调整状态栏的透明度
     */
    private void adjustStatusBar() {
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * 初始滑动控件
     */
    private void initEvents() {
       mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
           @Override
           public void onTabSelected(TabLayout.Tab tab) {
               if (tab == mTabLayout.getTabAt(0)){
                   tab.setIcon(R.drawable.dance_bar_choose);
                   mViewPager.setCurrentItem(0);
               }else if(tab == mTabLayout.getTabAt(1)){
                   tab.setIcon(R.drawable.video_choose);
                   mViewPager.setCurrentItem(1);
               }else if (tab == mTabLayout.getTabAt(2)){
                   tab.setIcon(R.drawable.activity_choose);
                   mViewPager.setCurrentItem(2);
               }
           }

           @Override
           public void onTabUnselected(TabLayout.Tab tab) {
               if (tab == mTabLayout.getTabAt(0)){
                   tab.setIcon(R.drawable.dance_bar_unchoose);
               }else if(tab == mTabLayout.getTabAt(1)){
                   tab.setIcon(R.drawable.video_unchoose);
               }else if (tab == mTabLayout.getTabAt(2)){
                   tab.setIcon(R.drawable.activity_unchoose);
               }
           }

           @Override
           public void onTabReselected(TabLayout.Tab tab) {

           }
       });

        listener_addMenu();
    }

    /**
     * 初始布局控件
     */
    private void initViews() {
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.container);

        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()){

            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public Fragment getItem(int position) {
                if (position == 1){
                    return new VideoFragment();
                }else if (position == 2){
                    return new ClothesFragment();
                }
                return new DanceBarFragment();
            }
        });

        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(1);

        TabLayout.Tab videoTab = mTabLayout.getTabAt(1);
        TabLayout.Tab danceBarTab = mTabLayout.getTabAt(0);
        TabLayout.Tab competitionTab = mTabLayout.getTabAt(2);

        if (videoTab != null && danceBarTab != null && competitionTab != null){
            videoTab.setIcon(R.drawable.video_choose);
            danceBarTab.setIcon(R.drawable.dance_bar_unchoose);
            competitionTab.setIcon(R.drawable.activity_unchoose);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView=(NavigationView)findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        headIcon = headerView.findViewById(R.id.head_icon);

        slideMenuButton = (CircleImageView) findViewById(R.id.main_head_icon);

        userNickNameView = headerView.findViewById(R.id.nick_name);
        setting=(LinearLayout) findViewById(R.id.setting);
        weather=(LinearLayout) findViewById(R.id.weather);
        exit=(LinearLayout) findViewById(R.id.back);

        addMenu = (ImageView)findViewById(R.id.add_menu);

    }

    /**
     * 设置添加屏幕的背景透明度
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha){
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha;
        getWindow().setAttributes(lp);
    }
}
