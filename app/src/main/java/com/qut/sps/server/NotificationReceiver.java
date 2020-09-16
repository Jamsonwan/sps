package com.qut.sps.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.qut.sps.MainActivity;
import com.qut.sps.aty.Notify;
import com.qut.sps.util.SystemUtils;

/**
 * Created by 潘攀 on 2017/8/13.
 */

public class NotificationReceiver extends BroadcastReceiver {
    /**
     * 在APP进程是否存活的情况下的具体操作
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        //判断app进程是否存活
        if(SystemUtils.isAppAlive(context, "com.qut.sps")){

           Intent mainIntent = new Intent(context, MainActivity.class);

            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);

            Intent detailIntent = new Intent(context, Notify.class);

            Intent[] intents = {mainIntent,detailIntent};

            context.startActivities(intents);
        }else {

            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.qut.sps");

            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            context.startActivity(launchIntent);
        }
    }
}

