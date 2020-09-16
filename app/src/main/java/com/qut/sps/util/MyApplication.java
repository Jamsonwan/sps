package com.qut.sps.util;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;

import org.litepal.LitePal;

import java.util.Iterator;
import java.util.List;

/**
 * Created by lenovo on 2017/8/3.
 */

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LitePal.initialize(context);
        initHuanXin();
    }

    private void initHuanXin() {
        EMOptions options = new EMOptions();
        options.setAcceptInvitationAlways(false);
        options.setDeleteMessagesAsExitGroup(true);
        options.setAutoLogin(false);
        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);
        if (processAppName == null ||!processAppName.equalsIgnoreCase(this.getPackageName())) {
            return;
        }
        EMClient.getInstance().init(this, options);
//在做打包混淆时，关闭debug模式，避免消耗不必要的资源
        EMClient.getInstance().setDebugMode(false);

    }

    private String getAppName(int pid) {
        String processName = null;
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pid) {
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
               e.printStackTrace();
            }
        }
        return processName;

    }

    public static Context getContext(){
        return context;
    }
}
