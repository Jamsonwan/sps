package com.qut.sps.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 潘攀 on 2017/8/12.
 */

public class Forecast {
    @SerializedName("date")
    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature{
        @SerializedName("max")
        public String max;
        @SerializedName("min")
        public String min;
    }
    public class More{
        @SerializedName("txt_d")
        public String info;
    }

}

