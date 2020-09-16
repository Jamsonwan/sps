package com.qut.sps.db;

import org.litepal.crud.DataSupport;

/**
 * Created by lenovo on 2017/8/23.
 */

public class NotifyMessage extends DataSupport {

    public static final String READ = "read";
    public static final String UNREAD = "unread";

    private int id;
    private String iconUrl;
    private String name;
    private String content;
    private String type;
    private String EMId;
    private String userId;
    private long time;
    private Boolean isRead;
    private String friendOrGroupId;


    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean read) {
        this.isRead = read;
    }

    public String getFriendOrGroupId() {
        return friendOrGroupId;
    }

    public void setFriendOrGroupId(String friendOrGroupId) {
        this.friendOrGroupId = friendOrGroupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEMId() {
        return EMId;
    }

    public void setEMId(String EMId) {
        this.EMId = EMId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
