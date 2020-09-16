package com.qut.sps.server;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.TextUtils;

import com.qut.sps.util.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class JudeLeaderService extends Service {

    private int currentState;
    private boolean haveLeader;
    private String leaderAccount;
    public  static AlarmManager manager;
    public static PendingIntent pendingIntent;

    public JudeLeaderService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String EMGroupId = intent.getStringExtra("EMGroupId");
        updateLeader(EMGroupId);

        manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int oneSecond = 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + oneSecond;
        Intent i = new Intent(this,JudeLeaderService.class);
        i.putExtra("EMGroupId", EMGroupId);
        pendingIntent = PendingIntent.getService(this,0,i,PendingIntent.FLAG_CANCEL_CURRENT);
        manager.cancel(pendingIntent);
        manager.set(AlarmManager.ELAPSED_REALTIME,triggerAtTime,pendingIntent);

        return super.onStartCommand(intent, flags, startId);
    }
    private void updateLeader(String EMGroupId) {
        String url = HttpUtil.SPS_URL + "JudgeLeaderServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("EMGroupId",EMGroupId)
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
                    haveLeader = true;
                    try {
                        JSONObject object = new JSONObject(responseData);
                        currentState = Integer.parseInt(object.getString("currentState"));
                        leaderAccount = object.getString("leaderAccount");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    haveLeader = false;
                }
                Intent intent = new Intent("com.qut.sps.LOCAL_BROADCAST");
                intent.putExtra("haveLeader",haveLeader);
                intent.putExtra("currentState",currentState);
                intent.putExtra("leaderAccount",leaderAccount);
                sendBroadcast(intent);
            }
        });
    }
}
