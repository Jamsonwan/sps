package com.qut.sps.aty;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.LinearLayoutManager;

import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.adapter.NewInfoAdapter;
import com.qut.sps.db.NewInfoMessage;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewInfoActivity extends AppCompatActivity {

    private Button clearNewInfo;
    private List<Map<String,String>> messageList = new ArrayList<>();
    private  NewInfoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_info);

        initView();
        initEvent();
    }

    /**
     * 初始化点击事件
     */
    private void initEvent() {
        clearNewInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewInfoActivity.this);
                builder.setCancelable(false);
                builder.setTitle("提醒");
                builder.setMessage("确定要清空新消息？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        clearAllNewInfo();
                        loadNewInfoFromDB();
                        adapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
            }
        });
    }

    /**
     * 清空所有新消息
     */
    private void clearAllNewInfo() {
        DataSupport.deleteAll(NewInfoMessage.class);
    }

    /**
     * 初始化一些控件
     */
    private void initView() {

        Button titleBack = (Button) findViewById(R.id.title_back);
        titleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView titleTextView = (TextView) findViewById(R.id.title_name);
        titleTextView.setText("新消息");

        ImageView titleRightView = (ImageView) findViewById(R.id.title_right_img);
        titleRightView.setVisibility(View.GONE);

        clearNewInfo = (Button) findViewById(R.id.title_right_btn);
        if (null != MainActivity.userId){
            clearNewInfo.setVisibility(View.VISIBLE);
        }
        clearNewInfo.setText("清空");

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.new_info_recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        loadNewInfoFromDB();
        adapter = new NewInfoAdapter(messageList);
        recyclerView.setAdapter(adapter);

    }

    /**
     * 从本地数据库中找新消息
     */
    private void loadNewInfoFromDB() {
        List<NewInfoMessage> infoMessageList = DataSupport.order("time desc").find(NewInfoMessage.class);
        messageList.clear();
        Map<String,String> map;
        for (NewInfoMessage infoMessage:infoMessageList){
            map = new HashMap<>();
            map.put("iconUrl",infoMessage.getIconUrl());
            map.put("infoFrom",infoMessage.getInfoFrom());
            map.put("otherInfo",infoMessage.getOtherInfo());
            map.put("isNeedChoose",infoMessage.getIsNeedChoose());
            map.put("EMGroupId",infoMessage.getEMGroupId());
            map.put("type",infoMessage.getType());
            messageList.add(map);
        }
    }

    /**
     * 启动新消息活动
     * @param context
     * @param flag
     */
    public static void startNewInfoActivity(Context context,int flag){
        Intent intent = new Intent(context,NewInfoActivity.class);
        intent.addFlags(flag);
        context.startActivity(intent);
    }
}
