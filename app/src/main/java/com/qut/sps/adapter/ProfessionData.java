package com.qut.sps.adapter;

/**
 * Created by 潘攀 on 2017/8/12.
 */

public class ProfessionData {

    private String name;
    private int imageId;

    public ProfessionData(String name,int imageId){
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
