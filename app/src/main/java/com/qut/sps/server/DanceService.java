package com.qut.sps.server;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
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

/**
 * Created by lenovo on 2017/8/21.
 */

public class DanceService extends Service {

    public static AlarmManager manager;
    public static PendingIntent pendingIntent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String EMGroupId = intent.getStringExtra("EMGroupId");
        String leaderAccount = intent.getStringExtra("leaderAccount");
        GetMusicAndControl(EMGroupId,leaderAccount);

        manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int onSecond = 500;
        long triggerAtTime = SystemClock.elapsedRealtime()+onSecond;

        Intent i = new Intent(this,DanceService.class);
        i.putExtra("EMGroupId",EMGroupId);
        i.putExtra("leaderAccount",leaderAccount);
        pendingIntent = PendingIntent.getService(this,0,i,PendingIntent.FLAG_CANCEL_CURRENT);

        manager.cancel(pendingIntent);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void GetMusicAndControl(String EMGroupId, String leaderAccount) {
        String url = HttpUtil.SPS_URL + "GetMusicAndControlServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("EMGroupId",EMGroupId)
                .add("leaderAccount",leaderAccount)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                boolean start;
                String musicUrl = null;
                String control = null;
                String musicName = null;
                String singer = null;
                if (!TextUtils.isEmpty(responseData)){
                    start = true;
                    try {
                        JSONObject object = new JSONObject(responseData);
                        musicUrl = object.getString("musicUrl");
                        control = object.getString("control");
                        musicName = object.getString("musicName");
                        singer = object.getString("singer");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    start = false;
                }
                Intent intent = new Intent("com.qut.sps.DANCE_BROADCAST");
                intent.putExtra("start",start);
                intent.putExtra("musicUrl",musicUrl);
                intent.putExtra("control",control);
                intent.putExtra("musicName",musicName);
                intent.putExtra("singer",singer);
                sendBroadcast(intent);
            }
        });
    }

}
