package com.example.managerclient;

import static com.example.tools.Constants.serverURL;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MarkerOptions;
import com.example.soniceyes.R;
import com.example.tools.NetworkHandler;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class Manager_homeActivity extends AppCompatActivity {

    private Spinner startPointSpinner, endPointSpinner;
    private EditText dateInput;
    private Button submitButton;
    private List<Point> allPoints = new ArrayList<>();
    private List<Point> startPoints = new ArrayList<>();
    private List<Point> endPoints = new ArrayList<>();
    private MapView mapView;
    private AMap aMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manager_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("经理端 - 添加任务");
        }

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState); // 此方法必须重写
        initMap();

        startPointSpinner = findViewById(R.id.start_point_spinner);
        endPointSpinner = findViewById(R.id.end_point_spinner);
        dateInput = findViewById(R.id.date_input);
        submitButton = findViewById(R.id.submit_button);

        // 获取所有点位信息
        fetchAllPoints();

        dateInput.setOnClickListener(v -> showDatePickerDialog());

        submitButton.setOnClickListener(v -> {
            String startId = getSelectedPointId(startPointSpinner);
            String endId = getSelectedPointId(endPointSpinner);
            String date = dateInput.getText().toString().trim();

            if (TextUtils.isEmpty(startId) || TextUtils.isEmpty(endId) || TextUtils.isEmpty(date)) {
                Toast.makeText(this, "所有字段均为必填项", Toast.LENGTH_SHORT).show();
                return;
            }

            if (startId.equals(endId)) {
                Toast.makeText(this, "起点和终点不能相同", Toast.LENGTH_SHORT).show();
                return;
            }

            submitTask(startId, endId, date);
        });

        setupBottomNavigation();
    }

    private void initMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
            // 基本的地图设置
            aMap.getUiSettings().setZoomControlsEnabled(false); // 不显示缩放按钮
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(39.9042, 116.4074), 10)); // 默认视角，北京
        }
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                    dateInput.setText(formattedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_add_task);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_add_task) {
                return true;
            } else if (id == R.id.nav_assign_task) {
                startActivity(new Intent(this, Manager_assignActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, Manager_profileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void fetchAllPoints() {
        String url = serverURL + "/all_points";

        new Thread(() -> {
            try {
                String response = NetworkHandler.get(url);
                if (response != null && !response.isEmpty()) {
                    JSONObject responseJson = JSONObject.parseObject(response);
                    if (responseJson.getIntValue("status_code") == 200) {
                        JSONArray pointsArray = responseJson.getJSONArray("points");
                        allPoints.clear();
                        startPoints.clear();
                        endPoints.clear();
                        for (int i = 0; i < pointsArray.size(); i++) {
                            JSONObject pointJson = pointsArray.getJSONObject(i);
                            Point point = new Point(
                                    pointJson.getString("pid"),
                                    pointJson.getString("name"),
                                    pointJson.getString("type"),
                                    pointJson.getDouble("x"),
                                    pointJson.getDouble("y")
                            );
                            allPoints.add(point);
                            // 根据类型分配到不同列表
                            if ("1".equals(point.getType())) {
                                startPoints.add(point);
                            } else if ("2".equals(point.getType())) {
                                endPoints.add(point);
                            }
                        }

                        runOnUiThread(() -> {
                            if (!allPoints.isEmpty()) {
                                setupSpinners();
                                Toast.makeText(this, "已加载 " + startPoints.size() + " 个起点和 " + endPoints.size() + " 个终点", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "未能获取到任何点位信息", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("Manager_homeActivity", "Failed to fetch all points", e);
                runOnUiThread(() -> Toast.makeText(this, "获取点位信息失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void setupSpinners() {
        // 为起点Spinner设置Adapter
        ArrayAdapter<Point> startAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, startPoints);
        startAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startPointSpinner.setAdapter(startAdapter);

        // 为终点Spinner设置Adapter
        ArrayAdapter<Point> endAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, endPoints);
        endAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endPointSpinner.setAdapter(endAdapter);

        android.widget.AdapterView.OnItemSelectedListener listener = new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateMapMarkers();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                aMap.clear();
            }
        };

        startPointSpinner.setOnItemSelectedListener(listener);
        endPointSpinner.setOnItemSelectedListener(listener);
    }

    private void updateMapMarkers() {
        if (aMap == null) return;

        aMap.clear(); // 清除旧的标记

        Point startPoint = (Point) startPointSpinner.getSelectedItem();
        Point endPoint = (Point) endPointSpinner.getSelectedItem();

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boolean hasPoints = false;

        if (startPoint != null && startPoint.getX() != 0 && startPoint.getY() != 0) {
            LatLng startLatLng = new LatLng(startPoint.getY(), startPoint.getX());
            aMap.addMarker(new MarkerOptions()
                    .position(startLatLng)
                    .title("起点: " + startPoint.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            boundsBuilder.include(startLatLng);
            hasPoints = true;
        }

        if (endPoint != null && endPoint.getX() != 0 && endPoint.getY() != 0) {
            LatLng endLatLng = new LatLng(endPoint.getY(), endPoint.getX());
            aMap.addMarker(new MarkerOptions()
                    .position(endLatLng)
                    .title("终点: " + endPoint.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            boundsBuilder.include(endLatLng);
            hasPoints = true;
        }
        
        if (hasPoints) {
            // 如果只有一个点，则将地图中心移动到该点
            if (startPoint != null && startPoint.equals(endPoint)) {
                 LatLng point = new LatLng(startPoint.getY(), startPoint.getX());
                 aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
            } else {
                 // 移动镜头以包含所有标记
                LatLngBounds bounds = boundsBuilder.build();
                aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150)); // 150是padding
            }
        }
    }

    private String getSelectedPointId(Spinner spinner) {
        Point selectedPoint = (Point) spinner.getSelectedItem();
        return selectedPoint != null ? selectedPoint.getId() : "";
    }

    private void submitTask(String startId, String endId, String date) {
        String url = serverURL + "/manager/work";

        JSONObject requestBody = new JSONObject();
        requestBody.put("start_id", startId);
        requestBody.put("end_id", endId);
        requestBody.put("date", date);
        String jsonBody = requestBody.toString();

        new Thread(() -> {
            try {
                String response = NetworkHandler.post(url, jsonBody);
                if (response != null && !response.isEmpty()) {
                    JSONObject responseJson = JSONObject.parseObject(response);
                    if (responseJson.getIntValue("status_code") == 200) {
                        runOnUiThread(() -> {
                            Toast.makeText(Manager_homeActivity.this, "任务提交成功", Toast.LENGTH_SHORT).show();
                            // 清空输入框
                            dateInput.setText("");
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(Manager_homeActivity.this, "任务提交失败", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(Manager_homeActivity.this, "服务器无响应", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(Manager_homeActivity.this, "请求失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                Log.e("ManagerHomeActivity", "Task submission failed", e);
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
} 