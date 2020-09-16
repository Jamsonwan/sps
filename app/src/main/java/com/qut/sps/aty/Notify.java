package com.qut.sps.aty;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.adapter.DateAdapter;
import com.qut.sps.adapter.DateOfData;
import com.qut.sps.util.Constant;
import com.qut.sps.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Notify extends AppCompatActivity {

    private List<DateOfData> dataList = new ArrayList<>();
    private  DateAdapter adapter;
    private Button back;
    private ListView noteListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);

        initView();
        init();
        try {
            queryFromServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() {

        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                (Notify.this).finish();
            }
        });
        noteListView.setAdapter(adapter);

    }

    private void initView() {
        back=(Button) findViewById(R.id.title_back);

        TextView titleName = (TextView) findViewById(R.id.title_name);
        titleName.setText("预约通知");

        ImageView titleImg = (ImageView) findViewById(R.id.title_right_img);
        titleImg.setVisibility(View.GONE);

        adapter = new DateAdapter(Notify.this, R.layout.date_data, dataList);
        noteListView = (ListView) findViewById(R.id.list_view);
    }

    /**
     * 从服务器查询预约信息
     * @throws IOException
     */
    private void queryFromServer() throws IOException {

        RequestBody requestBody = new FormBody.Builder()
                .add("memberId", MainActivity.userId)
                .build();

        String url = Constant.SPS_URL + "GetDateServlet";
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Notify.this, "查询失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                if (!TextUtils.isEmpty(responseText)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONArray array = new JSONArray(responseText);
                                dataList.clear();
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject object = array.getJSONObject(i);
                                    String groupName = object.getString("groupName");
                                    String nickName = object.getString("nickName");
                                    String account = object.getString("account");
                                    if(nickName.equals("null")){
                                        nickName=account;
                                    }
                                    String time = object.getString("time");
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                    String currentTime = simpleDateFormat.format(Calendar.getInstance().getTime());
                                    int rs = time.compareTo(currentTime);
                                    if(rs < 0){
                                        continue;
                                    }
                                    String place = object.getString("place");
                                    DateOfData p1 = new DateOfData(
                                            "群名："+groupName+"\n"+"邀请人："+nickName+"\n"+"时间："+time+"\n"+"地点："+place, R.drawable.reminde);
                                    dataList.add(p1);
                                }
                                if (dataList.size() == 0){
                                    DateOfData p2 = new DateOfData("预约表空空的，还未有人创建预约哦～～", R.drawable.reminde);
                                    dataList.add(p2);
                                }
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DateOfData p2 = new DateOfData("预约表空空的，还未有人创建预约哦～～", R.drawable.reminde);
                            dataList.add(p2);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }


            }
        });

    }
}
