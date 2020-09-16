package com.qut.sps.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 潘攀 on 2017/8/12.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;
    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
