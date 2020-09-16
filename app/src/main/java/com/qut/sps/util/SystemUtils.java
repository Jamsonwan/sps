package com.qut.sps.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.qut.sps.aty.Notify;

import java.util.List;

/**
 * Created by 潘攀 on 2017/8/13.
 */

public class SystemUtils {
    /**
     * 判断应用是否已经启动
     * @param context 一个context
     * @param packageName 要判断应用的包名
     * @return boolean
     */
    public static boolean isAppAlive(Context context, String packageName){
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();

        for(int i = 0; i < processInfos.size(); i++){
            if(processInfos.get(i).processName.equals(packageName)){

                return true;
            }
        }

        return false;
    }
    public static void startDetailActivity(Context context){
        Intent intent = new Intent(context, Notify.class);
        context.startActivity(intent);
    }

}

