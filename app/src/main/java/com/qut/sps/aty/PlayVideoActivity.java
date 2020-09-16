package com.qut.sps.aty;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.adapter.NormalVideoAdapter;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.util.MyApplication;
import com.qut.sps.util.ScreenUtil;
import com.qut.sps.util.WifiUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.RequestBody;
import okhttp3.Response;


public class PlayVideoActivity extends AppCompatActivity implements View.OnClickListener{

    private SurfaceView surfaceview;
    private SurfaceHolder holder;
    private MediaPlayer mediaPlayer;
    private LinearLayout topLayout;
    private LinearLayout bottomLayout;
    private ImageButton play;
    private TextView tvCurrent;
    private TextView tvProgress;
    private ImageButton back;
    private ImageButton full;
    private SeekBar seekBar;
    private LinearLayout showLinear;
    private Button collect;
    private TextView videoname;
    private TextView jianjie;
    private TextView description;
    private TextView near;

    private NormalVideoAdapter adapter;

    private List<Map<String,String>> videoList = new ArrayList<>();

    private String requestAddress;                //服务器请求地址

    public static String videoUrl;

    public static String Description;

    public static String videoId;

    public static String videoName;

    private String imageUrl;

    private String nearVideoName;

    private String nearVideoId;

    private Intent intent;

    private boolean isClicked = false;          //判断播放按钮是否点击过

    private boolean isCollect = false;           //判断是否为收藏视频的请求，以便返回数据的处理

    private String date;                       //上一个活动转递过来的数据

    private boolean isPlaying = false;       //设置一个变量，判断当前是否在播放：防止退出应用后 handler还在发送消息。最后需要在onDestory()方法中配置

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");         //视频播放时间的显示方式 00:00

    private SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd");       //收藏时间的显示方式 2017-08-13

    private Handler mhandler = new Handler(){                 //接受消息并处理
        @Override
        public void handleMessage(Message msg) {
            if(isPlaying) {
                    tvCurrent.setText(simpleDateFormat.format(new Date(mediaPlayer.getCurrentPosition())));          //设置当前进度
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());     //更新seekbar
                    mhandler.sendEmptyMessageDelayed(0, 500);                 //每隔半秒再发送一条消息 这样seekbar就能实时更新
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_video);

        intent = getIntent();
        initVideo();
        initView();
        setListener();
    }
    /**
     * 初始化各控件
     */
    private void initView(){
        videoName = intent.getStringExtra("videoName");
        topLayout = (LinearLayout) findViewById(R.id.top);
        bottomLayout = (LinearLayout) findViewById(R.id.bottom);
        play = (ImageButton) findViewById(R.id.video_play);
        back = (ImageButton) findViewById(R.id.video_back);
        full = (ImageButton) findViewById(R.id.video_full);

        tvCurrent = (TextView) findViewById(R.id.tvCurrent);
        tvProgress = (TextView) findViewById(R.id.tvProgress);
        seekBar = (SeekBar) findViewById(R.id.seekbar);


        showLinear = (LinearLayout) findViewById(R.id.showLinear);
        collect = (Button) findViewById(R.id.collect);
        videoname = (TextView) findViewById(R.id.videoname_tv);
        videoname.setText(videoName);
        jianjie = (TextView) findViewById(R.id.jianjie_tv);
        description = (TextView) findViewById(R.id.description_tv);
        near = (TextView) findViewById(R.id.near);

        surfaceview = (SurfaceView) findViewById(R.id.surfaceView);
        holder = surfaceview.getHolder();
        holder.addCallback(new SurfaceViewLis());
        mediaPlayer = new MediaPlayer();

    }

    /**
     * 定义回调接口
     */
    private class SurfaceViewLis implements SurfaceHolder.Callback {
        //当SurfaceView的大小发生改变时候触发该方法
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {

        }

        //当SurfaceView中Surface画面创建时回调
        //该方法表示Surface已经创建完成，可以在该方法中进行绘图操作
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            holder.setKeepScreenOn(true);   //播放时屏幕保持唤醒
        }

