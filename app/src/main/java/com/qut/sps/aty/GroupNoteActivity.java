package com.qut.sps.aty;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.qut.sps.R;
import com.qut.sps.util.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;


public class GroupNoteActivity extends AppCompatActivity {

    private String groupId;
    private String memberId;

    private Button saveNoteButton;
    private EditText editNoteText;
    private ImageView clearText;
    private TextView restNumOfNoteView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_note);


        groupId = getIntent().getStringExtra("groupId");
        memberId = getIntent().getStringExtra("userId");

        initView();
        getPreviousNote(groupId,memberId);
        initEvent();

    }


    /**
     * 初始化各个事件的点击事件
     */
    private void initEvent() {
        editNoteText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0){
                    clearText.setVisibility(View.VISIBLE);
                    int restNumber = 20 - editable.length();
                    restNumOfNoteView.setText(restNumber+"");
                    if(restNumber < 0){
                        editNoteText.setError("名片太长！");
                        editNoteText.requestFocus();
                    }
                }else {
                    clearText.setVisibility(View.GONE);
                    restNumOfNoteView.setText(20+"");
                }
            }
        });

        clearText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editNoteText.setText("");
            }
        });

        saveNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String note = editNoteText.getText().toString();
                if (note.length() <= 20 && note.length() >= 0){
                    saveChangedNote(groupId,memberId,note);
                }else{
                    Toast.makeText(GroupNoteActivity.this,"名片太长！",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 保存改变的群名片
     * @param groupId
     * @param memberId
     * @param memberNote  成员的新群名片
     */
    private void saveChangedNote(final String groupId, final String memberId, String memberNote) {
        String url = HttpUtil.SPS_URL + "SaveMemberNoteServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("groupId",groupId)
                .add("memberId",memberId)
                .add("memberNote",memberNote)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GroupNoteActivity.this,"修改失败！检查网络后重试！",Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(!TextUtils.isEmpty(response.body().toString())){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GroupNoteActivity.this,"修改成功！",Toast.LENGTH_SHORT).show();
                        }
                    });
                    finish();
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GroupNoteActivity.this,"修改失败！未知错误发生！",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 得到群成员的以前的群名片
     * @param groupId
     * @param memberId
     */
    private void getPreviousNote(String groupId, String memberId) {
        String url = HttpUtil.SPS_URL + "GetMemberNoteServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("groupId",groupId)
                .add("memberId",memberId)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                if (!TextUtils.isEmpty(responseData)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject object = new JSONObject(responseData);
                                editNoteText.setText(object.getString("memberNote"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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

        Button backButton = (Button) findViewById(R.id.title_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView titleText = (TextView) findViewById(R.id.title_name);
        titleText.setText("编辑群名片");

        ImageView titleRightView = (ImageView) findViewById(R.id.title_right_img);
        titleRightView.setVisibility(View.GONE);

        saveNoteButton = (Button) findViewById(R.id.title_right_btn);
        saveNoteButton.setVisibility(View.VISIBLE);
        saveNoteButton.setText("保存");

        clearText = (ImageView) findViewById(R.id.search_clear);
        editNoteText = (EditText) findViewById(R.id.edit_group_note);
        restNumOfNoteView = (TextView) findViewById(R.id.note_length);
    }

    /**
     * 启动该活动
     * @param context
     * @param groupId 群的id
     * @param userId 该用户的id
     */
    public static void startGroupNoteActivity(Context context,String groupId,String userId){
        Intent intent = new Intent(context,GroupNoteActivity.class);
        intent.putExtra("groupId",groupId);
        intent.putExtra("userId",userId);

        context.startActivity(intent);
    }
}
