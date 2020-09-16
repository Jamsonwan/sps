package com.qut.sps.aty;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.qut.sps.R;
import com.qut.sps.adapter.NormalVideoAdapter;
import com.qut.sps.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.qut.sps.util.MyApplication.getContext;


public class AllVideoActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private String imageUrl;

    private String videoName;

    private String id;

    private String requestAddress;            //服务器请求地址

    private String type;                       //舞种类别

    private NormalVideoAdapter adapter;

    private List<Map<String,String>> videoList = new ArrayList<>();

    private SwipeRefreshLayout swipeRefresh;

    private boolean isRefresh = false;             //判断是否为刷新图片

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_video);
        initView();
    }
    /**
     * 初始化各控件
     */
    private void initView(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#9A3402"));
        initImage();
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(android.R.color.holo_orange_light);
        swipeRefresh.setProgressViewOffset(true,200,500);               //改变下拉刷新的具体位置和高度
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                initImage();
            }
        });
    }

    /**
     *发出初始化不同类别图片的信号
     */
    private void initImage(){
        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        if(type.equals("lala")){
            toolbar.setTitle("精选");
            String signal = "lala_image";
            setAllImage(signal);
        }else if(type.equals("gcw")){
            toolbar.setTitle("热门");
            String signal = "gcw_image";
            setAllImage(signal);
        }else if(type.equals("yoga")){
            toolbar.setTitle("推荐");
            String signal = "yoga_image";
            setAllImage(signal);
        }
    }

    /**
     * 初始化每一类别的全部图片
     * @param signal
     */
    private void setAllImage(String signal){
            final RequestBody requestBody = new FormBody.Builder()
                    .add("signal",signal)
                    .build();
            requestAddress = HttpUtil.SPS_URL+"ImageServlet";
            HttpUtil.sendOkHttpRequest(requestAddress,requestBody,new Callback(){
                @Override
                public void onFailure(Call call, IOException e){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
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
                            handleMessageForAll(responseText);
                        }
                    });
                }
            });
    }

    /**
     * 为请求所有图片处理服务器返回的结果
     * @param responseData
     */
    private void handleMessageForAll(String responseData){
        if(!TextUtils.isEmpty(responseData)){
            try{
                JSONArray jsonArray = new JSONArray(responseData);
                Map<String,String> map;
                for(int i = 0;i<jsonArray.length();i++){
                    map = new HashMap<>();
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    imageUrl = HttpUtil.SPS_SOURCE_URL+jsonObject.getString("imageUrl");
                    videoName = jsonObject.getString("videoName");
                    id = jsonObject.getString("id");
                    map.put("imageUrl",imageUrl);
                    map.put("videoName",videoName);
                    map.put("id",id);
                    videoList.add(map);
                }
                if(isRefresh){
                    adapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                }
                if(!isRefresh){
                    initRecyclerView();
                }
                isRefresh = true;
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }
    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView(){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(AllVideoActivity.this, 2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new NormalVideoAdapter(videoList);
        recyclerView.setAdapter(adapter);

    }

    /**
     * 设置按钮点击事件
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
