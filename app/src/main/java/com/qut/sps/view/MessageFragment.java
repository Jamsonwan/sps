package com.qut.sps.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.adapter.MessageAdapter;
import com.qut.sps.aty.NewInfoActivity;
import com.qut.sps.db.NotifyMessage;
import com.qut.sps.util.Constant;
import com.qut.sps.util.MyApplication;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lenovo on 2017/8/4.
 */

public class MessageFragment extends Fragment{

    private View rootView;

    private List<Map<String,String >> mesageList = new ArrayList<>();
    private MessageAdapter messageAdapter;

    private LinearLayout newInfoLayout;
    private ImageView newInfoUnreadView;

    private NewNotifyMessageReceiver notifyMessageReceiver;
    private NewNoticeReceiver newNoticeReceiver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.NEW_MESSAGE);
        notifyMessageReceiver = new NewNotifyMessageReceiver();
        getActivity().registerReceiver(notifyMessageReceiver,intentFilter);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.NEW_NOTICE);
        newNoticeReceiver = new NewNoticeReceiver();
        getActivity().registerReceiver(newNoticeReceiver,filter);

        if(rootView != null){//已经存在
            initView();//初始化布局控件
            initEvents();//初始化各个控件的点击事件
            return rootView;
        }
        rootView = inflater.inflate(R.layout.message_fragment,container,false);
        initView();
        initEvents();
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        loadNotifyMessage();
        messageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        if (notifyMessageReceiver!= null){
            getActivity().unregisterReceiver(notifyMessageReceiver);
            notifyMessageReceiver = null;
        }

        if (newNoticeReceiver != null){
            getActivity().unregisterReceiver(newNoticeReceiver);
            newNoticeReceiver = null;
        }
        super.onDestroy();
    }

    /**
     * 初始化点击事件
     */
    private void initEvents() {
        newInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewInfoActivity.startNewInfoActivity(getContext(),Intent.FLAG_ACTIVITY_NEW_TASK);
                newInfoUnreadView.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * 初始化一些控件
     */
    private void initView() {
        RecyclerView recyclerView = rootView.findViewById(R.id.message_recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.getContext());
        recyclerView.setLayoutManager(manager);
        loadNotifyMessage();
        messageAdapter = new MessageAdapter(mesageList);
        recyclerView.setAdapter(messageAdapter);

        newInfoLayout = rootView.findViewById(R.id.new_info);
        newInfoUnreadView = rootView.findViewById(R.id.unread_img);
    }

    /**
     * 清空messageList从本地查找消息
     */
    private void loadNotifyMessage() {
        mesageList.clear();
        if (MainActivity.userId == null){
            return;
        }
        List<NotifyMessage> notifyMessageList = DataSupport.where("userId=?",MainActivity.userId).order("time desc").find(NotifyMessage.class);
        Map<String,String> map;
        for (NotifyMessage notifyMessage:notifyMessageList){
            map = new HashMap<>();
            map.put("id", notifyMessage.getFriendOrGroupId());
            map.put("type",notifyMessage.getType());
            map.put("iconUrl",notifyMessage.getIconUrl());
            map.put("content",notifyMessage.getContent());
            map.put("name",notifyMessage.getName());
            boolean isRead = notifyMessage.getIsRead();
            if (isRead){
                map.put("isRead",NotifyMessage.READ);
            }else{
                map.put("isRead",NotifyMessage.UNREAD);
            }
            mesageList.add(map);
        }
    }


    class NewNotifyMessageReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            loadNotifyMessage();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    class NewNoticeReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    newInfoUnreadView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

}
