package com.qut.sps.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.widget.Toast;

/**
 * 判断是否处于wifi状态以及处理非wifi下进行播放
 */

public class WifiUtil {

    public static void handleWifi(){
        WifiManager wifiManager = (WifiManager) MyApplication.getContext().getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if( wifiManager!=null ){
            int wifiState = wifiManager.getWifiState();
            if(wifiState == WifiManager.WIFI_STATE_DISABLED){
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                boolean InternetOk = preferences.getBoolean(Constant.INTERNET_OK,false);
                if(InternetOk){
                    Toast toast = Toast.makeText(MyApplication.getContext(),"非wifi下进行播放"
                            ,Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL,0,250);
                    toast.show();
                }else{
                    Toast toast = Toast.makeText(MyApplication.getContext(),"非wifi下进行播放!"
                            ,Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL,0,250);
                    toast.show();
                }

            }else if(wifiState == WifiManager.WIFI_STATE_ENABLED){

            }
        }
    }
}
