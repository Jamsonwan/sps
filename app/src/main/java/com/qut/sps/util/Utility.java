package com.qut.sps.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.qut.sps.MainActivity;
import com.qut.sps.db.City;
import com.qut.sps.db.County;
import com.qut.sps.db.Province;
import com.qut.sps.db.UsersInfo;
import com.qut.sps.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 潘攀 on 2017/8/11.
 */

public class Utility {
    /**
     * 解析和处理服务器返回的个人信息数据
     * @param response
     * @return
     * @throws JSONException
     */
    public static boolean handleUsersInfor(String response) throws JSONException {
        JSONArray array = new JSONArray(response);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            UsersInfo userInfo = new UsersInfo();
            userInfo.setId(Integer.parseInt(MainActivity.userId));
            userInfo.setAccount(object.getString("account"));
            userInfo.setNickName(object.getString("nickname"));
            userInfo.setIconUrl("usersIcon/"+object.getString("iconurl"));
            userInfo.setSex(object.getString("sex"));
            if(object.getString("age").equals("null")){
                userInfo.setAge(0);
            }else{
                userInfo.setAge(object.getInt("age"));
            }
            userInfo.setTel(object.getString("tel"));
            userInfo.setAddress(object.getString("address"));
            userInfo.setProfession(object.getString("profession"));
            userInfo.save();
        }
        return true;
    }

    /**
     *
     * 解析和处理服务器返回的省级数据
     * @param response
     * @return
     */
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allProvince=new JSONArray(response);
                for(int i=0;i<allProvince.length();i++){
                    JSONObject provinceObject=allProvince.getJSONObject(i);
                    Province province=new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     *解析和处理服务器返回的市级数据
     * @param response
     * @param provinceId
     * @return
     */
    public static boolean handleCityResponse(String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCities=new JSONArray(response);
                for(int i=0;i<allCities.length();i++){
                    JSONObject cityObject=allCities.getJSONObject(i);
                    City city=new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     *解析和处理服务器返回的县级数据
     * @param response
     * @param cityId
     * @return
     */
    public static boolean handleCountyResponse(String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCounties=new JSONArray(response);
                for(int i=0;i<allCounties.length();i++){
                    JSONObject countyObject=allCounties.getJSONObject(i);
                    County county=new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 将返回的json数据解析成Weather实体类
     * @param response
     * @return
     */
    public static Weather handleWeatherResponse(String response){
        try{
            JSONObject jsonObject=new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

