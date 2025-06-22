package com.example.blindclient.entity;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class Location implements Serializable {
    @JSONField(name = "locid", ordinal = 1)
    private int userId;

    @JSONField(name = "lat", ordinal = 2)
    private double latitude;

    @JSONField(name = "lon", ordinal = 3)
    private double longitude;

    public Location(int userId, double latitude, double longitude){
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Location(int userId){
        this.userId = userId;
    }

    public void setLoc(double lat, double lon){
        latitude = lat;
        longitude = lon;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
