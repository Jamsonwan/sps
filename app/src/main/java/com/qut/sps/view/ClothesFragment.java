package com.qut.sps.view;

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
import android.widget.Toast;

import com.qut.sps.R;
import com.qut.sps.adapter.ClothesAdapter;
import com.qut.sps.db.Costume;
import com.qut.sps.util.Constant;
import com.qut.sps.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *
 */
public class ClothesFragment extends Fragment {

    private List<Costume> list = new ArrayList<>();

    private ClothesAdapter adapter;

    private SwipeRefreshLayout swipeRefresh;

    private View view;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LitePal.getDatabase();
        initClothes();
        view = inflater.inflate(R.layout.fragment_clothes, container, false);
        return view;
    }

    private void init(){
        RecyclerView recyclerView = view.findViewById(R.id.clother_recycler);
        list = DataSupport.findAll(Costume.class);
        if (list.size()!=0) {
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
            recyclerView.setLayoutManager(layoutManager);
            adapter = new ClothesAdapter(list);
            recyclerView.setAdapter(adapter);
            swipeRefresh = view.findViewById(R.id.swipe_refresh);
            swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
            swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    swipeRefresh.setRefreshing(false);
                                    Toast.makeText(getContext(),"暂无数据更新!",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();
                }
            });
        }else {
            Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
        }

    }


    private void initClothes() {
        final RequestBody requestBody = new FormBody.Builder()
                .build();
        HttpUtil.sendOkHttpRequest(Constant.SPS_URL+"CostumeServlet", requestBody, new Callback() {
            @Override
            public void onFailure(final Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "服务器请求失败", Toast.LENGTH_SHORT).show();
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

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String message = (String) msg.obj;
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(message);
                        List<Costume> costumeList = DataSupport.findAll(Costume.class);
                        if (jsonArray.length() != costumeList.size() || costumeList.isEmpty()) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                try {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Costume costume = new Costume();
                                    costume.setId(Integer.parseInt(jsonObject.getString("id")));
                                    costume.setImageUrl(jsonObject.getString("imageurl"));
                                    costume.setImageName(jsonObject.getString("imagename"));
                                    costume.setTel(jsonObject.getString("tel"));
                                    costume.setDescription(jsonObject.getString("description"));
                                    costume.save();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }else {
                            init();
                        }
                        init();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
           }
        }
    };
}
