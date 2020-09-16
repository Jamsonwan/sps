package com.qut.sps.util;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by lenovo on 2017/8/6.
 */

public class HttpUtil {
    public static final String  SPS_URL="http://132.232.176.42:8080/sps/";
    public static final String  SPS_SOURCE_URL ="http://132.232.176.42:8080/spsSource/";
    public static final String GUOLIN_PIC="http://guolin.tech/api/bing_pic";

    public static void sendOkHttpRequest(String address,RequestBody requestBody,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request =  new Request.Builder().url(address)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request =  new Request.Builder().url(address)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static Response sendOkHttpRequest(String address, RequestBody requestBody) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Response response;
        Request request = new Request.Builder()
                .url(address)
                .post(requestBody)
                .build();
        response  =  client.newCall(request).execute();
        return response;
    }

}
