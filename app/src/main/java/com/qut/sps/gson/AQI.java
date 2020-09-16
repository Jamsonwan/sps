package com.qut.sps.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 潘攀 on 2017/8/12.
 */

public class AQI {
    public AQICity city;
    public class AQICity{
        @SerializedName("aqi")
        public String aqi;
        @SerializedName("pm25")
        public String pm25;
    }
}
