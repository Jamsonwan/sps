package com.qut.sps.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.adapter.FriendsAndTeamAdapter;
import com.qut.sps.aty.LetDanceActivity;
import com.qut.sps.db.Groups;
import com.qut.sps.util.MyApplication;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by lenovo on 2017/8/18.
 */

public class LetDanceFragment extends Fragment {

    private View rootView;
    private  FriendsAndTeamAdapter adapter;
    private List<Map<String,String>> groupList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(rootView != null){
            return rootView;
        }
        rootView = inflater.inflate(R.layout.fragment_let_dance,container,false);
        initView();
        return rootView;
    }

    private void initView() {
        RecyclerView  recyclerView = rootView.findViewById(R.id.choose_group_recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.getContext());
        recyclerView.setLayoutManager(manager);
        if (MainActivity.userId != null){
            loadGroupList();
        }
        adapter = new FriendsAndTeamAdapter(groupList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new FriendsAndTeamAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position, String id) {
                LetDanceActivity.startLetDanceActivity(getContext(),MainActivity.userId,id);
            }
            @Override
            public void OnItemCheckClick(View view, int position, boolean isChecked) {

            }
        });
    }

    private void loadGroupList() {
        groupList.clear();
        List<Groups> groupsList = DataSupport.where("userId=?", MainActivity.userId).find(Groups.class);
        Map<String,String> map;
        for (Groups group:groupsList){
            map = new HashMap<>();
            map.put("iconUrl",group.getIconUrl());
            map.put("name",group.getName());
            map.put("id", group.getEMGroupId());
            groupList.add(map);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(MainActivity.userId != null){
            loadGroupList();
            adapter.notifyDataSetChanged();
        }
    }
}
