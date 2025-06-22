package com.example.relativeclient.entity;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class activities {
    public  int activity_id;//活动id
    public  int volunteer_id;//志愿者id
    public  double latitude;//视障人士纬度
    public  double longtitude;//经度
    public  long start_time;//活动开始时间
    public  long end_time;//活动结束时间
    public  long b_time;
    public int ac_status;//活动状态
    public String address;//帮助地点
    public static boolean ac_exist=false;

    public activities(int a_id, int v_id, double lat, double lon) {
        activity_id = a_id;
        volunteer_id = v_id;
        latitude = lat;
        longtitude = lon;
    }

    public  int getActivity_id() {
        return this.activity_id;
    }

    public  void setActivity_id(int activity_id) {
        this.activity_id = activity_id;
    }

    public  int getVolunteer_id() {
        return this.volunteer_id;
    }

    public  void setVolunteer_id(int volunteer_id) {
        this.volunteer_id = volunteer_id;
    }

    public  double getLatitude() {
        return this.latitude;
    }

    public  void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public  double getLongtitude() {
        return this.longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public  String getStart_time() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if(this.start_time>=0){
        String dateStr = sdf.format(new Date(this.start_time));
        return dateStr;}
        else {return null;}
    }

    public void setStart_time(long start_time) {
        this.start_time = start_time;
    }

    public  String getEnd_time() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if(this.end_time>=0)
        {String dateStr = sdf.format(new Date(this.end_time));
        return dateStr;}
        else
        {return null;}
    }

    public  void setEnd_time(long end_time) {
        this.end_time = end_time;
    }

    public  String getB_time() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if(this.b_time>=0)
        {String dateStr = sdf.format(new Date(this.b_time));
            return dateStr;}
        else
        {return null;}
    }
    public void setB_time(long btime){
        this.b_time=btime;
    }

    public int getAc_status() {
        return ac_status;
    }

    public void setAc_status(int ac_status) {
        this.ac_status = ac_status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public static boolean isAc_exist() {
        return ac_exist;
    }

    public static void setAc_exist(boolean ac_exist) {
        activities.ac_exist = ac_exist;
    }
}
