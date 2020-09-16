package com.qut.sps.util;

import android.content.Context;
import android.content.res.Configuration;

/**
 * 定义一个判断竖屏的类
 */

public class ScreenUtil {
    public static boolean isScreenOrientationPortrait(Context context){
        if(context.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT){
            return true;
        }else{
            return false;
        }
    }
}
