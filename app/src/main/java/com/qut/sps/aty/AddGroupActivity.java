package com.qut.sps.aty;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.qut.sps.R;
import com.qut.sps.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddGroupActivity extends AppCompatActivity {

    private EditText editText;
    private ImageButton imageButton;
    private TextView cancel;

    private LinearLayout layout;
    private CircleImageView circleImageView;
    private TextView textView;

    private Map<String,String> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        initView();
        initListener();
    }

    private void initView() {
        editText = (EditText)findViewById(R.id.query_group);
        imageButton = (ImageButton)findViewById(R.id.search_clear_group);
        cancel = (TextView) findViewById(R.id.start_search_group);
        layout= (LinearLayout) findViewById(R.id.show_group_info);
    }

    private void initListener() {
        editText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            public void afterTextChanged(Editable editable) {

                if(editable.length()>0){
                    imageButton.setVisibility(View.VISIBLE);
                }else{
                    imageButton.setVisibility(View.INVISIBLE);
                    layout.setVisibility(View.GONE);
                }

            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_SEARCH){
// 先隐藏键盘
                    ((InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(
                                    AddGroupActivity.this.getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                    searchGroup();
                    return true;
                }
                return false;
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               finish();
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.getText().clear();
            }
        });
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddGroupActivity.this,ShowAddGroupActivity.class);
                intent.putExtra("info",(Serializable)map);
                startActivity(intent);
            }
        });
    }

    private void searchGroup(){

        String groupName = editText.getText().toString().trim();
        if(groupName.isEmpty()){
            Toast.makeText(AddGroupActivity.this,"请输入团队名称!",Toast.LENGTH_SHORT).show();
        }else{
            String url = HttpUtil.SPS_URL+"SearchGroupServlet";
            RequestBody requestBody = new FormBody.Builder()
                    .add("groupName",groupName)
                    .build();
            HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AddGroupActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String responseText = response.body().string();
                    handMessage(responseText);
                }
            });
        }
    }

    private void handMessage(String responseData){

        if(!TextUtils.isEmpty(responseData)){
            try {
                JSONArray jsonArray = new JSONArray(responseData);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                map = new HashMap<>();
                //为下一个活动准备显示数据
                String iconUrl = jsonObject.getString("iconurl");
                String name = jsonObject.getString("groupname");
                map.put("头像",iconUrl);
                map.put("团名",name);
                map.put("EMGroupId",jsonObject.getString("emgroupid"));
                map.put("id",jsonObject.getString("id"));
                map.put("团长",jsonObject.getString("username").equals("null")?jsonObject.getString("account"):jsonObject.getString("username"));
                map.put("团队介绍",jsonObject.getString("description"));

                showResponse(iconUrl,name);

            }catch (JSONException e){
                e.printStackTrace();
            }
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AddGroupActivity.this,"不存在该团队!",Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    private void showResponse(final String icon,final String name){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                circleImageView = (CircleImageView)findViewById(R.id.y_group_icon);
                textView = (TextView)findViewById(R.id.y_group_name);

                final String requestIcon = HttpUtil.SPS_SOURCE_URL+"groupsIcon/"+icon;
                Glide.with(AddGroupActivity.this).load(requestIcon).into(circleImageView);
                textView.setText(name);
                layout.setVisibility(View.VISIBLE);
            }
        });
    }
}
