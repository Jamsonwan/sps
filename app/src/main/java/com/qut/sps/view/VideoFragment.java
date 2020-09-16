package com.qut.sps.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.qut.sps.R;
import com.qut.sps.adapter.CardVideoAdapter;
import com.qut.sps.aty.AllVideoActivity;
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

/**
 * 教学视频主界面即展示界面
 */

public class VideoFragment extends Fragment implements View.OnClickListener {

    private LinearLayout showLinear;

    private LinearLayout linearBtn;

    private LinearLayout linearBtn1;

    private LinearLayout linearBtn2;

    private final String type = "type";

    private String imageUrl;

    private String videoName;

    private String id;

    private String requestAddress;            //服务器请求地址

    private CardVideoAdapter adapter1;
    private CardVideoAdapter adapter2;
    private CardVideoAdapter adapter3;
    private CardVideoAdapter adapter4;


    private List<Map<String,String>> videoList = new ArrayList<>();         //存放要显示的啦啦操视频数据以及大海报

    private List<Map<String,String>> videoList1 = new ArrayList<>();        //存放要显示的爵士视频数据

    private List<Map<String,String>> videoList2 = new ArrayList<>();        //存放要显示的广场舞视频数据

    private List<Map<String,String>> videoList3 = new ArrayList<>();        //存放要显示的瑜伽视频数据

    private List<Map<String,String>> videoList4 = new ArrayList<>();         //存放要显示的大海报

    private SwipeRefreshLayout swipeRefresh;

    private boolean isRefresh = false;             //判断是否为第1、3、....次刷新

    private boolean isShow = true;                 //判断是请求刷新数据(图片)还是展示数据(图片)

