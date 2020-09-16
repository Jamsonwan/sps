package com.qut.sps.aty;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.db.UsersInfo;
import com.qut.sps.util.Constant;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.util.NotificationsUtils;

import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MakeDateActivity extends AppCompatActivity {

    private Button submit;
    private Button back;
    private ImageView backgroundOfGround;
    private TextView groupNameOfDate;
    private TextView peopleOfDate;
    private EditText placeOfDate;
    private TextView dateDay;
    private TextView dateTime;

    private String groupName;
    private String nickName;
    private String groupId;

    private LinearLayout dayText;
    private LinearLayout timeText;
    private String theMonth,theDay,theHour,theMins;
    private String finalTime,finalPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_date);

        boolean r = NotificationsUtils.isNotificationEnabled(this);
        if (!r){

            AlertDialog.Builder dialog = new AlertDialog.Builder(MakeDateActivity.this);
            dialog.setMessage("您的通知栏未打开，请先打开舞吧app的通知状态");
            dialog.setCancelable(false);
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    goToSet();
                }
            });
            dialog.show();

        }

        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");
        nickName = getUserNickName(MainActivity.userId);

        initView();
        init();
    }

    private void goToSet(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE) {
            // 进入设置系统应用权限界面
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            startActivity(intent);
            return;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// 运行系统在5.x环境使用
            // 进入设置系统应用权限界面
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            startActivity(intent);
            return;
        }
    }

    private String getUserNickName(String userId){
        List<UsersInfo> usersInfoList = DataSupport.where("account = ?",MainActivity.userAccount).find(UsersInfo.class);
        for(UsersInfo usersInfo:usersInfoList){
            if (!usersInfo.getNickName().equals("null")){
                return usersInfo.getNickName();
            }else {
                return usersInfo.getAccount();
            }
        }
        return null;
    }

    public static void startMakeDateActivity(Context context,String groupId,String groupName){
        Intent intent = new Intent(context,MakeDateActivity.class);
        intent.putExtra("groupId",groupId);
        intent.putExtra("groupName",groupName);
        context.startActivity(intent);
    }

    private void initView() {

        submit = (Button) findViewById(R.id.title_right_btn);
        submit.setVisibility(View.VISIBLE);
        submit.setText("提交");
        ImageView imageView = (ImageView) findViewById(R.id.title_right_img);
        imageView.setVisibility(View.GONE);

        back=(Button) findViewById(R.id.title_back);

        TextView titleName = (TextView) findViewById(R.id.title_name);
        titleName.setText("预约");

        backgroundOfGround=(ImageView)findViewById(R.id.date_background);
        groupNameOfDate=(TextView)findViewById(R.id.group_name);
        peopleOfDate=(TextView)findViewById(R.id.date_people);
        placeOfDate=(EditText) findViewById(R.id.date_place);
        dateDay=(TextView)findViewById(R.id.date_day);
        dateTime=(TextView)findViewById(R.id.date_time);
        dayText=(LinearLayout)findViewById(R.id.day_text);
        timeText=(LinearLayout)findViewById(R.id.time_text);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case 1:
                if(resultCode == RESULT_OK){
                    String returnData = data.getStringExtra("data_return");
                    dateTime.setText(returnData);
                }
                break;
            case 2:
                if(resultCode == RESULT_OK){
                    String returnData = data.getStringExtra("data_return");
                    dateDay.setText(returnData);
                }
                break;
        }
    }

    private void init() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertToService();
            }
        });
        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                (MakeDateActivity.this).finish();
            }
        });

        Glide.with(this).load(R.drawable.ground8).into(backgroundOfGround);

        groupNameOfDate.setText(groupName);
        peopleOfDate.setText(nickName);

        InitSystemDayAndTime();

        dayText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
                new DatePickerDialog(MakeDateActivity.this,
                        // 绑定监听器
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                String finalMonth;
                                String finalDay;
                                int m=monthOfYear+1;
                                if(m<10){
                                     finalMonth="0"+m;
                                }else{
                                    finalMonth=String.valueOf(m);
                                }
                                if(dayOfMonth<10){
                                    finalDay="0"+dayOfMonth;
                                }else{
                                    finalDay=String.valueOf(dayOfMonth);
                                }
                                dateDay.setText(year + "-" + finalMonth
                                        + "-" + finalDay);
                            }
                        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        }
        });

        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent timeIntent=new Intent(MakeDateActivity.this,SetTimeActivity.class);
                startActivityForResult(timeIntent,1);
            }
        });
    }

    /**
     * 获取系统当前的日期和时间
     */
    private void  InitSystemDayAndTime() {

        Calendar calendar = Calendar.getInstance();
        int  year= calendar.get(Calendar.YEAR);
        int month=calendar.get(Calendar.MONTH)+1;
        int day=calendar.get(Calendar.DAY_OF_MONTH);

        if(month<10){
            theMonth="0"+month;
        }else{
            theMonth=String.valueOf(month);
        }
        if(day<10){
            theDay="0"+day;
        }else{
            theDay=String.valueOf(day);
        }
        dateDay.setText(year+"-"+theMonth+"-"+theDay);

        Calendar c1 = Calendar.getInstance();
        final int curHours = c1.get(Calendar.HOUR_OF_DAY);
        final int curMinutes = c1.get(Calendar.MINUTE);
        if(curHours<10){
            theHour="0"+curHours;
        }else{
            theHour=String.valueOf(curHours);
        }
        if(curMinutes<10){
            theMins="0"+curMinutes;
        }else{
            theMins=String.valueOf(curMinutes);
        }
        dateTime.setText(theHour+":"+theMins);

    }

    /**
     * 向数据库增加预约信息
     */
    private void insertToService() {
        finalTime=dateDay.getText().toString()+" "+dateTime.getText().toString();
        finalPlace=placeOfDate.getText().toString();

        if(dateDay.getText().toString().equals(" ")||dateTime.getText().toString().equals("")||placeOfDate.getText().length()==0) {

            popDialog();

        }else{

            uploadToService();

        }


    }

    /**
     * 上传至服务器
     */
    private void uploadToService() {
        RequestBody requestBody=new FormBody.Builder()
                .add("groupId",groupId)
                .add("userId", MainActivity.userId)
                .add("time",finalTime)
                .add("place",finalPlace)
                .build();
        String url = Constant.SPS_URL + "InsertDateInfoServlet";
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                req();
            }

            private void req() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MakeDateActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                handMessage(responseText);//进行json数据解析
            }

            private void handMessage(String responseText) {
                if(!TextUtils.isEmpty(responseText)){
                    try{
                        JSONObject object=new JSONObject(responseText);
                        String result=object.getString("result");
                        showResponse(result);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

            }
            private void showResponse(final String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        AlertDialog.Builder dialog = new AlertDialog.Builder(MakeDateActivity.this);
                        dialog.setMessage("预约成功");
                        dialog.setCancelable(false);
                        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });
                        dialog.show();

                    }
                });
            }
        });
    }

    /**
     * 弹出对话框
     */
    private void popDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MakeDateActivity.this);
        dialog.setTitle("注意咯");
        dialog.setIcon(R.drawable.img6);
        dialog.setMessage("请将预约信息填写完整哟~~~~");
        dialog.setCancelable(false);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog.show();
    }


}
