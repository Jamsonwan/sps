package com.qut.sps.aty;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.qut.sps.R;
import com.qut.sps.adapter.SearchFriendResultAdapter;
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

public class SearchFriendActivity extends AppCompatActivity {

    private ImageButton clearSearch;

    private EditText editText;
    private TextView cancel;

    private LinearLayout layout;

    private Map<String,String> map;
    private List<Map<String,String>> list = new ArrayList<>();
    private RecyclerView recyclerView;
    private SearchFriendResultAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friend);

        initView();
        initEvent();
    }

    private void initEvent() {
        Intent intent= getIntent();
        final String data=intent.getStringExtra("data");
        editText.setHint("请输入"+data);
        editText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    clearSearch.setVisibility(View.VISIBLE);
                } else {
                    clearSearch.setVisibility(View.INVISIBLE);
                    layout.setVisibility(View.GONE);
                    list.removeAll(list);
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
                                    SearchFriendActivity.this.getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);

//实现自己的搜索逻辑
                    //从服务器查找
                    String mysearch = editText.getText().toString().trim();
                    if(mysearch.isEmpty()||!mysearch.matches("^[0-9a-zA-Z]*$")&&data.matches("账号")){
                        Toast.makeText(SearchFriendActivity.this,"请按要求输入",Toast.LENGTH_SHORT).show();
                    }else if(data.matches("账号")){//按账号
                        String url = HttpUtil.SPS_URL+"SearchFdByTwoWayServlet";
                        RequestBody requestBody = new FormBody.Builder()
                                .add("account",mysearch)//服务器取数据一定要和键名一致
                                .build();
                        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {//访问服务器失败
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(SearchFriendActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String responseText = response.body().string();
                                //进行json数据解析
                                handMessage(responseText);
                            }

                        });

                    }else{    //按昵称
                        String url = HttpUtil.SPS_URL+"SearchFdByTwoWayServlet";
                        RequestBody requestBody = new FormBody.Builder()
                                .add("nickName",mysearch)//服务器取数据一定要和键名一致
                                .build();
                        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {//访问服务器失败
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(SearchFriendActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                //得到服务器返回的json数据
                                //注意是.string()方法！！！！！
                                String responseText = response.body().string();
                                //进行json数据解析
                                handMessage(responseText);
                            }

                        });
                    }

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

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.getText().clear();
            }
        });

    }

    private void initView() {
        clearSearch = (ImageButton)findViewById(R.id.search_clear);
        editText = (EditText)findViewById(R.id.query);
        cancel = (TextView) findViewById(R.id.start_search);

        layout = (LinearLayout)findViewById(R.id.y_find);

        recyclerView = (RecyclerView)findViewById(R.id.y_look_friend_info);
        LinearLayoutManager layoutManager = new LinearLayoutManager(SearchFriendActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SearchFriendResultAdapter(list);
        recyclerView.setAdapter(adapter);

    }

    private void handMessage(String responseData){
        if(!TextUtils.isEmpty(responseData)){
            try {
                //进行数据解析
                list.clear();
                JSONArray jsonArray = new JSONArray(responseData);

                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    //信息存储
                    map = new HashMap<>();
                    map.put("性别",jsonObject.getString("sex").equals("null")?"未设置":jsonObject.getString("sex"));
                    map.put("年龄",jsonObject.getString("age").equals("null")?"未设置":jsonObject.getString("age"));
                    map.put("地址",jsonObject.getString("address").equals("null")?"未设置":jsonObject.getString("address"));
                    map.put("职业",jsonObject.getString("profession").equals("null")?"未设置":jsonObject.getString("profession"));
                    //显示第一步
                    map.put("昵称",jsonObject.getString("nickname").equals("null")?"未设置":jsonObject.getString("nickname"));
                    map.put("账号",jsonObject.getString("account"));
                    map.put("头像",jsonObject.getString("iconurl").equals("null")?"未设置":jsonObject.getString("iconurl"));
                    list.add(map);
                }
                //显示结果
               showResponse(list);

            }catch (JSONException e){
                e.printStackTrace();
            }
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SearchFriendActivity.this, "不存在该用户！", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

   private void showResponse(final List<Map<String,String>> list){
       runOnUiThread(new Runnable() {
           @Override
           public void run() {
               adapter.notifyDataSetChanged();
               layout.setVisibility(View.VISIBLE);
           }
       });
    }

}

