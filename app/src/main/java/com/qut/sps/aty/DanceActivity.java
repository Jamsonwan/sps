package com.qut.sps.aty;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qut.sps.R;
import com.qut.sps.adapter.MusicListAdapter;
import com.qut.sps.server.DanceService;
import com.qut.sps.server.JudeLeaderService;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.util.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DanceActivity extends AppCompatActivity {

    public final String PAUSE = "pause";
    public final String PLAY = "play";

    private Button titleBackBtn;
    private Button playMusicBtn;
    private Button pauseMusicBtn;
    private Button preMusicBtn;
    private Button nextMusicBtn;
    private Button musicListBtn;

    private SeekBar seekBar;

    private TextView playingMusicName;
    private TextView playMusicSinger;

    private CircleImageView musicImgView;

    private PopupWindow popupWindow;

    private List<Map<String,String>> musicList = new ArrayList<>();
    private String EMGroupId;
    private String userAccount;
    private String leaderAccount;
    private String currentMusicUrl;

    private int currentPosition;
    private int duration;

    private Animation animation;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Intent haveLeaderIntent;
    private Intent controlIntent;

    private SeekBar.OnSeekBarChangeListener sbListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mediaPlayer!= null){
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        }
    };

    private Runnable start = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null){
                mediaPlayer.start();
                handler.post(updateSeekBar);
            }
        }
    };
    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null){
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(updateSeekBar,1000);
            }
        }
    };

    private HaveLeaderReceiver haveLeaderReceiver;
    private StartAndControlReceiver controlReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_dance);

        EMGroupId = getIntent().getStringExtra("EMGroupId");
        userAccount = getIntent().getStringExtra("userAccount");
        leaderAccount = getIntent().getStringExtra("leaderAccount");

        if(!userAccount.equals(leaderAccount)){
            haveLeaderIntent = new Intent(this,JudeLeaderService.class);
            haveLeaderIntent.putExtra("EMGroupId",EMGroupId);
            startService(haveLeaderIntent);

            controlIntent = new Intent(this,DanceService.class);
            controlIntent.putExtra("EMGroupId",EMGroupId);
            controlIntent.putExtra("leaderAccount",leaderAccount);
            startService(controlIntent);
        }


        initView();
        final ProgressDialog progressDialog = new ProgressDialog(DanceActivity.this);
        progressDialog.setTitle("正在加载");
        progressDialog.setMessage("请稍后...");
        progressDialog.setCancelable(false);

        loadMusicListFromServer();
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        initEvent();
                    }
                });
            }
        }).start();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.qut.sps.LOCAL_BROADCAST");
        haveLeaderReceiver = new HaveLeaderReceiver();
        registerReceiver(haveLeaderReceiver,intentFilter);

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.qut.sps.DANCE_BROADCAST");
        controlReceiver = new StartAndControlReceiver();
        registerReceiver(controlReceiver,filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null){
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }


        if (haveLeaderReceiver != null){
            unregisterReceiver(haveLeaderReceiver);
            haveLeaderReceiver = null;
        }
        if (controlReceiver != null){
            unregisterReceiver(controlReceiver);
            controlReceiver = null;
        }
        if (!userAccount.equals(leaderAccount)){
            JudeLeaderService.manager.cancel(JudeLeaderService.pendingIntent);
            DanceService.manager.cancel(DanceService.pendingIntent);
            stopService(haveLeaderIntent);
            stopService(controlIntent);
        }
    }

    /**
     * 用于判断领舞者是否退出了本次舞会
     */
    class HaveLeaderReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean haveLeader = intent.getBooleanExtra("haveLeader",false);
            if (!haveLeader){
                Toast.makeText(MyApplication.getContext(),"本次舞会已经结束",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * 监听领舞者发来的监控
     */
    class StartAndControlReceiver extends BroadcastReceiver{

        private int flag = 0;
        private boolean change = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean state = intent.getBooleanExtra("start",false);
            String musicUrl = intent.getStringExtra("musicUrl");
            String control = intent.getStringExtra("control");
            String musicName = intent.getStringExtra("musicName");
            String singer = intent.getStringExtra("singer");
            musicUrl = HttpUtil.SPS_SOURCE_URL + "music/"+musicUrl;
            if (state){
                flag ++;
                if (flag > 0){
                    if (musicUrl.equals(currentMusicUrl)){
                        if (PAUSE.equals(control)){
                            if(!change){
                                change = true;
                            }
                            mediaPlayer.pause();
                            playMusicBtn.setVisibility(View.VISIBLE);
                            pauseMusicBtn.setVisibility(View.INVISIBLE);
                            musicImgView.clearAnimation();
                        }else {
                            if (change){
                                change = false;
                                handler.post(start);
                                playMusicBtn.setVisibility(View.INVISIBLE);
                                pauseMusicBtn.setVisibility(View.VISIBLE);
                                musicImgView.startAnimation(animation);
                            }
                        }
                    }else {
                        currentMusicUrl = musicUrl;
                        userChangeMusic(musicUrl,musicName,singer);
                    }
                }else {
                    currentMusicUrl = musicUrl;
                    userChangeMusic(musicUrl,musicName,singer);
                }
            }
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            if(leaderAccount.equals(userAccount)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DanceActivity.this);
                builder.setCancelable(false);
                builder.setTitle("警告");
                builder.setMessage("退出将取消本次舞会！");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendCancelDance(EMGroupId);
                        finish();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.show();
            }else{
                AlertDialog.Builder dialog = new AlertDialog.Builder(DanceActivity.this);
                dialog.setCancelable(false);
                dialog.setTitle("提醒");
                dialog.setMessage("确定退出本次舞会！");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendExitDance(userAccount);
                        finish();
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dialog.show();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 用户收到更换音乐
     * @param musicUrl
     * @param musicName
     * @param singer
     */
    private void userChangeMusic(String musicUrl, final String musicName, final String singer){
        if (mediaPlayer != null){
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = getMediaPlayer(this);
        try {
            mediaPlayer.setDataSource(this,Uri.parse(musicUrl));
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("参与者播放失败",e.getMessage());
            Toast.makeText(this,"播放音乐失败",Toast.LENGTH_SHORT).show();
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                handler.post(start);
                duration = mediaPlayer.getDuration();
                seekBar.setMax(duration);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playMusicBtn.setVisibility(View.INVISIBLE);
                        pauseMusicBtn.setVisibility(View.VISIBLE);

                        musicImgView.setVisibility(View.VISIBLE);
                        musicImgView.startAnimation(animation);
                    }
                });
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                playMusicBtn.setVisibility(View.VISIBLE);
                pauseMusicBtn.setVisibility(View.INVISIBLE);
                musicImgView.clearAnimation();
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playingMusicName.setText(musicName);
                playMusicSinger.setText(singer);
            }
        });
    }
    /**
     * 初始化部分事件
     */
    private void initEvent() {
        if(userAccount.equals(leaderAccount)){
            showPopupWindow();
        }
        seekBar.setOnSeekBarChangeListener(sbListener);

        titleBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(leaderAccount.equals(userAccount)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DanceActivity.this);
                    builder.setCancelable(false);
                    builder.setTitle("警告");
                    builder.setMessage("退出将取消本次舞会！");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sendCancelDance(EMGroupId);
                            finish();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    builder.show();
                }else{
                    AlertDialog.Builder dialog = new AlertDialog.Builder(DanceActivity.this);
                    dialog.setCancelable(false);
                    dialog.setTitle("提醒");
                    dialog.setMessage("确定退出本次舞会！");
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sendExitDance(userAccount);
                            finish();
                        }
                    });
                    dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    dialog.show();
                }
            }
        });

        playMusicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userAccount.equals(leaderAccount)){
                    if (mediaPlayer != null){
                        musicImgView.setVisibility(View.VISIBLE);
                        sendControl(EMGroupId,leaderAccount,PLAY);
                        handler.post(start);
                        if (animation != null){
                            musicImgView.startAnimation(animation);
                        }
                        playMusicBtn.setVisibility(View.INVISIBLE);
                        pauseMusicBtn.setVisibility(View.VISIBLE);
                    }else {
                        Toast.makeText(DanceActivity.this,"请先选择音乐！",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(DanceActivity.this,"请等待领舞者控制歌曲！",Toast.LENGTH_SHORT).show();
                }
            }
        });

        pauseMusicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userAccount.equals(leaderAccount)){
                    sendControl(EMGroupId,leaderAccount,PAUSE);
                    mediaPlayer.pause();
                    musicImgView.clearAnimation();
                    playMusicBtn.setVisibility(View.VISIBLE);
                    pauseMusicBtn.setVisibility(View.INVISIBLE);
                }else {
                    Toast.makeText(DanceActivity.this,"请等待领舞者控制歌曲！",Toast.LENGTH_SHORT).show();
                }
            }
        });

        musicListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupWindow();
            }
        });

        preMusicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              if (leaderAccount.equals(userAccount)){
                  if (mediaPlayer != null){
                      currentPosition = currentPosition - 1;
                      if (currentPosition < 0){
                          currentPosition = 58;
                      }
                      if (musicImgView.getAnimation() != null){
                          musicImgView.clearAnimation();
                      }
                      changeMusic(currentPosition);
                  }else {
                      Toast.makeText(DanceActivity.this,"请等待领舞者控制歌曲！",Toast.LENGTH_SHORT).show();
                  }

              }else {
                  Toast.makeText(DanceActivity.this,"只有领舞者才可以更换音乐！",Toast.LENGTH_SHORT).show();
              }
            }
        });

        nextMusicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userAccount.equals(leaderAccount)){
                    if (mediaPlayer != null){
                        currentPosition = currentPosition + 1;
                        if (currentPosition > 58){
                            currentPosition  = 0;
                        }
                        if (musicImgView.getAnimation() != null){
                            musicImgView.clearAnimation();
                        }
                        changeMusic(currentPosition);
                    }else {
                        Toast.makeText(DanceActivity.this,"请等待领舞者控制歌曲！",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(DanceActivity.this,"只有领舞者才可以更换音乐！",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 发送控信号
     * @param EMGroupId
     * @param leaderAccount
     * @param control
     */
    private void sendControl(String EMGroupId, String leaderAccount,String control) {
        String url = HttpUtil.SPS_URL + "ChangeControlServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("EMGroupId",EMGroupId)
                .add("control",control)
                .add("leaderAccount",leaderAccount)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DanceActivity.this,"请检查网络后重试！",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(!response.body().string().equals("OK")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DanceActivity.this,"服务器异常！",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 个人退出本次舞会
     * @param account
     */
    private void sendExitDance(String account) {
        String url = HttpUtil.SPS_URL + "ExitDanceServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("memberAccount",account)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyApplication.getContext(),"请检查网络后重试！",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.body().string().equals("OK")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MyApplication.getContext(),"退出舞会成功！",Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("ERROR5",5+"");
                            Toast.makeText(MyApplication.getContext(),"发生未知错误！",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 领舞者发出取消或者完成了本次舞会
     * @param EMGroupId
     */
    private void sendCancelDance(String EMGroupId) {
        String url = HttpUtil.SPS_URL + "CancelDanceServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("EMGroupId",EMGroupId)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyApplication.getContext(),"请检查网络后重试！",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body().string().equals("OK")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MyApplication.getContext(),"取消舞会成功！",Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("ERROR2",2+"");
                            Toast.makeText(MyApplication.getContext(),"发生未知错误！",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 初始化一些控件
     */
    private void initView() {
        titleBackBtn = (Button) findViewById(R.id.music_back);
        playingMusicName = (TextView) findViewById(R.id.playing_music_name);
        playMusicSinger = (TextView) findViewById(R.id.playing_music_singer);
        musicImgView = (CircleImageView) findViewById(R.id.music_img);
        musicImgView.setVisibility(View.GONE);

        playMusicBtn = (Button) findViewById(R.id.play_music_btn);
        pauseMusicBtn = (Button) findViewById(R.id.pause_music_btn);
        preMusicBtn = (Button) findViewById(R.id.pre_music);
        nextMusicBtn = (Button) findViewById(R.id.next_music);
        musicListBtn = (Button) findViewById(R.id.music_list_btn);

        seekBar = (SeekBar) findViewById(R.id.music_seek_bar);

        animation = AnimationUtils.loadAnimation(this,R.anim.tip);
        LinearInterpolator lin = new LinearInterpolator();
        animation.setInterpolator(lin);
    }

    /**
     * 从服务器加载音乐列表
     */
    private void loadMusicListFromServer() {
        String url = HttpUtil.SPS_URL + "MusicListServlet";
        final RequestBody requestBody = new FormBody.Builder()
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DanceActivity.this,"请检查网络后重试！",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                if (!TextUtils.isEmpty(responseData)){
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        Map<String,String> map;
                        for (int i = 0;i < jsonArray.length();i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            map = new HashMap<>();
                            map.put("musicName",jsonObject.getString("musicname"));
                            map.put("singerName",jsonObject.getString("singer"));
                            map.put("musicUrl",jsonObject.getString("musicurl"));
                            musicList.add(map);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    Toast.makeText(DanceActivity.this,"服务器维护中！请稍后重试！",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 弹出音乐列表
     */
    private void showPopupWindow(){
        View contentView = LayoutInflater.from(DanceActivity.this).inflate(R.layout.pop_music_list,null);
        popupWindow = new PopupWindow(contentView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,true);

        RecyclerView recyclerView = contentView.findViewById(R.id.music_list_recycler_view);
        Button popTitleBack = contentView.findViewById(R.id.pop_title_back);
        LinearLayoutManager manager =new  LinearLayoutManager(this);

        popTitleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        recyclerView.setLayoutManager(manager);
        final MusicListAdapter adapter = new MusicListAdapter(musicList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new MusicListAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(final int position) {
                if (userAccount.equals(leaderAccount)){
                    if (musicImgView.getAnimation() != null){
                        musicImgView.clearAnimation();
                    }
                    currentPosition = position;
                    changeMusic(position);
                    popupWindow.dismiss();
                }else {
                    Toast.makeText(DanceActivity.this,"只有领舞者才可以选择歌曲！",Toast.LENGTH_SHORT).show();
                }
            }
        });

        View rootView = LayoutInflater.from(DanceActivity.this).inflate(R.layout.activity_dance,null);
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM,0,0);
    }

    /**
     * 领舞者改变音乐
     * @param position
     */
    private void  changeMusic(int position){
        final String musicName = musicList.get(position).get("musicName");
        final String singerName = musicList.get(position).get("singerName");
        String musicUrl = musicList.get(position).get("musicUrl");

        sendMusicUrl(EMGroupId,leaderAccount,musicUrl,PLAY);
        String url = HttpUtil.SPS_SOURCE_URL + "music/"+musicUrl;

        if (mediaPlayer != null){
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer =getMediaPlayer(DanceActivity.this);
        try {
            mediaPlayer.setDataSource(this,Uri.parse(url));
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Toast.makeText(this,"加载音乐失败！",Toast.LENGTH_SHORT).show();
            Log.d("播放音乐失败！",e.getMessage());
            e.printStackTrace();
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                duration = mediaPlayer.getDuration();
                seekBar.setMax(duration);
                handler.post(start);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playMusicBtn.setVisibility(View.INVISIBLE);
                        pauseMusicBtn.setVisibility(View.VISIBLE);
                        musicImgView.setVisibility(View.VISIBLE);
                        musicImgView.startAnimation(animation);
                    }
                });
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                playMusicBtn.setVisibility(View.VISIBLE);
                pauseMusicBtn.setVisibility(View.INVISIBLE);
                musicImgView.clearAnimation();
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playingMusicName.setText(musicName);
                playMusicSinger.setText(singerName);
            }
        });
    }

    /**
     * 发送更换音乐
     * @param EMGroupId
     * @param leaderAccount
     * @param musicUrl
     * @param control
     */
    private void sendMusicUrl(String EMGroupId, String leaderAccount, String musicUrl, String control) {
        String url = HttpUtil.SPS_URL + "UpdateMusicServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("EMGroupId",EMGroupId)
                .add("leaderAccount",leaderAccount)
                .add("musicUrl",musicUrl)
                .add("control",control)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DanceActivity.this,"请检查网络后重试！",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(!response.body().string().equals("OK")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DanceActivity.this,"服务器维护中！",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 得到mediaPlayer
     * @param context
     * @return
     */
    private MediaPlayer getMediaPlayer(Context context){
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
            return  mediaPlayer;
        }
        try {
            Class<?> cMediaTimeProvider = Class.forName("android.media.MediaTimeProvider");
            Class<?> cSubtitleController = Class.forName("android.media.SubtitleController");
            Class<?> iSubtitleControllerAnchor = Class.forName("android.media.SubtitleController$Anchor");
            Class<?> iSubtitleControllerListener = Class.forName("android.media.SubtitleController$Listener");
            Constructor constructor = cSubtitleController.getConstructor(
                    Context.class, cMediaTimeProvider, iSubtitleControllerListener);
            Object subtitleInstance = constructor.newInstance(context, null, null);
            Field f = cSubtitleController.getDeclaredField("mHandler");
            f.setAccessible(true);
            try {
                f.set(subtitleInstance, new Handler());
            } catch (IllegalAccessException e) {
                return mediaPlayer;
            } finally {
                f.setAccessible(false);
            }
            Method setSubtitleAnchor = mediaPlayer.getClass().getMethod("setSubtitleAnchor",
                    cSubtitleController, iSubtitleControllerAnchor);
            setSubtitleAnchor.invoke(mediaPlayer, subtitleInstance, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaPlayer;
    }
    /**
     * 启动舞会活动
     * @param context
     * @param EMGroupId
     * @param userAccount
     * @param leaderAccount
     */
    public static void startDanceActivity(Context context,String EMGroupId,String userAccount,String leaderAccount){
        Intent intent = new Intent(context,DanceActivity.class);
        intent.putExtra("EMGroupId",EMGroupId);
        intent.putExtra("userAccount",userAccount);
        intent.putExtra("leaderAccount",leaderAccount);
        context.startActivity(intent);
    }
}
