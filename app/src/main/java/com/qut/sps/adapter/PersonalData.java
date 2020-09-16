package com.qut.sps.adapter;

/**
 * Created by 潘攀 on 2017/8/6.
 */

public class PersonalData {

    private String name;
    private String data;

    public PersonalData(String name, String data){
        this.name=name;
        this.data=data;
    }
    public String getName(){
        return name;
    }
    public String getData(){
        return data;
    }

}
