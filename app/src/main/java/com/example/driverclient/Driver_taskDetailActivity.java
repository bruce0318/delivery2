package com.example.driverclient;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Driver_taskDetailActivity extends AppCompatActivity {

    private MapView mapView;
    private AMap aMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_task_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        aMap.getUiSettings().setZoomControlsEnabled(false);

        Intent intent = getIntent();
        String startName = intent.getStringExtra("startName");
        String endName = intent.getStringExtra("endName");
        String dateStr = intent.getStringExtra("date");
        int order = intent.getIntExtra("order", 0);
        double startX = intent.getDoubleExtra("startX", 0);
        double startY = intent.getDoubleExtra("startY", 0);
        double endX = intent.getDoubleExtra("endX", 0);
        double endY = intent.getDoubleExtra("endY", 0);
        
        TextView tvDate = findViewById(R.id.tv_task_date);
        TextView tvOrder = findViewById(R.id.tv_task_order);
        TextView tvStart = findViewById(R.id.tv_start_point);
        TextView tvEnd = findViewById(R.id.tv_end_point);
        Button btnNavigate = findViewById(R.id.btn_navigate);

        // 格式化日期
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

        if (startX != 0 && startY != 0 && endX != 0 && endY != 0) {
            LatLng startPoint = new LatLng(startY, startX);
            LatLng endPoint = new LatLng(endY, endX);

            aMap.addMarker(new MarkerOptions().position(startPoint).title(startName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            aMap.addMarker(new MarkerOptions().position(endPoint).title(endName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(startPoint);
            builder.include(endPoint);
            aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
        }

        btnNavigate.setOnClickListener(v -> {
            if (startX != 0) {
                Intent navIntent = new Intent("android.intent.action.VIEW", Uri.parse("amapuri://route/plan/?dlat=" + startY + "&dlon=" + startX + "&dname=" + startName + "&dev=0&t=0"));
                navIntent.setPackage("com.autonavi.minimap");
                startActivity(navIntent);
            } else {
                Toast.makeText(this, "起点位置信息不正确", Toast.LENGTH_SHORT).show();
            }
        });
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