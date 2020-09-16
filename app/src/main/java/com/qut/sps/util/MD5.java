package com.qut.sps.util;


import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by 13686 on 2017/8/7.
 */

public class MD5 {
    public static String  EncoderByMd5(String str){
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(str.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
    public static boolean checkpassword(String newpasswd,String oldpasswd) {
        if (EncoderByMd5(newpasswd).equals(oldpasswd)) {
            return true;
        } else {
            return false;
        }
    }
}
