package com.example.blindclient.entity;

import com.alibaba.fastjson.annotation.JSONField;

public class OnlineStatus {
    @JSONField(name = "id", ordinal = 1)
    private int userId;

    @JSONField(name = "status", ordinal = 2)
    private int status;

    public OnlineStatus(int userId){
        this.userId = userId;
    }

    public OnlineStatus setOnline(){
        this.status = 1;
        return this;
    }

    public OnlineStatus setOffline(){
        this.status = 0;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
