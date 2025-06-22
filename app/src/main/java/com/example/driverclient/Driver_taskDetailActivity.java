package com.example.driverclient;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MarkerOptions;
import com.example.soniceyes.R;
import com.example.tools.NetworkHandler;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class Driver_taskDetailActivity extends AppCompatActivity {

    private String serverURL = "http://47.111.136.83:8888";
    private MapView mapView;
    private AMap aMap;
    private LatLng startCoord;
    private LatLng endCoord;
    private String startName;
    private String endName;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_task_detail);

        initProgressDialog();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        aMap.getUiSettings().setZoomControlsEnabled(false);

        Intent intent = getIntent();
        String startId = intent.getStringExtra("start_id");
        String endId = intent.getStringExtra("end_id");
        startName = intent.getStringExtra("start_name");
        endName = intent.getStringExtra("end_name");
        String dateStr = intent.getStringExtra("date");
        int order = intent.getIntExtra("order", 0);

        TextView tvDate = findViewById(R.id.tv_task_date);
        TextView tvOrder = findViewById(R.id.tv_task_order);
        TextView tvStart = findViewById(R.id.tv_start_point);
        TextView tvEnd = findViewById(R.id.tv_end_point);
        Button btnNavigateToStart = findViewById(R.id.btn_navigate_to_start);
        Button btnNavigateToEnd = findViewById(R.id.btn_navigate_to_end);
        Button btnNavigateFullRoute = findViewById(R.id.btn_navigate_full_route);

        // Format and set date
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            tvDate.setText("日期: " + outputFormat.format(date));
        } catch (ParseException e) {
            tvDate.setText("日期: " + dateStr);
            e.printStackTrace();
        }

        tvOrder.setText("顺序: " + order);
        tvStart.setText("起点: " + startName);
        tvEnd.setText("终点: " + endName);

        fetchAndProcessCoordinates(startId, endId);
    }

    private void initProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(R.layout.dialog_loading);
        progressDialog = builder.create();
    }

    private void fetchAndProcessCoordinates(String startId, String endId) {
        progressDialog.show();
        new Thread(() -> {
            final CountDownLatch latch = new CountDownLatch(2);

            fetchPoint(startId, (latLng) -> {
                startCoord = latLng;
                latch.countDown();
            });

            fetchPoint(endId, (latLng) -> {
                endCoord = latLng;
                latch.countDown();
            });

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> {
                progressDialog.dismiss();
                addMarkersToMap();
                setupNavigationButtons();
            });
        }).start();
    }

    private void fetchPoint(String pointId, PointCallback callback) {
        new Thread(() -> {
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("pid", pointId);
                String response = NetworkHandler.post(serverURL + "/point", requestBody.toString());
                if (response != null && !response.startsWith("IOException")) {
                    JSONObject pointJson = new JSONObject(response);
                    double x = pointJson.getDouble("x");
                    double y = pointJson.getDouble("y");
                    callback.onResult(new LatLng(y, x));
                } else {
                    callback.onResult(null);
                }
            } catch (JSONException e) {
                Log.e("FetchPoint", "Error parsing point data for id " + pointId, e);
                callback.onResult(null);
            }
        }).start();
    }

    interface PointCallback {
        void onResult(LatLng latLng);
    }

    private void addMarkersToMap() {
        if (startCoord != null && endCoord != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(startCoord);
            aMap.addMarker(new MarkerOptions().position(startCoord).title(startName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            builder.include(endCoord);
            aMap.addMarker(new MarkerOptions().position(endCoord).title(endName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
        } else if (startCoord != null) {
            aMap.addMarker(new MarkerOptions().position(startCoord).title(startName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startCoord, 15));
        } else if (endCoord != null) {
            aMap.addMarker(new MarkerOptions().position(endCoord).title(endName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(endCoord, 15));
        } else {
            Toast.makeText(this, "无法获取有效的坐标点", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupNavigationButtons() {
        Button btnNavigateToStart = findViewById(R.id.btn_navigate_to_start);
        Button btnNavigateToEnd = findViewById(R.id.btn_navigate_to_end);
        Button btnNavigateFullRoute = findViewById(R.id.btn_navigate_full_route);

        btnNavigateToStart.setOnClickListener(v -> {
            if (startCoord != null) {
                String uri = String.format(Locale.US, "amapuri://route/plan/?dlat=%f&dlon=%f&dname=%s&dev=0&t=0", startCoord.latitude, startCoord.longitude, startName);
                Intent intentNav = new Intent("android.intent.action.VIEW", Uri.parse(uri));
                intentNav.setPackage("com.autonavi.minimap");
                startActivity(intentNav);
            } else {
                Toast.makeText(this, "起点坐标无效", Toast.LENGTH_SHORT).show();
            }
        });

        btnNavigateToEnd.setOnClickListener(v -> {
            if (endCoord != null) {
                String uri = String.format(Locale.US, "amapuri://route/plan/?dlat=%f&dlon=%f&dname=%s&dev=0&t=0", endCoord.latitude, endCoord.longitude, endName);
                Intent intentNav = new Intent("android.intent.action.VIEW", Uri.parse(uri));
                intentNav.setPackage("com.autonavi.minimap");
                startActivity(intentNav);
            } else {
                Toast.makeText(this, "终点坐标无效", Toast.LENGTH_SHORT).show();
            }
        });

        btnNavigateFullRoute.setOnClickListener(v -> {
            if (startCoord != null && endCoord != null) {
                String uri = String.format(Locale.US, "amapuri://route/plan/?slat=%f&slon=%f&sname=%s&dlat=%f&dlon=%f&dname=%s&dev=0&t=0", startCoord.latitude, startCoord.longitude, startName, endCoord.latitude, endCoord.longitude, endName);
                Intent intentNav = new Intent("android.intent.action.VIEW", Uri.parse(uri));
                intentNav.setPackage("com.autonavi.minimap");
                startActivity(intentNav);
            } else {
                Toast.makeText(this, "起点或终点坐标无效", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidCoords(double lat, double lon) {
        return lat != 0.0 && lon != 0.0;
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
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
} 