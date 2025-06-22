package com.example.volunteerclient;

import com.example.users.User;
import android.os.Handler;
import android.os.Looper;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;

public class BVActivity {
    private String aid;
    private String bid;
    private int vid = User.getUserId();
    private String btime;
    private String stime;
    private String etime;
    private double lat;
    private double lon;
    private String address;
    private String distance;
    //    private Context context;
    // 构造函数
    private Handler mHandler;

    public BVActivity() {
        mHandler = new Handler(Looper.getMainLooper());
    }

//    }

    public BVActivity(String aid, String bid, int vid, String btime, String stime, String etime, double lat, double lon, String address, String distance) {
        this.aid = aid;
        this.bid = bid;
        this.vid = vid;
        this.btime = btime;
        this.stime = stime;
        this.etime = etime;
        this.lat = lat;
        this.lon = lon;
        this.address = address;
        this.distance = distance;
    }

    // Getter和Setter方法
    public String getId() {
        return aid;
    }

    public void setId(String id) {
        this.aid = id;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public int getVid() {
        return vid;
    }

    public void setVid(int vid) {
        this.vid = vid;
    }

    public String getBtime() {
        String formattedBtime = formattedTime(btime);
        return formattedBtime;
    }

    public void setBtime(String Btime) {
        this.btime = btime;
    }

    public String getStime() {
        String formattedStime = formattedTime(stime);
        return formattedStime;
    }

    public void setStime(String stime) {
        this.stime = stime;
    }

    public String getEtime() {
        String formattedEtime = formattedTime(etime);
        return formattedEtime;
    }

    public void setEtime(String etime) {
        this.etime = etime;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }


    public void setAddress(String address) {
        this.address = address;
//        new FetchAddressTask().execute(lat, lon);
//        fetchAddressWithThread(lat, lon);
    }

    public String getAddress() {
        return address;
    }

    public String getDistance() {
        return distance;
    }

    //转换一下时间格式
    public static String formattedTime(String time){
        if (time != null){
            OffsetDateTime odt = OffsetDateTime.parse(time);
            odt = odt.withOffsetSameInstant(ZoneOffset.ofHours(+8));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = odt.format(formatter);
            return formattedTime;
        }
        else{
            return  "时间请求失败";
        }
    }

}