    private  View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.video_show,container,false);
        initView();
        initEvent();
        setShowImage();
        return view;
    }

    /**
     * 初始化各控件
     */
    private void initView(){
        showLinear = view.findViewById(R.id.show_linear);
        linearBtn = view.findViewById(R.id.linear_btn);
        linearBtn1 = view.findViewById(R.id.linear_btn1);
        linearBtn2 = view.findViewById(R.id.linear_btn2);

        swipeRefresh =  view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(android.R.color.holo_orange_light);       //设置刷新时动画的颜色，可以设置4个
        swipeRefresh.setProgressViewOffset(true,200,500);               //改变下拉刷新的具体位置和高度
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                showLinear.setVisibility(View.VISIBLE);
                setRefreshImage();
                setShowImage();
            }
        });
    }

    /**
     * 向服务器请求最新的刷新图片
     */
    private void setRefreshImage(){
                final RequestBody requestBody = new FormBody.Builder()
                        .add("signal","refresh_image")
                        .build();
                requestAddress = HttpUtil.SPS_URL+"RefreshServlet";
                HttpUtil.sendOkHttpRequest(requestAddress,requestBody,new Callback(){
                    @Override
                    public void onFailure(Call call, IOException e){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(getContext(),"网络请求失败"
                                        ,Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER,0,0);
                                toast.show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response)throws IOException {
                        final String responseText = response.body().string();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                videoList.clear();
                                handleMessageForRefresh(responseText);
                            }
                        });
                    }
                });
    }

    /**
     * 为刷新请求处理服务器返回结果
     * @param responseData
     */
    private void  handleMessageForRefresh(String responseData){
        if(!TextUtils.isEmpty(responseData)){
            try{
                if(!isRefresh){
                    JSONArray jsonArray = new JSONArray(responseData);
                    Map<String,String> map ;
                    map = new HashMap<>();
                    //存放要显示的啦啦操视频数据
                    JSONObject jsonObject = jsonArray.getJSONObject(2);
                    imageUrl = HttpUtil.SPS_SOURCE_URL + jsonObject.getString("imageUrl");
                    videoName = jsonObject.getString("videoName");
                    id = jsonObject.getString("id");
                    map.put("imageUrl", imageUrl);
                    map.put("videoName", videoName);
                    map.put("id", id);
                    videoList.add(map);

                    map = new HashMap<>();
                    jsonObject = jsonArray.getJSONObject(3);
                    imageUrl = HttpUtil.SPS_SOURCE_URL + jsonObject.getString("imageUrl");
                    videoName = jsonObject.getString("videoName");
                    id = jsonObject.getString("id");
                    map.put("imageUrl", imageUrl);
                    map.put("videoName", videoName);
                    map.put("id", id);
                    videoList.add(map);

                    map = new HashMap<>();
                    jsonObject = jsonArray.getJSONObject(1);
                    imageUrl = HttpUtil.SPS_SOURCE_URL + jsonObject.getString("imageUrl");
                    videoName = jsonObject.getString("videoName");
                    id = jsonObject.getString("id");
                    map.put("imageUrl", imageUrl);
                    map.put("videoName", videoName);
                    map.put("id", id);
                    videoList.add(map);


                    map = new HashMap<>();
                    jsonObject = jsonArray.getJSONObject(0);
                    imageUrl = HttpUtil.SPS_SOURCE_URL + jsonObject.getString("imageUrl");
                    videoName = jsonObject.getString("videoName");
                    id = jsonObject.getString("id");
                    map.put("imageUrl", imageUrl);
                    map.put("videoName", videoName);
                    map.put("id", id);
                    videoList.add(map);

                    adapter1.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                    isRefresh = true;
                }else {
                    JSONArray jsonArray = new JSONArray(responseData);
                    Map<String,String> map;
                    for(int i = 0;i<4;i++){            //存放要显示的啦啦操视频数据
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
                    adapter1.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                    isRefresh = false;
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 向服务器请求界面要显示的图片
     */
    private void setShowImage(){
                final RequestBody requestBody = new FormBody.Builder()
                        .add("signal","show_image")
                        .build();
                requestAddress = HttpUtil.SPS_URL+"ImageServlet";
                HttpUtil.sendOkHttpRequest(requestAddress,requestBody,new Callback(){
                    @Override
                    public void onFailure(Call call, IOException e){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showLinear.setVisibility(View.INVISIBLE);
                                Toast toast = Toast.makeText(getContext(),"当前网络不佳，请刷新重试"
                                        ,Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER,0,0);
                                toast.show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response)throws IOException {
                        final String responseText = response.body().string();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                videoList1.clear();
                                videoList2.clear();
                                videoList3.clear();
                                videoList4.clear();
                                handleMessageForShow(responseText);
                            }
                        });
                    }
                });
    }

    /**
     * 为显示图片请求处理返回结果
     * @param responseData
     */
    private void handleMessageForShow(String responseData){
        if(!TextUtils.isEmpty(responseData)){
            try{
                JSONArray jsonArray = new JSONArray(responseData);
                Map<String,String> map;
                if(isShow){
                    for(int i = 0;i<4;i++){            //存放要显示的啦啦操视频数据
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
                }
                for(int i = 4;i<6;i++){           //存放要显示的瑜伽视频数据
                    map = new HashMap<>();
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    imageUrl = HttpUtil.SPS_SOURCE_URL+jsonObject.getString("imageUrl");
                    videoName = jsonObject.getString("videoName");
                    id = jsonObject.getString("id");
                    map.put("imageUrl",imageUrl);
                    map.put("videoName",videoName);
                    map.put("id",id);
                    videoList3.add(map);
                }
                for(int i = 6;i<10;i++){           //存放要显示的广场舞视频数据
                    map = new HashMap<>();
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    imageUrl = HttpUtil.SPS_SOURCE_URL+jsonObject.getString("imageUrl");
                    videoName = jsonObject.getString("videoName");
                    id = jsonObject.getString("id");
                    map.put("imageUrl",imageUrl);
                    map.put("videoName",videoName);
                    map.put("id",id);
                    videoList2.add(map);
                }
                for(int i = 10;i<13;i++){       //存放要显示的爵士视频数据
                    map = new HashMap<>();
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    imageUrl = HttpUtil.SPS_SOURCE_URL+jsonObject.getString("imageUrl");
                    videoName = jsonObject.getString("videoName");
                    id = jsonObject.getString("id");
                    map.put("imageUrl",imageUrl);
                    map.put("videoName",videoName);
                    map.put("id",id);
                    videoList1.add(map);
                }
                for(int i = 13;i<14;i++){       //存放要显示的大海报数据
                    map = new HashMap<>();
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    imageUrl = HttpUtil.SPS_SOURCE_URL+jsonObject.getString("imageUrl");
                    videoName = jsonObject.getString("videoName");
                    id = jsonObject.getString("id");
                    map.put("imageUrl",imageUrl);
                    map.put("videoName",videoName);
                    map.put("id",id);
                    videoList4.add(map);
                }
                if(isShow){
                    initRecyclerView();
                }
                if(!isShow){
                    adapter2.notifyDataSetChanged();
                    adapter3.notifyDataSetChanged();
                    adapter4.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                }
                isShow = false;
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化各RecyclerView
     */
    private void initRecyclerView(){

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(),2);
        recyclerView.setLayoutManager(layoutManager);
        adapter1 = new CardVideoAdapter(videoList);
        recyclerView.setAdapter(adapter1);
        recyclerView.setNestedScrollingEnabled(false);

        RecyclerView recyclerView1 = view.findViewById(R.id.recycler_view1);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getActivity());
        recyclerView1.setLayoutManager(layoutManager1);
        adapter2 = new CardVideoAdapter(videoList1);
        recyclerView1.setAdapter(adapter2);
        recyclerView1.setNestedScrollingEnabled(false);

        RecyclerView recyclerView2 = view.findViewById(R.id.recycler_view2);
        GridLayoutManager layoutManager2 = new GridLayoutManager(getActivity(),2);
        recyclerView2.setLayoutManager(layoutManager2);
        adapter3 = new CardVideoAdapter(videoList2);
        recyclerView2.setAdapter(adapter3);
        recyclerView2.setNestedScrollingEnabled(false);


        RecyclerView recyclerView3 = view.findViewById(R.id.recycler_view3);
        GridLayoutManager layoutManager3 = new GridLayoutManager(getActivity(),2);
        recyclerView3.setLayoutManager(layoutManager3);
        adapter4 = new CardVideoAdapter(videoList3);
        recyclerView3.setAdapter(adapter4);
        recyclerView3.setNestedScrollingEnabled(false);

    }

    /**
     * 初始化各点击事件
     */
    private void initEvent(){
        linearBtn.setOnClickListener(VideoFragment.this);
        linearBtn1.setOnClickListener(VideoFragment.this);
        linearBtn2.setOnClickListener(VideoFragment.this);
    }

    /**
     * 设置具体的点击事件
     * @param view
     */

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.linear_btn:
                String data = "lala";
                Intent intent = new Intent(getActivity(), AllVideoActivity.class);
                intent.putExtra(type, data);
                startActivity(intent);
                break;
            case R.id.linear_btn2:
                data = "gcw";
                intent = new Intent(getActivity(), AllVideoActivity.class);
                intent.putExtra(type, data);
                startActivity(intent);
                break;
            case R.id.linear_btn1:
                data = "yoga";
                intent = new Intent(getActivity(), AllVideoActivity.class);
                intent.putExtra(type, data);
                startActivity(intent);
                break;
            default:break;
        }
    }
}
