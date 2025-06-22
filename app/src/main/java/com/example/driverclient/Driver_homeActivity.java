package com.example.driverclient;

import static com.example.tools.Constants.serverURL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.example.soniceyes.R;
import com.example.tools.NetworkHandler;
import com.example.users.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Driver_homeActivity extends AppCompatActivity implements AMapLocationListener {

    private FloatingActionButton buttonRefresh, buttonLocate;
    private Double lat = 0.0, lon = 0.0;
    private final List<TaskPoint> taskPoints = Collections.synchronizedList(new ArrayList<>());

    private MapView mapView;
    private AMap aMap;

    private AMapLocationClient mLocationClient;

    private AlertDialog progressDialog;
    private boolean isFirstLocation = true;
    private String uid;
    private Polyline mPolyline;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_home);

        MapsInitializer.updatePrivacyShow(this, true, true);
        MapsInitializer.updatePrivacyAgree(this, true);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.setMinZoomLevel(10);
        if (aMap == null) {
            Log.e("Driver_homeActivity", "AMap initialization failed");
            return;
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        buttonRefresh = findViewById(R.id.button_refresh);
        buttonLocate = findViewById(R.id.button_locate);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("今日任务");
                Drawable logo = ContextCompat.getDrawable(this, R.mipmap.eyewhite);
                if (logo != null) {
                    int width = (int) (logo.getIntrinsicWidth() * 0.6);
                    int height = (int) (logo.getIntrinsicHeight() * 0.6);
                    logo.setBounds(0, 0, width, height);
                    toolbar.setLogo(logo);
                }
            }
        } else {
            Log.e("Driver_homeActivity", "Toolbar is not found in the layout");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(R.layout.dialog_loading);
        progressDialog = builder.create();
        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        bottomNavigationView.setSelectedItemId(R.id.nav_map);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_task_list) {
                 startActivity(new Intent(this, Driver_taskListActivity.class));
                 finish();
                return true;
            } else if (id == R.id.nav_map) {
                return true;
            } else if (id == R.id.nav_history) {
                 startActivity(new Intent(this, Driver_historyActivity.class));
                 finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, Driver_profileActivity.class));
                finish();
                return true;
            }
            return false;
        });

        buttonRefresh.setOnClickListener(view -> {
            buttonRefresh.animate().rotationBy(360).setDuration(500).start();
            Toast.makeText(Driver_homeActivity.this, "刷新任务", Toast.LENGTH_SHORT).show();
            if (lat != 0.0 && lon != 0.0) {
                 aMap.clear();
                 showDriverLocation();
                 getRoutesAndDraw();
            }
        });

        buttonLocate.setOnClickListener(view -> {
            if (lat != 0.0 && lon != 0.0) {
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 18));
                Toast.makeText(Driver_homeActivity.this, "已定位到当前位置", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Driver_homeActivity.this, "定位中，请稍后...", Toast.LENGTH_SHORT).show();
            }
        });

        aMap.setOnMarkerClickListener(marker -> {
            Object obj = marker.getObject();
            if (obj instanceof TaskPoint) {
                TaskPoint clickedPoint = (TaskPoint) obj;
                new AlertDialog.Builder(Driver_homeActivity.this)
                        .setTitle("操作")
                        .setMessage("您想对 " + clickedPoint.name + " 做什么？")
                        .setPositiveButton("开始导航", (dialog, which) -> {
                            try {
                                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("amapuri://route/plan/?dlat=" + clickedPoint.y + "&dlon=" + clickedPoint.x + "&dname=" + clickedPoint.name + "&dev=0&t=0"));
                                intent.setPackage("com.autonavi.minimap");
                                startActivity(intent);
                            } catch (Exception e) {
                                Log.e("Driver_homeActivity", "Could not start navigation", e);
                                Toast.makeText(Driver_homeActivity.this, "无法启动高德地图导航", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                return true;
            }
            return false;
        });

        init();
    }

    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }

        //设置地图语言
        aMap.setMapLanguage(AMap.CHINESE);

        // 获取uid
        SharedPreferences sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
        uid = sharedPreferences.getString("uid", "3");

        startLocation();
    }

    private void startLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return; // Return and wait for permission result
        }

        try {
            mLocationClient = new AMapLocationClient(getApplicationContext());
            mLocationClient.setLocationListener(this);
            AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setOnceLocation(false);
            mLocationOption.setInterval(2000);
            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.startLocation();
        } catch (Exception e) {
            Log.e("Driver_homeActivity", "Init location client failed.", e);
            Toast.makeText(this, "初始化定位失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                lat = amapLocation.getLatitude();
                lon = amapLocation.getLongitude();
                Log.d("Driver_homeActivity", "定位成功: " + lat + "," + lon);
                if (isFirstLocation) {
                    showDriverLocation();
                    isFirstLocation = false;
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 15));
                    getRoutesAndDraw();
                }
            } else {
                Log.e("Driver_homeActivity", "定位失败：" + amapLocation.getErrorInfo());
            }
        }
    }

    private void showDriverLocation() {
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        myLocationStyle.interval(2000);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(false);
        aMap.setMyLocationEnabled(true);
    }

    private void getRoutesAndDraw() {
        progressDialog.show();
        aMap.clear();
        showDriverLocation();
        final LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        new Thread(() -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String currentDate = sdf.format(new Date());

                JSONObject requestBody = new JSONObject();
                requestBody.put("uid", uid);
                requestBody.put("date", currentDate);

                String json = NetworkHandler.post(serverURL + "/admin", requestBody.toString());
                Log.d("DriverHome", "Response from /admin: " + json);

                if (json != null && !json.startsWith("IOException")) {
                    JSONObject jsonObject = JSON.parseObject(json);
                    if (jsonObject.getIntValue("status_code") == 200) {
                        if (jsonObject.containsKey("transporting_geojson")) {
                            JSONObject transportingGeojson = jsonObject.getJSONObject("transporting_geojson");
                            parseAndDrawGeoJson(transportingGeojson, false, boundsBuilder); // a solid line
                        }

                        if (jsonObject.containsKey("transferring_geojson")) {
                            JSONObject transferringGeojson = jsonObject.getJSONObject("transferring_geojson");
                            parseAndDrawGeoJson(transferringGeojson, true, boundsBuilder); // a dotted line
                        }

                        runOnUiThread(() -> {
                            try {
                                if (boundsBuilder.build().northeast != null) {
                                     aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 150));
                                }
                            } catch (IllegalStateException e) {
                                Log.e("DriverHome", "No points to build bounds", e);
                            }
                        });

                    } else {
                        String message = jsonObject.getString("message");
                        runOnUiThread(() -> Toast.makeText(Driver_homeActivity.this, "获取路线失败: " + message, Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(Driver_homeActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("DriverHome", "Error getting routes", e);
                runOnUiThread(() -> Toast.makeText(Driver_homeActivity.this, "获取路线时发生错误", Toast.LENGTH_SHORT).show());
            } finally {
                runOnUiThread(() -> progressDialog.dismiss());
            }
        }).start();
    }

    private void parseAndDrawGeoJson(final JSONObject geojson, final boolean isDotted, final LatLngBounds.Builder boundsBuilder) {
        if (geojson == null) return;
        try {
            final JSONArray features = geojson.getJSONArray("features");
            if (features == null || features.isEmpty()) return;

            for (int i = 0; i < features.size(); i++) {
                JSONObject feature = features.getJSONObject(i);
                JSONObject geometry = feature.getJSONObject("geometry");
                JSONArray multiLineString = geometry.getJSONArray("coordinates");
                final List<LatLng> allFeaturePoints = new ArrayList<>();

                for (int j = 0; j < multiLineString.size(); j++) {
                    JSONArray lineString = multiLineString.getJSONArray(j);
                    final List<LatLng> points = new ArrayList<>();
                    for (int k = 0; k < lineString.size(); k++) {
                        JSONArray coordinates = lineString.getJSONArray(k);
                        double longitude = coordinates.getDoubleValue(0);
                        double latitude = coordinates.getDoubleValue(1);
                        LatLng point = new LatLng(latitude, longitude);
                        points.add(point);
                        boundsBuilder.include(point);
                    }
                    allFeaturePoints.addAll(points);
                    runOnUiThread(() -> {
                        if (!points.isEmpty()) {
                            PolylineOptions polylineOptions = new PolylineOptions()
                                    .addAll(points)
                                    .width(10)
                                    .color(isDotted ? Color.RED : Color.BLUE)
                                    .setDottedLine(isDotted);
                            aMap.addPolyline(polylineOptions);
                        }
                    });
                }

                runOnUiThread(() -> {
                    if(!allFeaturePoints.isEmpty()){
                        addMarker(allFeaturePoints.get(0), "起点", R.drawable.locationpink);
                        addMarker(allFeaturePoints.get(allFeaturePoints.size() - 1), "终点", R.drawable.location_button);
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("DriverHome", "Error parsing GeoJSON", e);
        }
    }

    private void addMarker(LatLng position, String title, int iconResId) {
        aMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
                .icon(BitmapDescriptorFactory.fromResource(iconResId))
        );
    }

    static class TaskPoint {
        String id;
        String name;
        double x;
        double y;
        String type;

        public TaskPoint(String id, String name, double x, double y, String type) {
            this.id = id;
            this.name = name;
            this.x = x;
            this.y = y;
            this.type = type;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
            mLocationClient = null;
        }
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

    @SuppressWarnings("MissingSuperCall")
    @Override
    public void onBackPressed() {
        //禁用返回键
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocation(); // Permission granted, start location
            } else {
                Toast.makeText(this, "需要定位权限才能显示地图和任务", Toast.LENGTH_LONG).show();
            }
        }
    }
}
