package com.example.volunteerclient;

import static com.example.tools.Constants.serverURL;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.example.soniceyes.R;
import com.example.tools.NetworkHandler;
import com.example.users.User;

public class OrderDetailActivity extends AppCompatActivity {
    private Button btn_take_order, btn_locate_volunteer, btn_locate_blind;
    private MapView mapView;
    private AMap aMap;

    private String phoneNumber = "";
    private double vlat = 0.0, vlon = 0.0;
    private double blat = 0.0, blon = 0.0; // 求助者坐标

    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;

    private boolean hasDrawnBlind = false;  // 标记是否已经画过盲人点

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.updatePrivacyShow(this, true, true);
        MapsInitializer.updatePrivacyAgree(this, true);
        setContentView(R.layout.order_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("订单详情");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.setMinZoomLevel(15); // 设置最小缩放等级，比如15

        btn_take_order = findViewById(R.id.btn_take_order);
        btn_locate_volunteer = findViewById(R.id.btn_locate_volunteer);
        btn_locate_blind = findViewById(R.id.btn_locate_blind);

        // 接收订单数据
        String aid = getIntent().getStringExtra("aid");
        String address = getIntent().getStringExtra("address");
        blat = getIntent().getDoubleExtra("blat", 0.0);
        blon = getIntent().getDoubleExtra("blon", 0.0);


        if(blat == 0.0 && blon == 0.0){
            Toast.makeText(OrderDetailActivity.this, "获取盲人位置失败", Toast.LENGTH_SHORT);
        }

        String bid = getIntent().getStringExtra("bid");
        String btime = getIntent().getStringExtra("btime");
        String distance = getIntent().getStringExtra("distance");

        // 显示订单数据
        TextView tvOrderNumber = findViewById(R.id.tv_order_number);
        TextView tvAddress = findViewById(R.id.tv_address);
        TextView tvTime = findViewById(R.id.tv_time);
        TextView tvDistance = findViewById(R.id.tv_distance);
        tvOrderNumber.setText("#" + aid);
        tvAddress.setText("地址：" + address);
        tvTime.setText("请求时间：" + btime);
        tvDistance.setText("距离：" + distance + "km");

        btn_locate_volunteer.setOnClickListener(v -> {
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(vlat, vlon), 18));
        });

        btn_locate_blind.setOnClickListener(v -> {
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(blat, blon), 18));
        });

        btn_take_order.setOnClickListener(view -> {
            String servicePathGet = "help/blind?aid=" + aid + "&bid=" + bid + "&vid=" + User.getUserId();
            String getUrl = serverURL + "/" + servicePathGet;
            new Thread(() -> {
                try {
                    String help = NetworkHandler.get(getUrl);
                    JSONObject root = JSONObject.parseObject(help);
                    int code = root.getInteger("code");
                    if (code == 200) {
                        phoneNumber = root.getString("data");
                        runOnUiThread(() -> {
                            Toast.makeText(OrderDetailActivity.this, "接单成功", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(OrderDetailActivity.this, OrderConfirmedActivity.class);
                            intent.putExtra("aid", aid);
                            intent.putExtra("bid", bid);
                            intent.putExtra("address", address);
                            intent.putExtra("blat", blat);
                            intent.putExtra("blon", blon);
                            intent.putExtra("btime", btime);
                            intent.putExtra("phoneNumber", phoneNumber);
                            intent.putExtra("distance", distance);
                            startActivity(intent);
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(OrderDetailActivity.this, "接单失败", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    Log.e("OrderDetailActivity", "Error in network request", e);
                }
            }).start();
        });

        startLocation();
    }

    private void startLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            try {
                mLocationClient = new AMapLocationClient(getApplicationContext());
                mLocationClient.setLocationListener(amapLocation -> {
                    if (amapLocation != null && amapLocation.getErrorCode() == 0) {
                        vlat = amapLocation.getLatitude();
                        vlon = amapLocation.getLongitude();
                        Log.d("OrderDetailActivity", "定位成功: " + vlat + "," + vlon);

                        // 拿到自己定位后一起画
                        showBothPoints();
                    } else {
                        Toast.makeText(this, "定位失败：" + (amapLocation != null ? amapLocation.getErrorInfo() : "未知错误"), Toast.LENGTH_SHORT).show();
                    }
                });

                mLocationOption = new AMapLocationClientOption();
                mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                mLocationOption.setOnceLocation(true);
                mLocationClient.setLocationOption(mLocationOption);
                mLocationClient.startLocation();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "初始化定位失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showBothPoints() {
        aMap.clear(); // 清空之前的Marker

        // 求助者 Marker
        LatLng blindLatLng = new LatLng(blat, blon);
        aMap.addMarker(new MarkerOptions()
                .position(blindLatLng)
                .title("求助者")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.location)));

        // 志愿者 Marker
        LatLng volunteerLatLng = new LatLng(vlat, vlon);
        aMap.addMarker(new MarkerOptions()
                .position(volunteerLatLng)
                .title("志愿者")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.locationpurple)));

        // 地图中心到盲人，缩放
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(blindLatLng, 17));

        aMap.setOnMarkerClickListener(marker -> {
            Toast.makeText(this, marker.getTitle() + " 定位", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
