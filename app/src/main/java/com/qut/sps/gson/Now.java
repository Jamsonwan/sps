package com.qut.sps.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 潘攀 on 2017/8/12.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;
    @SerializedName("cond")
    public More more;

    public class More{
        @SerializedName("txt")
        public String info;
    }
}

