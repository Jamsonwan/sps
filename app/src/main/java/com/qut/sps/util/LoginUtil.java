package com.qut.sps.util;

import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 登录注册界面所用到的向服务器发送请求的方法
 */

public class LoginUtil{

    /**
     * 登录方法
     * @param account 用户名
     * @param pwd 密码
     */
    public static Map<String,String> signIn(String nickName,String account, String pwd){
        RequestBody requestBody = new FormBody.Builder()
                .add("account",account)
                .add("pwd",pwd)
                .build();
        Map<String,String> message = new HashMap<>();
        try {
            Response response = HttpUtil.sendOkHttpRequest(Constant.SPS_URL + "LoginServlet", requestBody);
            if (response.isSuccessful()){
                String result = response.body().string();
                JSONObject object = new JSONObject(result);
                message.put("code",object.getString("code"));
                message.put("message",object.getString("message"));
            }else {
                message.put("code","000");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("登录错误：",e.getMessage());
            message.put("code","000");
        }
        return message;
    }

    /**
     * 注册方法
     * @param account 用户名
     * @param pwd 密码
     */
    public static Map<String,String> signUp(String nickName, String account,String pwd){
        final RequestBody requestBody = new FormBody.Builder()
                .add("nickName",nickName)
                .add("account",account)
                .add("pwd",pwd)
                .build();
        Map<String,String> message = new HashMap<>();
        try {
            Response response = HttpUtil.sendOkHttpRequest(Constant.SPS_URL + "RegisterServlet", requestBody);
            if (response.isSuccessful()){
                String result = response.body().string();
                JSONObject object = new JSONObject(result);
                message.put("code",object.getString("code"));
                message.put("message",object.getString("message"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("注册失败",e.getMessage());
            message.put("code","000");
        }
        return message;
    }

    /**
     * 验证码获取方法
     * @param phoneNumber 接收验证码的手机号
     */
    public static Map<String,String> getCode(String phoneNumber){
        RequestBody requestBody =  new FormBody.Builder()
                 .add("phoneNumber",phoneNumber)
                .build();
        Map<String,String> message = new HashMap<>();
        try {
            Response response = HttpUtil.sendOkHttpRequest(Constant.SPS_URL + "GetCodeServlet", requestBody);
            if (response.isSuccessful()){
                String result = response.body().string();
                JSONObject object = new JSONObject(result);
                message.put("code",object.getString("code"));
                message.put("message",object.getString("message"));
            }else {
                message.put("code","000");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("获取失败",e.getMessage());
            message.put("code","000");
        }
        return message;
    }

    /**
     * 修改密码
     * @param account 用户名
     * @param password 新密码
     */
    public static Map<String,String> updatePassword(String account,String password){
        final RequestBody requestBody = new FormBody.Builder()
                .add("account",account)
                .add("pwd",password)
                .build();
        Map<String,String> message = new HashMap<>();
        try {
            Response response = HttpUtil.sendOkHttpRequest(Constant.SPS_URL + "ForgetPwdServlet", requestBody);
            if (response.isSuccessful()){
                String result = response.body().string();
                JSONObject object = new JSONObject(result);
                message.put("message",object.getString("message"));
            }else {
                message.put("code","000");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("修改失败",e.getMessage());
            message.put("code","000");
        }
        return message;
    }

}

