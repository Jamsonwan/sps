package com.qut.sps.aty;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.adapter.ViewPagerAdapter;
import com.qut.sps.util.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 程序主入口
 *
 *
 */
public class InitActivity extends BaseActivity implements View.OnClickListener{

    private ViewPager mViewPaper;

    private List<View> dots;

    private int currentItem;

    //记录上一次点的位置
    private int oldPosition = 0;

    //存放图片的id
    private int[] imageIds = new int[]{

            R.drawable.guide1,
            R.drawable.pic1,
            R.drawable.pic3

    };

    private ScheduledExecutorService scheduledExecutorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_guide);
        if (ContextCompat.checkSelfPermission(InitActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(InitActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        Button login_button = (Button)findViewById(R.id.login_button);
        Button register_button =(Button) findViewById(R.id.register_button);
        Button main_button =(Button) findViewById(R.id.main_button);
        login_button.setOnClickListener(this);
        register_button.setOnClickListener(this);
        main_button.setOnClickListener(this);
        mViewPaper = (ViewPager) findViewById(R.id.vp);
        SharedPreferences.Editor editor = getSharedPreferences("init",MODE_PRIVATE).edit();
        editor.putString("init","OK");
        editor.apply();

        //显示的图片
        List<ImageView> images = new ArrayList<ImageView>();
        for(int i = 0; i < imageIds.length; i++){
            ImageView imageView = new ImageView(this);
            imageView.setBackgroundResource(imageIds[i]);
            images.add(imageView);
        }
        //显示的小点
        dots = new ArrayList<View>();
        dots.add(findViewById(R.id.dot_0));
        dots.add(findViewById(R.id.dot_1));
        dots.add(findViewById(R.id.dot_2));


        ViewPagerAdapter adapter = new ViewPagerAdapter(images);
        mViewPaper.setAdapter(adapter);

        mViewPaper.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {


            @Override
            public void onPageSelected(int position) {
                dots.get(position).setBackgroundResource(R.drawable.dot_focused);
                dots.get(oldPosition).setBackgroundResource(R.drawable.dot_normal);

                oldPosition = position;
                currentItem = position;
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.login_button:
                Intent intent = new Intent(InitActivity.this,LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.register_button:
                Intent intent1 = new Intent(InitActivity.this,RegisterActivity.class);
                startActivity(intent1);
                break;
            case R.id.main_button:
                Intent intent2 = new Intent(InitActivity.this,MainActivity.class);
                startActivity(intent2);

                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * 利用线程池定时执行动画轮播
     */
    @Override
    protected void onStart() {
        super.onStart();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(
                new ViewPageTask(),
                5,
                5,
                TimeUnit.SECONDS);
    }


    /**
     * 图片轮播任务
     * @author
     *
     */
    private class ViewPageTask implements Runnable{

        @Override
        public void run() {
            currentItem = (currentItem + 1) % imageIds.length;
            mHandler.sendEmptyMessage(0);
        }
    }

    /**
     * 接收子线程传递过来的数据
     */
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            mViewPaper.setCurrentItem(currentItem);
        };
    };
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        if(scheduledExecutorService != null){
            scheduledExecutorService.shutdown();
            scheduledExecutorService = null;
        }
    }

}