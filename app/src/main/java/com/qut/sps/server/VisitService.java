package com.qut.sps.server;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;

import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.util.Constant;
import com.qut.sps.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VisitService extends Service {

    private String groupName;
    private String nickName;
    private String time="";
    private String place;

    public static AlarmManager alarmManager;
    public static PendingIntent pi;
    private List<String> timeList =  new ArrayList<>();


    public VisitService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (MainActivity.userId == null){
            stopSelf();
            if (alarmManager != null){
                alarmManager.cancel(pi);
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    queryDateFromService();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

            alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            int requestTime = 60 * 1000;
            long triggerAtTime = SystemClock.elapsedRealtime()+requestTime;
            Intent i = new Intent(this, VisitService.class);
            pi = PendingIntent.getService(this, 0, i, 0);
            alarmManager.cancel(pi);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
            return super.onStartCommand(intent, flags, startId);

        }

    /**
     * 从服务器查询预约信息
     * @throws IOException
     */
    private void queryDateFromService() throws IOException {

        String url = Constant.SPS_URL+ "GetDateServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("memberId", MainActivity.userId)
                .build();

        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                if(!TextUtils.isEmpty(responseText)){
                    try {
                        JSONArray array = new JSONArray(responseText);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            groupName = object.getString("groupName");
                            nickName = object.getString("nickName");
                            String account = object.getString("account");
                            if(nickName.equals("null")){
                                nickName=account;
                            }
                            time = object.getString("time");
                            place = object.getString("place");

                            showNoification();
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    /**
     * 转化时间类型，显示通知
     */
    private void showNoification() {
        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String d=sim.format(Calendar.getInstance().getTime());
        Date t= null;
        try {
            t = sim.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long date=t.getTime()-1*60*60*1000;

        boolean flag = true;
        for(String time1 : timeList){
            if (time1.equals(time)){
                flag = false;
            }
        }
        String ti=sim.format(date);

        if(ti.equals(d) && flag) {
            timeList.add(time);
            popNotification();
        }
    }
    /**
     * 弹出通知信息
     */
    private void popNotification() {
        NotificationManagerCompat.from(this).areNotificationsEnabled();
        Intent broadcastIntent = new Intent(VisitService.this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(VisitService.this, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification;
        notification = new NotificationCompat.Builder(VisitService.this)
                .setContentTitle("预约dance")
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.logo_white)
                .setVibrate(new long[]{0,1000,1000,1000})
                .setAutoCancel(true)
              //.setStyle(new NotificationCompat.BigTextStyle().bigText(groupName+"群的"+nickName+",邀您在"+time+" "+place+"happy"))
               // .setStyle(new NotificationCompat.BigTextStyle().bigText("happy"
                .setContentText(groupName+"群的"+nickName+",邀您一起在"+time+" "+place+"happy")
                .build();
        notificationManager.notify(2, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected void requestPermission(int requestCode) {
        // TODO Auto-generated method stub
        // 6.0以上系统才可以判断权限

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
        return;
    }
}
