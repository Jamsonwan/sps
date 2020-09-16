package com.qut.sps.aty;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.db.GroupMessage;
import com.qut.sps.db.Groups;
import com.qut.sps.db.NotifyMessage;
import com.qut.sps.util.ActivityCollector;
import com.qut.sps.util.BaseActivity;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.util.MyApplication;
import com.qut.sps.view.MyFragment;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroupInfoActivity extends BaseActivity {

    public static final int EXIT_DISMISS_GROUP = 1;

    private String groupId;
    private String EMGroupId;
    private String groupName;
    private int type;

    private LinearLayout myGroupNoteLayout;
    private LinearLayout memberListLayout;
    private LinearLayout letDanceLayout;
    private LinearLayout inviteMemberLayout;
    private LinearLayout exitGroupLayout;
    private LinearLayout dismissGroupLayout;
    private LinearLayout dateLayout;

    private ImageView groupIconView;
    private TextView groupNameView;
    private TextView groupDescriptionView;
    private LinearLayout deleteGroupMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_group_info);

        groupId = getIntent().getStringExtra("groupId");
        initView();
        initGroupInfo();
        initEvent();
    }

    /**
     * 初始化各个控件的点击事件
     */
    private void initEvent() {

        myGroupNoteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GroupNoteActivity.startGroupNoteActivity(GroupInfoActivity.this,groupId,MyFragment.userId);
            }
        });

        memberListLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GroupMemberListActivity.startGroupMemberListActivity(GroupInfoActivity.this,groupId,type,EMGroupId);
            }
        });

        letDanceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LetDanceActivity.startLetDanceActivity(GroupInfoActivity.this, MyFragment.userId,EMGroupId);
            }
        });

        inviteMemberLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InviteFriendActivity.startInviteFriendActivity(GroupInfoActivity.this,EMGroupId,type);
            }
        });

        dateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MakeDateActivity.startMakeDateActivity(GroupInfoActivity.this,groupId,groupName);
            }
        });

        exitGroupLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(GroupInfoActivity.this);
                dialog.setCancelable(false);
                dialog.setTitle("提醒");
                dialog.setMessage("确定退出该群？");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        exitGroup(MyFragment.userId,groupId);
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                dialog.show();
            }
        });

        dismissGroupLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(GroupInfoActivity.this);
                dialog.setCancelable(false);
                dialog.setTitle("提醒");
                dialog.setMessage("确定解散该群？");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismissGroup(groupId);
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                dialog.show();
            }
        });

        deleteGroupMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteGroupMessage(groupId,EMGroupId);
                Toast.makeText(GroupInfoActivity.this,"删除记录成功！",Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 删除聊天信息
     * @param groupId
     * @param emGroupId
     */
    private void DeleteGroupMessage(String groupId, String emGroupId) {
        DataSupport.deleteAll(GroupMessage.class,"groupId=? and userId = ?",groupId, MainActivity.userId);
        DataSupport.deleteAll(NotifyMessage.class,"EMId = ? and userId = ?",emGroupId,MainActivity.userId);
        EMClient.getInstance().chatManager().deleteConversation(emGroupId,false);
    }

    /**
     * 解散群
     * @param groupId
     */
    private void dismissGroup(final String groupId) {

        DataSupport.deleteAll(Groups.class,"userId = ? and groupId = ?",MainActivity.userId,groupId);
        DataSupport.deleteAll(NotifyMessage.class,"userId=? and EMId = ?",MainActivity.userId,EMGroupId);
        DataSupport.deleteAll(GroupMessage.class,"userId=? and groupId = ?",MainActivity.userId,groupId);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().destroyGroup(EMGroupId);
                } catch (HyphenateException e) {
                    Log.d("解散群失败！",e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
        String url = HttpUtil.SPS_URL+"DismissGroupServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("id",groupId)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GroupInfoActivity.this,"解散群失败！请检查网络后重试！",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body().string().equals("OK")){
                    Intent intent = new Intent(MyApplication.getContext(),MainActivity.class);
                    startActivity(intent);
                    ActivityCollector.finishAll();
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GroupInfoActivity.this,"服务器异常！",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 用户个人退出群体聊天
     * @param userId
     * @param groupId
     */
    private void exitGroup(String userId,String groupId) {

        DataSupport.deleteAll(Groups.class,"userId = ? and groupId = ?",userId,groupId);
        DataSupport.deleteAll(NotifyMessage.class,"userId=? and EMId = ?",MainActivity.userId,EMGroupId);
        DataSupport.deleteAll(GroupMessage.class,"userId=? and groupId = ?",MainActivity.userId,groupId);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().leaveGroup(EMGroupId);
                } catch (HyphenateException e) {
                    Log.d("退出群失败！",e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();

        String url = HttpUtil.SPS_URL + "DeleteGroupMemberServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("groupId",groupId)
                .add("memberId",userId)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GroupInfoActivity.this,"退出失败!请检查网络后重试!"
                                ,Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body().string().equals("OK")){
                    Intent intent = new Intent(MyApplication.getContext(),MainActivity.class);
                    startActivity(intent);
                    ActivityCollector.finishAll();
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GroupInfoActivity.this,"服务器异常！",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 点击返回
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

    /**
     * 初始化群信息群名和icon
     */
    private void initGroupInfo() {
        List<Groups> groupsList = DataSupport.where("groupId=?",groupId)
                .find(Groups.class);
        for (Groups group : groupsList){
            String url = group.getIconUrl();
            EMGroupId = group.getEMGroupId();
            Glide.with(GroupInfoActivity.this).load(HttpUtil.SPS_SOURCE_URL+url).into(groupIconView);
            groupName = group.getName();
            groupNameView.setText(group.getName());
            type = group.getType();
            groupDescriptionView.setText(group.getDescription());
        }
        if (type == Groups.JOINED_TEAM){
            exitGroupLayout.setVisibility(View.VISIBLE);
            dismissGroupLayout.setVisibility(View.GONE);
        }else {
            exitGroupLayout.setVisibility(View.GONE);
            dismissGroupLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化界面的控件
     */
    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.group_chat_room_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("群资料");
        }

        groupIconView = (ImageView) findViewById(R.id.group_icon);
        groupNameView = (TextView) findViewById(R.id.group_name);
        groupDescriptionView = (TextView) findViewById(R.id.group_description);

        deleteGroupMessage = (LinearLayout) findViewById(R.id.delete_group_message);
        myGroupNoteLayout = (LinearLayout) findViewById(R.id.my_group_note);
        memberListLayout = (LinearLayout) findViewById(R.id.group_member_list);
        letDanceLayout = (LinearLayout) findViewById(R.id.let_us_dance);
        inviteMemberLayout = (LinearLayout) findViewById(R.id.invite_new_member);
        dateLayout = (LinearLayout) findViewById(R.id.make_date);
        exitGroupLayout = (LinearLayout) findViewById(R.id.exit_group);
        dismissGroupLayout = (LinearLayout) findViewById(R.id.dismiss_group);
    }

    /**
     * 启动群资料活动
     * @param context
     * @param groupId
     */
    public static void startGroupInfoActivity(Context context,String groupId){
        Intent intent = new Intent(context,GroupInfoActivity.class);
        intent.putExtra("groupId",groupId);
        context.startActivity(intent);
    }
}
