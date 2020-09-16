package com.qut.sps.db;

import org.litepal.crud.DataSupport;

/**
 * Created by lenovo on 2017/8/7.
 */

public class Groups extends DataSupport {

    public static int BUILD_TEAM = 1;
    public static int JOINED_TEAM = 0;

    private int id;
    private String iconUrl;
    private String name;
    private int type;
    private int userId;
    private int groupId;
    private String description;
    private String EMGroupId;

    public String getEMGroupId() {
        return EMGroupId;
    }

    public void setEMGroupId(String EMGroupId) {
        this.EMGroupId = EMGroupId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
