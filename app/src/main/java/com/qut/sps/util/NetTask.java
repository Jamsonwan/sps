package com.qut.sps.util;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.qut.sps.MainActivity;
import com.qut.sps.db.UsersInfo;

import org.json.JSONObject;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by 潘攀 on 2017/8/15.
 */

public class NetTask extends AsyncTask<String,Integer,String>{

    @Override
    protected String doInBackground(String... strings) {
        return doPost(strings[0]);

    }

    protected void onPostExecute(String s) {
        Log.i("Result",s);
    }


    protected  String doPost(String imagePath) {

        OkHttpClient mOkHttpClient = new OkHttpClient();
        String requestUrl = HttpUtil.SPS_URL+"UpLoadIconServlet";
        String result = "error";

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.addFormDataPart("image", imagePath,
                RequestBody.create(MediaType.parse("image/jpeg"), new File(imagePath)));


        builder.addFormDataPart("id", MainActivity.userId);

        RequestBody requestBody = builder.build();

        Request.Builder reqBuilder = new Request.Builder();
        Request request = reqBuilder
                .url(requestUrl)
                .post(requestBody)
                .build();
        try{
            Response response = mOkHttpClient.newCall(request).execute();

            String responseText = response.body().string();

            if (response.isSuccessful()) {

                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject object = new JSONObject(responseText);
                        result = object.getString("result");
                        showResponse(result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    private void showResponse(String result) {
        if (result.contains(".png")){
            UsersInfo usersInfo = new UsersInfo();
            usersInfo.setIconUrl("usersIcon/"+result);
            usersInfo.updateAll("account=?",MainActivity.userAccount);
        }
       Log.d("结果 ",result);
    }
}

