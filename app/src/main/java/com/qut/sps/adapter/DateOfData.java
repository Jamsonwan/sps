package com.qut.sps.adapter;

/**
 * Created by 潘攀 on 2017/8/11.
 */

public class DateOfData {

    private String name;
    private int imageId;

    public DateOfData(String name,int imageId){
        this.name=name;
        this.imageId=imageId;
    }
    public String getName(){
        return name;
    }
    public int getImageId(){
        return imageId;
    }
}
