package com.qut.sps.db;

import org.litepal.crud.DataSupport;

/**
 * Created by lenovo on 2017/8/6.
 */

public class GroupMessage extends DataSupport {

    private int id;
    private int userId;
    private int groupId;
    private int type;
    private String content;
    private String iconUrl;
    private String note;

    public String getIconUrl() {
        return iconUrl;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
