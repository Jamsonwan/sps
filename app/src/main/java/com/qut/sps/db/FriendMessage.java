package com.qut.sps.db;

import org.litepal.crud.DataSupport;

/**
 * Created by lenovo on 2017/8/6.
 */

public class FriendMessage extends DataSupport {
    public static final int SENDMESSAGE = 1;
    public static final int RECEIVEMESSAGE = 0;
    private int id;
    private int friendId;
    private int userId;
    private int type;
    private String content;
    private String iconUrl;

    public String getIconUrl() {
        return iconUrl;
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

    public int getFriendId() {
        return friendId;
    }

    public void setFriendId(int friendId) {
        this.friendId = friendId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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
