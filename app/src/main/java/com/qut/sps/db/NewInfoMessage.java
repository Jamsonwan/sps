package com.qut.sps.db;

import org.litepal.crud.DataSupport;

/**
 * Created by lenovo on 2017/8/24.
 */

public class NewInfoMessage extends DataSupport {

    public static final String NEED_CHOOSE = "NEED_CHOOSE";
    public static final String NO_NEED_CHOOSE = "NO_NEED_CHOOSE";

    private int id;
    private String iconUrl;
    private String infoFrom;
    private String otherInfo;
    private String isNeedChoose;
    private String type;
    private String EMGroupId;
    private long time;
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getEMGroupId() {
        return EMGroupId;
    }

    public void setEMGroupId(String EMGroupId) {
        this.EMGroupId = EMGroupId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getInfoFrom() {
        return infoFrom;
    }

    public void setInfoFrom(String infoFrom) {
        this.infoFrom = infoFrom;
    }

    public String getOtherInfo() {
        return otherInfo;
    }

    public void setOtherInfo(String otherInfo) {
        this.otherInfo = otherInfo;
    }

    public String getIsNeedChoose() {
        return isNeedChoose;
    }

    public void setIsNeedChoose(String isNeedChoose) {
        this.isNeedChoose = isNeedChoose;
    }
}
