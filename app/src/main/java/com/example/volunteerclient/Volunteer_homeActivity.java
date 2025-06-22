package com.example.volunteerclient;

import static com.example.tools.Constants.GaodeAPI_key;
import static com.example.tools.Constants.serverURL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSONObject;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.model.MyLocationStyle;


import com.example.soniceyes.R;
import com.example.tools.NetworkHandler;
import com.example.users.User;
import com.example.relativeclient.InformationActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.util.ArrayList;
import java.util.List;

public class Volunteer_homeActivity extends AppCompatActivity implements AMapLocationListener {

    private FloatingActionButton buttonRefresh, buttonLocate;
    private Double lat = 0.0, lon = 0.0;
    private List<BVActivity> BVActivityList;

    private MapView mapView;
    private AMap aMap;

    // 高德定位
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;

    private ProgressDialog progressDialog;
    private boolean isFirstLocation = true;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.volunteer_home);

        // 高德隐私协议（必须）
        MapsInitializer.updatePrivacyShow(this, true, true);
        MapsInitializer.updatePrivacyAgree(this, true);

        // 地图初始化
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.setMinZoomLevel(15); // 设置最小缩放等级，比如15
        if (aMap == null) {
            Log.e("Volunteer_homeActivity", "AMap initialization failed");
            return;
        }

        // 按钮绑定
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        buttonRefresh = findViewById(R.id.button_refresh);
        buttonLocate = findViewById(R.id.button_locate);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("欢迎加入帮视界!");

                //logo
                Drawable logo = ContextCompat.getDrawable(this, R.mipmap.eyewhite);
                if (logo != null) {
                    int width = (int) (logo.getIntrinsicWidth() * 0.6);
                    int height = (int) (logo.getIntrinsicHeight() * 0.6);
                    logo.setBounds(0, 0, width, height);
                    toolbar.setLogo(logo);
                }
            }
        }
        else {
            Log.e("Volunteer_homeActivity", "Toolbar is not found in the layout");
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在加载...");
        progressDialog.setCancelable(false);

        // 按钮跳转
        bottomNavigationView.setSelectedItemId(R.id.nav_map);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_help) {
                startActivity(new Intent(this, Volunteer_helpActivity.class));
                return true;
            } else if (id == R.id.nav_map) {
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, Volunteer_historyActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, Vedit_informationActivity.class));
                return true;
            }
            return false;
        });

        buttonRefresh.setOnClickListener(view -> {
            buttonRefresh.animate().rotationBy(360).setDuration(500).start(); // 转圈动画
            Toast.makeText(Volunteer_homeActivity.this, "刷新成功", Toast.LENGTH_SHORT).show();
            if (lat != 0.0 && lon != 0.0) {
                showVolunteer();
                getBlind();
            }
            checkStatus();
        });

        buttonLocate.setOnClickListener(view -> {
            if (lat != 0.0 && lon != 0.0) {
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 18));
                Toast.makeText(Volunteer_homeActivity.this, "已定位到自己", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Volunteer_homeActivity.this, "定位中，请稍后...", Toast.LENGTH_SHORT).show();
            }
        });


        // 启动高德定位
        startLocation();
        checkStatus();
    }

    private void startLocation() {
        // 检查定位权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        try {
            mLocationClient = new AMapLocationClient(getApplicationContext());
            mLocationClient.setLocationListener(this);

            mLocationOption = new AMapLocationClientOption();
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setOnceLocation(false); // 连续定位
            mLocationOption.setInterval(2000); // 每2秒定位一次
            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.startLocation();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "初始化定位失败", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                lat = amapLocation.getLatitude();
                lon = amapLocation.getLongitude();
                Log.d("Volunteer_homeActivity", "定位成功: " + lat + "," + lon);

                showVolunteer();  // 随时更新小蓝点

                if (isFirstLocation) {
                    isFirstLocation = false;  // 只让第一次定位时执行
                    getBlind();  // 只第一次发请求
                }

            } else {
                Log.e("Volunteer_homeActivity", "定位失败：" + amapLocation.getErrorInfo());
                Toast.makeText(this, "定位失败：" + amapLocation.getErrorInfo(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    //采用定位蓝点
    private void showVolunteer() {
        MyLocationStyle myLocationStyle = new MyLocationStyle();

        // 蓝点旋转，镜头不跟随
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);

        if (isFirstLocation) {
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 18));
        }

        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.locationpurple));
        myLocationStyle.strokeColor(0);  // 边框透明
        myLocationStyle.radiusFillColor(0); // 填充透明
        myLocationStyle.showMyLocation(true);

        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(false);
        aMap.setMyLocationEnabled(true);
    }





    //以前的调用方法，定位需要手动刷新
    private void showVolunteer2() {
        LatLng latLng = new LatLng(lat, lon);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.locationpurple);
        Bitmap smallBitmap = Bitmap.createScaledBitmap(bitmap,
                bitmap.getWidth() / 2,  // 宽缩小一半
                bitmap.getHeight() / 2, // 高缩小一半
                false);

        MarkerOptions markerOption = new MarkerOptions()
                .position(latLng)
                .title("志愿者")
                .draggable(false)
                .icon(BitmapDescriptorFactory.fromBitmap(smallBitmap))
                .setFlat(true);

        aMap.addMarker(markerOption);
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
    }


    private void showActivitiesOnMap(List<BVActivity> activities) {
        for (BVActivity activity : activities) {
            LatLng point = new LatLng(activity.getLat(), activity.getLon());
            showPoint(point, "求助者" + activity.getId(), R.mipmap.location, activity);
        }
    }

    public void showPoint(LatLng point, String pointName, int idDrawable, BVActivity activity) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), idDrawable);
        Bitmap smallBitmap = Bitmap.createScaledBitmap(bitmap,
                bitmap.getWidth() / 2,
                bitmap.getHeight() / 2,
                false);

        MarkerOptions markerOption = new MarkerOptions()
                .position(point)
                .draggable(false)
                .icon(BitmapDescriptorFactory.fromBitmap(smallBitmap))
                .setFlat(true);

        Marker marker = aMap.addMarker(markerOption);

        aMap.setOnMarkerClickListener(clickedMarker -> {
            if (clickedMarker.equals(marker)) {
                Toast.makeText(Volunteer_homeActivity.this, pointName, Toast.LENGTH_SHORT).show();
                new AlertDialog.Builder(Volunteer_homeActivity.this)
                        .setTitle("提示")
                        .setMessage("是否查看求助详情？")
                        .setPositiveButton("确定", (dialog, which) -> {
                            Intent intent = new Intent(Volunteer_homeActivity.this, OrderDetailActivity.class);
                            intent.putExtra("aid", activity.getId());
                            intent.putExtra("bid", activity.getBid());
                            intent.putExtra("address", activity.getAddress());
                            intent.putExtra("blat", activity.getLat());
                            intent.putExtra("blon", activity.getLon());
                            intent.putExtra("btime", activity.getBtime());
                            intent.putExtra("distance", activity.getDistance());
                            startActivity(intent);
                        })
                        .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                        .show();
                return true;
            }
            return false;
        });
    }


    private void getBlind() {
        progressDialog.show();
        String getUrl = serverURL + "/help/volunteer?lat=" + lat + "&lon=" + lon + "&id=" + User.getUserId();

        new Thread(() -> {
            try {
                // 设定请求超时时间，避免死等
                String response = NetworkHandler.getWithTimeout(getUrl, 5000); // 5秒超时
                if (response != null && !response.isEmpty()) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        BVActivityList = new ArrayList<>();
                        List<BVActivity> activities = Volunteer_helpActivity.JsonParser.parseActivities(response);
                        showActivitiesOnMap(activities);
                        BVActivityList.addAll(activities);
                    });
                } else {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "当前无可帮助订单", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "连接服务器失败，请检查网络！", Toast.LENGTH_SHORT).show();
                });
                Log.e("Volunteer_homeActivity", "网络请求失败：" + e.toString());
            }
        }).start();
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

    @SuppressWarnings("MissingSuperCall")
    @Override
    public void onBackPressed() {
        //禁用返回键
    }

    private void checkStatus() {
        String getUrl = serverURL + "/online/volunteer?vid=" + User.getUserId();

        new Thread(() -> {
            try {
                String check = NetworkHandler.get(getUrl);
                if (check != null && !check.isEmpty()) {
                    runOnUiThread(() -> {
                        try {
                            JSONObject root = JSONObject.parseObject(check);
                            int status = root.getInteger("status") != null ? root.getInteger("status") : -1;
                            JSONObject help = root.getJSONObject("help");
                            String distance = root.getString("distance");
                            String phone = root.getString("phone");
                            switch (status) {
                                case 0:
                                    String servicePathPost = "online";
                                    String jsonBody = "{\"id\":\"" + User.getUserId() + "\", \"status\": \"" + "1" + "\"}";
                                    String postUrl = serverURL + "/" + servicePathPost;
                                    Toast.makeText(this, "当前状态由离线转为在线", Toast.LENGTH_SHORT).show();
                                    // 执行POST请求的异步任务
                                    new NetworkTask("POST", jsonBody).execute(postUrl);
                                    break;
                                case 1:
                                    Toast.makeText(this, "当前状态：在线", Toast.LENGTH_SHORT).show();
                                    break;
                                case 2:
                                    Toast.makeText(this, "当前有进行中的订单", Toast.LENGTH_SHORT).show();
                                    if (help != null) {
                                        // 防止字段缺失报错
                                        String aid = help.getString("aid");
                                        String bid = help.getString("bid");
                                        String address = help.getString("address");
                                        Double blat = help.getDouble("lat");
                                        Double blon = help.getDouble("lon");
                                        String btime = BVActivity.formattedTime(help.getString("btime"));
                                        Log.d("Volunteer_homeActivity", "aid:" + aid + " bid:" + bid + " address:" + address + " blat:" + blat + " blon:" + blon + " btime:" + btime);

                                        if (aid != null && bid != null && address != null) {
                                            Intent intent = new Intent(Volunteer_homeActivity.this, OrderConfirmedActivity.class);
                                            intent.putExtra("aid", aid);
                                            intent.putExtra("bid", bid);
                                            intent.putExtra("address", address);
                                            intent.putExtra("blat", blat);
                                            intent.putExtra("blon", blon);
                                            intent.putExtra("btime", btime);
                                            intent.putExtra("distance", distance);
                                            intent.putExtra("phoneNumber", phone);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(this, "订单信息缺失，请稍后重试", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(this, "服务器未返回订单详情", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                default:
                                    Toast.makeText(this, "未知状态，请稍后重试", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "解析服务器数据失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "服务器无响应，请检查网络", Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "请求服务器失败，请稍后重试", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

}