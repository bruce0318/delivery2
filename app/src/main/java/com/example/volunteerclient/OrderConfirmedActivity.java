package com.example.volunteerclient;

import static com.example.tools.Constants.serverURL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

public class OrderConfirmedActivity extends Activity {

    // UI
    private Button btn_call, btn_over, btn_refresh, btn_start_navigation, btn_locate_volunteer, btn_locate_blind;
    private TextView TVaid, TVaddress, TVphone, TVdistance, TVtime, censorship;
    private MapView mapView;
    private AMap aMap;

    // 地图和定位
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;

    // 业务数据
    private Double vlat = 0.0, vlon = 0.0;
    private String aid = "", address = "", bid = "", btime = "", phoneNumber = "", distance = "";
    private Double blat = 0.0, blon = 0.0; // 求助者位置


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 高德隐私合规
        MapsInitializer.updatePrivacyShow(this, true, true);
        MapsInitializer.updatePrivacyAgree(this, true);

        setContentView(R.layout.order_confirmed);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState); // 必须！

        aMap = mapView.getMap();
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.setMinZoomLevel(15); // 设置最小缩放等级，比如15


        initializeViews();

        btn_locate_volunteer.setOnClickListener(v -> {
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(vlat, vlon), 18));
        });

        btn_locate_blind.setOnClickListener(v -> {
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(blat, blon), 18));
        });

        btn_start_navigation.setOnClickListener(v -> {
            showNavigationChoiceDialog();
        });

        setupData();
        setupListeners();
        startLocation();
    }


    private void initializeViews() {
        TVaid = findViewById(R.id.tv_aid);
        TVaddress = findViewById(R.id.tv_address);
        TVphone = findViewById(R.id.tv_phone);
        TVdistance = findViewById(R.id.tv_distance);
        TVtime = findViewById(R.id.tv_time);
        btn_call = findViewById(R.id.btn_call);
        btn_over = findViewById(R.id.btn_over);
        btn_refresh = findViewById(R.id.btn_refresh);
        censorship = findViewById(R.id.Censorship);
        btn_locate_volunteer = findViewById(R.id.btn_locate_volunteer);
        btn_start_navigation = findViewById(R.id.btn_start_navigation);
        btn_locate_blind = findViewById(R.id.btn_locate_blind);

    }

    private void setupData() {
        Intent intent = getIntent();
        if (intent != null) {
            aid = intent.getStringExtra("aid");
            address = intent.getStringExtra("address");
            bid = intent.getStringExtra("bid");
            blat = intent.getDoubleExtra("blat", 0.0);
            blon = intent.getDoubleExtra("blon", 0.0);
            btime = intent.getStringExtra("btime");
            phoneNumber = intent.getStringExtra("phoneNumber");
            distance = intent.getStringExtra("distance");
        }

        TVaid.setText("求助号：" + aid);
        TVaddress.setText("地址：" + address);
        TVtime.setText(btime);
        TVphone.setText(phoneNumber);
        TVdistance.setText("距离：" + distance + "km");
        censorship.setText("审图号：" + aMap.getMapContentApprovalNumber());
    }

    private void setupListeners() {
        btn_call.setOnClickListener(view -> dialNumber(phoneNumber));
        btn_over.setOnClickListener(view -> {
            finishHelp();
        });
        btn_refresh.setOnClickListener(view -> refreshBlind());
    }

    private void startLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            try {
                mLocationClient = new AMapLocationClient(getApplicationContext());
                mLocationOption = new AMapLocationClientOption();
                mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                mLocationOption.setOnceLocation(true); // 只定位一次

                mLocationClient.setLocationListener(aMapLocation -> {
                    if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                        vlat = aMapLocation.getLatitude();
                        vlon = aMapLocation.getLongitude();
                        Log.d("OrderConfirmedActivity", "定位成功：" + vlat + "," + vlon);
                        showPoints();
                    } else {
                        Toast.makeText(this, "定位失败：" + (aMapLocation != null ? aMapLocation.getErrorInfo() : "未知错误"), Toast.LENGTH_SHORT).show();
                    }
                });

                mLocationClient.setLocationOption(mLocationOption);
                mLocationClient.startLocation();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "初始化定位失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showPoints() {
        aMap.clear();

        LatLng blindLatLng = new LatLng(blat, blon);
        LatLng volunteerLatLng = new LatLng(vlat, vlon);

        aMap.addMarker(new MarkerOptions()
                .position(blindLatLng)
                .title("求助者")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.location)));

        aMap.addMarker(new MarkerOptions()
                .position(volunteerLatLng)
                .title("志愿者")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.locationpurple)));

        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(blindLatLng, 17));

        aMap.setOnMarkerClickListener(marker -> {
            Toast.makeText(OrderConfirmedActivity.this, marker.getTitle() + " 定位", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void refreshBlind() {
        String getUrl = serverURL + "/location/" + bid;
        new Thread(() -> {
            try {
                String response = NetworkHandler.get(getUrl);
                JSONObject root = JSONObject.parseObject(response);
                JSONObject data = root.getJSONObject("data");
                blat = data.getDouble("lat");
                blon = data.getDouble("lon");

                runOnUiThread(() -> {
                    Toast.makeText(OrderConfirmedActivity.this, "盲人定位更新成功", Toast.LENGTH_SHORT).show();
                    showPoints();
                });
            } catch (Exception e) {
                Log.e("OrderConfirmedActivity", "刷新盲人位置失败", e);
            }
        }).start();
    }

    private void dialNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    private void finishHelp() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("感谢您的善举！")    //设置标题
                .setMessage("您的每一次伸手相助，都在为视障朋友点亮一盏温暖的灯。\n" +
                        "世界因您的善意而更美好，期待下次继续与您同行！")  //设置提醒的信息
                .setIcon(R.drawable.happy_blind)    //设置图标
                .setPositiveButton("继续传递爱心", (dialogInterface, which) -> {
                    // 用户点击确定后执行以下操作
                    String postUrl = serverURL + "/help/end";
                    String jsonbody = "{\"aid\":\"" + aid + "\"}";

                    // 发送网络请求
                    new Thread(() -> NetworkHandler.post(postUrl, jsonbody)).start();

                    // 结束当前 Activity 并跳转
                    finish();
                    startActivity(new Intent(OrderConfirmedActivity.this, Volunteer_homeActivity.class));
                })

                .create();
        dialog.show();


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "正在帮扶中，结束帮扶后可回到主页面", Toast.LENGTH_SHORT).show();
    }

    private void navigateToBlind(int mode) {
        try {
            String modeString;
            switch (mode) {
                case 1:
                    modeString = "1"; // 步行
                    break;
                case 2:
                    modeString = "2"; // 骑行
                    break;
                default:
                    modeString = "0"; // 驾车
            }

            Uri uri = Uri.parse("androidamap://route/plan/?" +
                    "sourceApplication=VolunteerApp" +
                    "&dlat=" + blat +
                    "&dlon=" + blon +
                    "&dname=盲人位置" +
                    "&dev=0" +
                    "&t=" + modeString);

            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.autonavi.minimap");
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "请安装高德地图App", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNavigationChoiceDialog() {
        String[] items = {"驾车导航", "步行导航", "骑行导航"};

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("请选择导航方式")
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            navigateToBlind(0); // 驾车
                            break;
                        case 1:
                            navigateToBlind(1); // 步行
                            break;
                        case 2:
                            navigateToBlind(2); // 骑行
                            break;
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

}
