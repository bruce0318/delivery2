package com.example.driverclient;

import static com.example.tools.Constants.serverURL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
                 getDailyTasks();
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
                showDriverLocation();
                if (isFirstLocation) {
                    isFirstLocation = false;
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 18));
                    getDailyTasks();
                }
            } else {
                Log.e("Driver_homeActivity", "定位失败：" + amapLocation.getErrorInfo());
                Toast.makeText(this, "定位失败：" + amapLocation.getErrorInfo(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDriverLocation() {
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.locationpurple));
        myLocationStyle.strokeColor(Color.TRANSPARENT);
        myLocationStyle.radiusFillColor(Color.TRANSPARENT);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMyLocationEnabled(true);
        aMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    private void parseAndDrawRoutesAndPoints(String jsonResponse) {
        aMap.clear();
        showDriverLocation();

        try {
            JSONObject response = JSONObject.parseObject(jsonResponse);
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            Map<String, String> pointIdToTypeMap = new HashMap<>();

            // 1. Draw polylines and collect point IDs
            extractRouteData(response.getJSONObject("transporting_geojson"), Color.BLUE, false, boundsBuilder, pointIdToTypeMap);
            extractRouteData(response.getJSONObject("transferring_geojson"), Color.GREEN, true, boundsBuilder, pointIdToTypeMap);

            // 2. Fetch point details and then draw markers
            Set<String> pointIds = pointIdToTypeMap.keySet();
            if (!pointIds.isEmpty()) {
                fetchAndDrawMarkers(pointIds, pointIdToTypeMap, boundsBuilder);
            } else {
                Toast.makeText(this, "今日无任务", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            Log.e("Driver_homeActivity", "Error parsing or drawing routes", e);
            Toast.makeText(this, "解析路线数据时出错", Toast.LENGTH_SHORT).show();
        }
    }

    private void extractRouteData(JSONObject geojson, int color, boolean isDashed, LatLngBounds.Builder boundsBuilder, Map<String, String> pointIdToTypeMap) {
        if (geojson == null) return;
        JSONArray features = geojson.getJSONArray("features");
        if (features == null) return;

        for (int i = 0; i < features.size(); i++) {
            JSONObject feature = features.getJSONObject(i);
            if (feature == null) continue;

            drawPolylineFromFeature(feature, color, isDashed, boundsBuilder);

            JSONObject properties = feature.getJSONObject("properties");
            if (properties != null) {
                String startId = properties.getString("start_id");
                String endId = properties.getString("end_id");
                if (startId != null) {
                    pointIdToTypeMap.putIfAbsent(startId, "start");
                }
                if (endId != null) {
                    pointIdToTypeMap.putIfAbsent(endId, "end");
                }
            }
        }
    }

    private void drawPolylineFromFeature(JSONObject feature, int color, boolean isDashed, LatLngBounds.Builder boundsBuilder) {
        if (feature == null) return;
        JSONObject geometry = feature.getJSONObject("geometry");
        if (geometry == null) return;

        String type = geometry.getString("type");
        JSONArray coordinates = geometry.getJSONArray("coordinates");
        if (coordinates == null) return;

        PolylineOptions baseOptions = new PolylineOptions().width(10).color(color).useGradient(true);
        if (isDashed) {
            baseOptions.setDottedLine(true);
        }

        if ("LineString".equals(type)) {
            PolylineOptions polylineOptions = new PolylineOptions().width(baseOptions.getWidth()).color(baseOptions.getColor()).useGradient(baseOptions.isUseGradient()).setDottedLine(baseOptions.isDottedLine());
            for (int i = 0; i < coordinates.size(); i++) {
                JSONArray coord = coordinates.getJSONArray(i);
                if (coord != null && coord.size() >= 2) {
                    LatLng point = new LatLng(coord.getDouble(1), coord.getDouble(0));
                    polylineOptions.add(point);
                    boundsBuilder.include(point);
                }
            }
            aMap.addPolyline(polylineOptions);
        } else if ("MultiLineString".equals(type)) {
            for (int i = 0; i < coordinates.size(); i++) {
                PolylineOptions polylineOptions = new PolylineOptions().width(baseOptions.getWidth()).color(baseOptions.getColor()).useGradient(baseOptions.isUseGradient()).setDottedLine(baseOptions.isDottedLine());
                JSONArray lineString = coordinates.getJSONArray(i);
                if (lineString == null) continue;
                for (int j = 0; j < lineString.size(); j++) {
                    JSONArray coord = lineString.getJSONArray(j);
                    if (coord != null && coord.size() >= 2) {
                        LatLng point = new LatLng(coord.getDouble(1), coord.getDouble(0));
                        polylineOptions.add(point);
                        boundsBuilder.include(point);
                    }
                }
                aMap.addPolyline(polylineOptions);
            }
        }
    }

    private void fetchAndDrawMarkers(Set<String> pointIds, Map<String, String> pointIdToTypeMap, LatLngBounds.Builder boundsBuilder) {
        taskPoints.clear();
        AtomicInteger pendingRequests = new AtomicInteger(pointIds.size());

        for (String pid : pointIds) {
            String url = serverURL + "/point";
            JSONObject requestBody = new JSONObject();
            requestBody.put("pid", pid);
            String jsonBody = requestBody.toString();

            new Thread(() -> {
                try {
                    String response = NetworkHandler.post(url, jsonBody);
                    if (response != null && !response.startsWith("IOException")) {
                        JSONObject pointJson = JSONObject.parseObject(response);
                        if (pointJson != null && pointJson.containsKey("name")) {
                            taskPoints.add(new TaskPoint(
                                    pid,
                                    pointJson.getString("name"),
                                    pointJson.getDoubleValue("x"),
                                    pointJson.getDoubleValue("y"),
                                    pointIdToTypeMap.getOrDefault(pid, "middle")
                            ));
                        } else {
                             Log.w("Driver_homeActivity", "Point data for pid: " + pid + " is invalid: " + response);
                        }
                    }
                } catch (Exception e) {
                    Log.e("Driver_homeActivity", "Failed to get point data for pid: " + pid, e);
                } finally {
                    if (pendingRequests.decrementAndGet() == 0) {
                        runOnUiThread(() -> {
                            drawTaskPointsOnMap();
                            if (!taskPoints.isEmpty()) {
                                aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 150));
                            }
                        });
                    }
                }
            }).start();
        }
    }

    private void drawTaskPointsOnMap() {
        for (TaskPoint point : taskPoints) {
            LatLng latLng = new LatLng(point.y, point.x);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(point.name);

            if ("start".equals(point.type)) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_outline));
            } else if ("end".equals(point.type)) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_blind_location));
            }
            // For "middle" points or if type is not specified, default icon will be used.

            Marker marker = aMap.addMarker(markerOptions);
            marker.setObject(point);
        }
    }

    private void getDailyTasks() {
        progressDialog.show();

        String url = serverURL + "/driver/work";
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int uid = User.getUserId();

        JSONObject requestBody = new JSONObject();
        requestBody.put("uid", uid);
        requestBody.put("date", date);
        String jsonBody = requestBody.toString();

        new Thread(() -> {
            try {
                String response = NetworkHandler.post(url, jsonBody);
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                    if (response != null && !response.startsWith("IOException")) {
                        try {
                            JSONObject jsonResponse = JSONObject.parseObject(response);
                            if (jsonResponse.getIntValue("status_code") == 200) {
                                parseAndDrawRoutesAndPoints(response);
                            } else {
                                Toast.makeText(Driver_homeActivity.this, "获取任务失败: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("Driver_homeActivity", "Failed to parse daily tasks", e);
                            Toast.makeText(Driver_homeActivity.this, "解析任务数据时出错", Toast.LENGTH_SHORT).show();
                        }
                } else {
                        Toast.makeText(Driver_homeActivity.this, "网络请求失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                    }
                    });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Log.e("Driver_homeActivity", "Failed to get daily tasks", e);
                    Toast.makeText(Driver_homeActivity.this, "网络请求异常", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
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
