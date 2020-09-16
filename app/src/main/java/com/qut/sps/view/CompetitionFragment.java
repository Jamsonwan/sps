package com.qut.sps.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.adapter.CompetitionAdapter;
import com.qut.sps.aty.CompetitionCreatActivity;
import com.qut.sps.util.Constant;
import com.qut.sps.util.HttpUtil;

import org.json.JSONArray;
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


public class CompetitionFragment extends Fragment {

    private List<Map<String,String>> competitionList = new ArrayList<>();

    private CompetitionAdapter adapter;

    private SwipeRefreshLayout swipeRefresh;

    private View view;

    private ImageView Creat_imageView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initCompetition();
        view = inflater.inflate(R.layout.fragment_competition, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Creat_imageView = view.findViewById(R.id.creat_imageView);
        if (MainActivity.userId == null){
            Creat_imageView.setVisibility(View.GONE);
        }
        Creat_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), CompetitionCreatActivity.class);
                startActivity(intent);
            }
        });
    }

    private void init(){
        RecyclerView recyclerView = view.findViewById(R.id.competition_recycler);
        if (competitionList.size()!=0) {
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 1);
            recyclerView.setLayoutManager(layoutManager);
            adapter = new CompetitionAdapter(competitionList);
            recyclerView.setAdapter(adapter);
            swipeRefresh = view.findViewById(R.id.swipe_refresh_com);
            swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
            swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    competitionList.clear();
                    initCompetition();
                    adapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                }
            });
        }else {
            Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
        }

    }
    private void initCompetition(){
        final RequestBody requestBody = new FormBody.Builder().build();
        HttpUtil.sendOkHttpRequest(Constant.SPS_URL+"CompetitionServlet", requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),"服务器请求失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Message msg = new Message();
                msg.what = 1;
                msg.obj = result;
                handler.sendMessage(msg);
            }
        });
    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    String message = (String) msg.obj;
                    JSONArray jsonArray;
                    try {
                        jsonArray = new JSONArray(message);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Map<String,String> map = new HashMap<>();
                                    map.put("name",jsonObject.getString("competitionname"));
                                    map.put("userId",jsonObject.getString("userid"));
                                    map.put("startTime",jsonObject.getString("starttime"));
                                    map.put("endTime",jsonObject.getString("endtime"));
                                    map.put("place",jsonObject.getString("place"));
                                    map.put("tel",jsonObject.getString("tel"));
                                    map.put("description",jsonObject.getString("description"));
                                    map.put("imageUrl",jsonObject.getString("imageurl"));
                                    competitionList.add(map);
                            }
                        init();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };
}
