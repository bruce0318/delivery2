package com.example.volunteerclient;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.List;

public class GPSUtils {

    //这是用来调GPS的工具

    private static GPSUtils instance;
    private LocationManager locationManager;
    public static final int LOCATION_CODE = 1000;
    public static final int OPEN_GPS_CODE = 1001;

    public String province = "";

    public static GPSUtils getInstance() {
        if (instance == null) {
            instance = new GPSUtils();
        }
        return instance;
    }

    //获取定位
    public  Locid getLocation(Context context){

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE); // 使用传入的Context

        Location location = null;
        // 是否已经授权
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }

        Locid locid = new Locid();
        if (location != null) {
            Log.i("GPS: ", "获取位置信息成功");
            locid.lat = location.getLatitude();
            locid.lon = location.getLongitude();
            Log.i("GPS: ", "纬度：" + locid.lat);
            Log.i("GPS: ", "经度：" + locid.lon);
            // 获取地址信息

        } else {
            Log.i("GPS: ", "获取位置信息失败，请检查是够开启GPS,是否授权");
        }

        return locid;

    }



    public String getCity(Context context) {
        Log.i("GPS: ", "getCity");
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE); // 使用传入的Context

        Location location = null;
        // 是否已经授权
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }

        String p = "";
        if (location != null) {
            Log.i("GPS: ", "获取位置信息成功");
            Log.i("GPS: ", "经度：" + location.getLatitude());
            Log.i("GPS: ", "纬度：" + location.getLongitude());

            // 获取地址信息
            p = getAddress(context, location.getLatitude(), location.getLongitude());
            Log.i("GPS: ", "location：" + p);
        } else {
            Log.i("GPS: ", "获取位置信息失败，请检查是够开启GPS,是否授权");
        }

        return p;
    }

    /*
     * 根据经度纬度 获取国家，省份
     * */
    public String getAddress(Context context, double latitude, double longitude) {
        String cityName = "";
        List<Address> addList = null;
        Geocoder ge = new Geocoder(context);
        try {
            addList = ge.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addList != null && addList.size() > 0) {
            for (int i = 0; i < addList.size(); i++) {
                Address ad = addList.get(i);
                cityName += ad.getCountryName() + " " + ad.getLocality();
            }
        }
        return cityName;
    }

    public class Locid{
        public double lat;
        public double lon;
    }
}

