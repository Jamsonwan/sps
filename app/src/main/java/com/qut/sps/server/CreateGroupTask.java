package com.qut.sps.server;


import android.content.Intent;
import android.os.AsyncTask;

import com.qut.sps.util.Constant;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.util.MyApplication;

import java.io.File;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by nyy on 2017/8/15.
 */

public class CreateGroupTask extends AsyncTask<Map<String,String>,Integer,String> {


    @Override
    protected String doInBackground(Map<String, String>... maps) {
        return doPost(maps[0]);
    }

    @Override
    protected void onPostExecute(String s) {
       if(!s.equals("失败")){
           Intent intent = new Intent(Constant.UPDATE_GROUP);
           MyApplication.getContext().sendBroadcast(intent);
        }
    }

    private String doPost(Map<String, String> param) {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        //servlet的地址
        String requestUrl = HttpUtil.SPS_URL+"CreateGroupServlet";
        //返回结果
        String result = "error";
        //发送请求数据以表单形式进行发送
        MultipartBody.Builder builder = new MultipartBody.Builder();
        //图片
        builder.addFormDataPart("image", param.get("imagePath"),
                RequestBody.create(MediaType.parse("image/jpeg"), new File(param.get("imagePath"))));

        //字符串表单用于对数据库的操作
        builder.addFormDataPart("groupName",param.get("groupName"));
        builder.addFormDataPart("userId",param.get("userId"));
        builder.addFormDataPart("description",param.get("description"));
        builder.addFormDataPart("EMGroupId",param.get("EMGroupId"));

        RequestBody requestBody = builder.build();
        Request.Builder reqBuilder = new Request.Builder();
        Request request = reqBuilder
                .url(requestUrl)
                .post(requestBody)
                .build();

        try{
            //进行上传数据
            Response response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                //返回服务器返回的数据
                return response.body().string();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //返回上传失败
        return result;
    }
}