        //Surface销毁时回调
        //当Surface销毁时候，同时把MediaPlayer也销毁
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // 销毁SurfaceHolder的时候停止播放
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }

        }

    }

    /**
     * 设置各监听器
     */
    private void setListener() {
        play.setOnClickListener(this);
        back.setOnClickListener(this);
        full.setOnClickListener(this);
        collect.setOnClickListener(this);

        surfaceview.setOnTouchListener(new View.OnTouchListener() {            //设置 surfaceView点击监听
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if(ScreenUtil.isScreenOrientationPortrait(PlayVideoActivity.this)&&
                                (topLayout.getVisibility() == View.VISIBLE)&&
                                (bottomLayout.getVisibility() ==View.VISIBLE)){
                            topLayout.setVisibility(View.INVISIBLE);
                            bottomLayout.setVisibility(View.INVISIBLE);
                        }else if(ScreenUtil.isScreenOrientationPortrait(PlayVideoActivity.this)){
                            topLayout.setVisibility(View.VISIBLE);
                            bottomLayout.setVisibility(View.VISIBLE);
                        }else if(!ScreenUtil.isScreenOrientationPortrait(PlayVideoActivity.this)){
                            if(topLayout.getVisibility() == View.VISIBLE){
                                topLayout.setVisibility(View.INVISIBLE);
                            }else{
                                topLayout.setVisibility(View.VISIBLE);
                            }
                        }
                        break;
                }
                return true;       //返回True代表事件已经处理了
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {       //给进度条设置滑动的监听，并设置拖动seekbar改变播放进度
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress=seekBar.getProgress();
                mediaPlayer.seekTo(progress);      //进度条在当前位置播放
            }
        });
    }

    /**
     * 设置具体的点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_play:
                if(!isClicked){
                    WifiUtil.handleWifi();
                    isClicked = true;
                }
                play = (ImageButton) findViewById(R.id.video_play);
                if(mediaPlayer != null&&!mediaPlayer.isPlaying()){
                        mediaPlayer.start();
                        video_play();
                        play.setBackgroundResource(R.drawable.ic_media_pause);
                }else if(mediaPlayer != null&&mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    play.setBackgroundResource(R.drawable.ic_media_play);
                }
                break;
            case R.id.video_full:
                full = (ImageButton) findViewById(R.id.video_full);
                if(ScreenUtil.isScreenOrientationPortrait(PlayVideoActivity.this)){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    getWindow().getDecorView().setSystemUiVisibility(View.INVISIBLE);
                    changeVideoSize();
                    showLinear.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.video_back:
                if(!ScreenUtil.isScreenOrientationPortrait(PlayVideoActivity.this)){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
                    restoreVideoSize();
                    showLinear.setVisibility(View.VISIBLE);
                }else{
                    mediaPlayer.pause();
                    finish();                       //要返回上一个activity，只需结束当前的activity
                }
                break;
            case R.id.collect:
                if (null == MainActivity.userId){
                    Toast.makeText(PlayVideoActivity.this,"请先登录！",Toast.LENGTH_SHORT).show();
                    break;
                }
                isCollect = true;
                collect = (Button) findViewById(R.id.collect);
                collect.setBackgroundResource(R.drawable.collection_choose);
                collect.setClickable(false);
                addCollection();
                break;
            default:
                break;
        }
    }

    /**
     * 开始播放视频
     */
    protected void video_play() {

        try {// 设置播放的视频源
            if(videoUrl!=null){
                mediaPlayer.setDataSource(videoUrl);
            }else{
                Toast.makeText(PlayVideoActivity.this, "videoUrl is null", Toast.LENGTH_SHORT).show();
            }
            mediaPlayer.setDisplay(holder);         // 设置显示视频的SurfaceHolder，这一步是关键，制定用于显示视频的SurfaceView对象（通过setDisplay））

            mediaPlayer.prepareAsync();             //准备异步播放视频

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();

                    isPlaying = true;

                    tvProgress.setText(simpleDateFormat.format(new Date(mediaPlayer.getDuration())));     //设置视频总长 videoView.getDuration()为毫秒数需要转换，注意getDuration()方法要在prepare()方法之后

                    seekBar.setMax(mediaPlayer.getDuration());        // 设置进度条的最大进度为视频流的最大播放时长

                    mhandler.sendEmptyMessage(0);            //创建Handler 发送一条 空消息 通知seekbar和2个TextView视频播放了，得开始更新进度条的刻度
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // 在播放完毕被回调

                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    return false;
                }
            });

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置全屏
     *设置SurfaceView的大小
     */
    private void changeVideoSize(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth  = dm.widthPixels;     // 屏幕宽
        int screenHeight = dm.heightPixels  ;   // 屏幕高
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(screenWidth,
                screenHeight);
        surfaceview.setLayoutParams(layoutParams);
        bottomLayout.setVisibility(View.INVISIBLE);
    }

    /**
     * 恢复窗口大小
     */
    private void restoreVideoSize(){
        int screenWidth  = getWindowManager().getDefaultDisplay().getWidth();     // 屏幕宽
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(screenWidth, 660);
        surfaceview.setLayoutParams(layoutParams);
    }

    /**
     * 添加收藏
     */
    private void addCollection(){
        videoId = intent.getStringExtra("id");
        date = simpleDateFormat1.format(new Date(System.currentTimeMillis()));
        final RequestBody requestBody = new okhttp3.FormBody.Builder()
                .add("signal","addcollection")
                .add("userId", MainActivity.userId)
                .add("videoId",videoId)
                .add("date",date)
                .build();
        String servlet = "CollectServlet";
        setVideoAndCollection(requestBody,servlet);
    }

    /**
     * 初始化视频数据
     */
    private void initVideo(){
        videoId = intent.getStringExtra("id");
        final RequestBody requestBody = new okhttp3.FormBody.Builder()
                .add("signal","video")
                .add("id",videoId)
                .build();
        String servlet = "VideoServlet";
        setVideoAndCollection(requestBody,servlet);
    }

    /**
     * 发起网络请求，向服务器请求资源
     * @param requestBody
     * @param servlet
     */
    private void setVideoAndCollection(RequestBody requestBody,String servlet){

                requestAddress = HttpUtil.SPS_URL+servlet;
                HttpUtil.sendOkHttpRequest(requestAddress,requestBody,new Callback(){
                    @Override
                    public void onFailure(Call call, IOException e){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MyApplication.getContext(),"网络加载失败",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response)throws IOException {
                        final String responseText = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                videoList.clear();
                                handleMessage(responseText);
                            }
                        });
                    }
                });
    }

    /**
     *判断请求类别，从而处理服务器返回的结果
     * @param responseData
     */
    private void handleMessage(String responseData){
        if(isCollect){
            handleMessageForAddCollection(responseData);
            isCollect = false;
        }else{
            handleMessageForVideo(responseData);
        }
    }

    /**
     *为视频请求处理返回结果
     * @param responseData
     */
    private void handleMessageForVideo(String responseData){
            if(!TextUtils.isEmpty(responseData)) {
                try{
                    JSONArray jsonArray = new JSONArray(responseData);
                    JSONObject object = jsonArray.getJSONObject(0);
                    videoUrl = HttpUtil.SPS_SOURCE_URL + object.getString("videoUrl");
                    Description = object.getString("description");
                    Map<String,String> map;
                    for(int i = 1;i<jsonArray.length();i++) {
                        map = new HashMap<>();
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        imageUrl = HttpUtil.SPS_SOURCE_URL + jsonObject.getString("imageUrl");
                        nearVideoName = jsonObject.getString("videoName");
                        nearVideoId = jsonObject.getString("id");
                        map.put("imageUrl", imageUrl);
                        map.put("videoName", nearVideoName);
                        map.put("id", nearVideoId);
                        videoList.add(map);
                    }
                    description.setText(Description);
                    initRecyclerView();
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
    }
    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView(){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(PlayVideoActivity.this, 2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new NormalVideoAdapter(videoList);
        recyclerView.setAdapter(adapter);
    }

    /**
     *为收藏请求处理返回结果
     * @param responseData
     */
    private void handleMessageForAddCollection(String responseData){
        if(!TextUtils.isEmpty(responseData)){
            try{
                JSONObject object = new JSONObject(responseData);
                final String result  = object.getString("result");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PlayVideoActivity.this,result,Toast.LENGTH_SHORT).show();
                    }
                });
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 为活动配置
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 销毁活动
     */
    @Override
    protected void onDestroy() {
        if(mediaPlayer != null){
            try{
                isPlaying = false;
                mediaPlayer.stop();
                mediaPlayer.release();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        super.onDestroy();

    }
}





