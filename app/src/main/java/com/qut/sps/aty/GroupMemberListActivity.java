package com.qut.sps.aty;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.qut.sps.R;
import com.qut.sps.adapter.FriendsAndTeamAdapter;
import com.qut.sps.db.Groups;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.view.MyFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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

public class GroupMemberListActivity extends AppCompatActivity {

    private int type;
    private String groupId;
    private String EMGroupId;

    private List<Map<String,String>> memberList = new ArrayList<>();
    private List<Integer> positionList = new ArrayList<>();

    private Button titleRightButton;
    private  Button backButton;
    private Button titleCancelButton;

    private FriendsAndTeamAdapter adapter;

    private CircleImageView groupOwnerIcon;
    private  ImageView titleRightView;

    private TextView groupOwnerName;

    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member_list);

        type = getIntent().getIntExtra("type",0);
        groupId = getIntent().getStringExtra("groupId");
        EMGroupId = getIntent().getStringExtra("EMGroupId");

        initView();
        initGroupOwner();
        loadMemberList();
        initEvent();
    }

    /**
     * 初始化点击事件
     */
    private void initEvent() {

        adapter.setOnItemClickListener(new FriendsAndTeamAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position, String id) {

            }

            @Override
            public void OnItemCheckClick(View view, int position, boolean isChecked) {
                boolean flag = true;
                int m = 0;
                int i = 0;
                for(int previous : positionList){
                    if (previous == position) {
                        flag = false;
                        m = i;
                    }
                    i++;
                }
                if (flag){
                    positionList.add(position);
                }else{
                    positionList.remove(m);
                }
            }
        });

        titleRightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupWindow();
            }
        });

        titleRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(positionList.size() > 0){
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(GroupMemberListActivity.this);
                    dialog.setCancelable(false);
                    dialog.setTitle("提醒");
                    if (positionList.size() > 1){
                        dialog.setMessage("确定移除这些群成员?");
                    }else {
                        dialog.setMessage("确定移除该群成员？");
                    }
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            deleteMember();

                            titleRightButton.setVisibility(View.GONE);
                            titleRightView.setVisibility(View.VISIBLE);

                            titleCancelButton.setVisibility(View.GONE);
                            backButton.setVisibility(View.VISIBLE);
                        }
                    });
                    dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    dialog.show();
                }else{
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(GroupMemberListActivity.this);
                    alertDialog.setCancelable(false);
                    alertDialog.setTitle("提醒");
                    alertDialog.setMessage("请先选择要移除的群成员！");

                    alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    alertDialog.show();
                }
            }
        });

        titleCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.flag = !adapter.flag;
                adapter.notifyDataSetChanged();

                titleCancelButton.setVisibility(View.GONE);
                backButton.setVisibility(View.VISIBLE);

                titleRightView.setVisibility(View.VISIBLE);
                titleRightButton.setVisibility(View.GONE);
            }
        });
    }

    /**
     *从服务器删除群成员
     */
    private void deleteMember() {

        String url = HttpUtil.SPS_URL + "DeleteGroupMemberServlet";
        String memberId ="";

        int flag = 0;
        for(int  position : positionList){
            position = position - flag;
            DeleteOnServer(memberList.get(position).get("id"));
            memberId += "," + memberList.get(position).get("id");
            memberList.remove(position);
            flag++;
        }
        memberId = memberId.substring(1);

        final ProgressDialog progressDialog = new ProgressDialog(GroupMemberListActivity.this);
        progressDialog.setTitle("正在删除");
        progressDialog.setMessage("请稍后...");
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
                        Toast.makeText(GroupMemberListActivity.this,
                                "删除成员成功！",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();

        RequestBody requestBody = new FormBody.Builder()
                .add("groupId",groupId)
                .add("memberId",memberId)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GroupMemberListActivity.this,"删除失败！请检查网络后重试！"
                                ,Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body().string().equals("OK")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.flag = !adapter.flag;
                            adapter.notifyDataSetChanged();
                            positionList.clear();

                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GroupMemberListActivity.this,
                                    "服务器内部异常！",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 从环信服务器删除
     * @param id
     */
    private void DeleteOnServer(String id) {
        String url = HttpUtil.SPS_URL + "GetUserInfoServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("id",id)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                if (!TextUtils.isEmpty(responseData)){
                    try {
                        JSONObject object = new JSONObject(responseData);
                        String account = object.getString("account");
                        if (account != null && EMGroupId != null){
                            try {
                                EMClient.getInstance().groupManager().removeUserFromGroup(EMGroupId,account);
                            } catch (HyphenateException e) {
                                e.printStackTrace();
                                Log.d("删除失败!",e.getMessage());
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 初始化群主基本信息
     */
    private void initGroupOwner() {
        if (type == Groups.BUILD_TEAM){
           titleRightView.setVisibility(View.VISIBLE);
        }else {
            titleRightView.setVisibility(View.GONE);
        }
        loadGroupOwnerFromServer();
    }

    /**
     * 从服务器获取群主信息
     */
    private void loadGroupOwnerFromServer() {

        String url = HttpUtil.SPS_URL + "GetGroupOwnerServlet";
        final RequestBody requestBody = new FormBody.Builder()
                .add("id",groupId)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GroupMemberListActivity.this,"加载失败!",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                if (!TextUtils.isEmpty(responseData)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONArray jsonArray = new JSONArray(responseData);
                                String iconUrl = null;
                                String ownerName = null;
                                for (int i = 0;i < jsonArray.length();i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    iconUrl = object.getString("iconUrl");
                                    ownerName = object.getString("name");
                                }
                                iconUrl = HttpUtil.SPS_SOURCE_URL + "usersIcon/"+iconUrl;
                                Glide.with(GroupMemberListActivity.this).load(iconUrl)
                                        .into(groupOwnerIcon);
                                groupOwnerName.setText(ownerName);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GroupMemberListActivity.this,"加载失败!",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 加载群成员
     */
    private void loadMemberList() {
        String url = HttpUtil.SPS_URL + "GetGroupMemberListServlet";
        final RequestBody requestBody = new FormBody.Builder()
                .add("groupId",groupId)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GroupMemberListActivity.this,"加载失败！",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                if (!TextUtils.isEmpty(responseData)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONArray jsonArray =new JSONArray(responseData);
                                Map<String,String> map;
                                for (int i = 0;i < jsonArray.length(); i++){
                                    map = new HashMap<>();
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    map.put("iconUrl","usersIcon/"+object.getString("iconUrl"));
                                    map.put("name",object.getString("name"));
                                    map.put("id",object.getString("memberId"));
                                    map.put("type",MyFragment.GROUP_LIST);
                                    memberList.add(map);
                                }
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GroupMemberListActivity.this,"快去邀请其他小伙伴吧！",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 初始化各个控件
     */
    private void initView() {

        titleCancelButton = (Button) findViewById(R.id.title_cancel);

        backButton = (Button) findViewById(R.id.title_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView titleText = (TextView) findViewById(R.id.title_name);
        titleText.setText("群成员");

        titleRightView = (ImageView) findViewById(R.id.title_right_img);
        if(type == Groups.JOINED_TEAM){
            titleRightView.setVisibility(View.GONE);
        }else{
            titleRightView.setImageResource(R.drawable.music_list);
        }

        titleRightButton = (Button) findViewById(R.id.title_right_btn);

        RecyclerView memberListRecyclerView = (RecyclerView) findViewById(R.id.group_member_list_recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        memberListRecyclerView.setLayoutManager(manager);
        adapter = new FriendsAndTeamAdapter(memberList);
        memberListRecyclerView.setAdapter(adapter);

        groupOwnerIcon = (CircleImageView) findViewById(R.id.group_owner_icon);
        groupOwnerName = (TextView) findViewById(R.id.group_owner_name);
    }

    /**
     * 显示popupWindow
     */
    private void showPopupWindow(){

        View contentView = LayoutInflater.from(GroupMemberListActivity.this).inflate(R.layout.pop_group_member_list,null);
        popupWindow = new PopupWindow(contentView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,true);
        Button deleteMemberButton = contentView.findViewById(R.id.pop_delete_member);
        Button cancelButton = contentView.findViewById(R.id.pop_cancel);

        View rootView = LayoutInflater.from(GroupMemberListActivity.this).inflate(R.layout.activity_group_member_list,null);
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM,0,0);

        deleteMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                titleRightView.setVisibility(View.GONE);
                titleRightButton.setVisibility(View.VISIBLE);
                titleRightButton.setText("移除");

                titleCancelButton.setVisibility(View.VISIBLE);
                titleCancelButton.setText("取消");
                backButton.setVisibility(View.GONE);

                positionList.clear();
                adapter.flag = !adapter.flag;
                adapter.notifyDataSetChanged();
                popupWindow.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                titleRightView.setVisibility(View.VISIBLE);
                titleRightButton.setVisibility(View.GONE);
            }
        });
    }
    /**
     * 启动群成员活动
     * @param context
     * @param groupId
     * @param type
     */
    public static void startGroupMemberListActivity(Context context,String groupId,int type,String EMGroupId){
        Intent intent = new Intent(context,GroupMemberListActivity.class);
        intent.putExtra("groupId",groupId);
        intent.putExtra("EMGroupId",EMGroupId);
        intent.putExtra("type",type);
        context.startActivity(intent);
    }

}
