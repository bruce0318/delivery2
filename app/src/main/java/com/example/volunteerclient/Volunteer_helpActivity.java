package com.example.volunteerclient;

import static com.example.tools.Constants.serverURL;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.example.soniceyes.R;
import com.example.tools.NetworkHandler;
import com.example.users.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Volunteer_helpActivity extends AppCompatActivity implements AMapLocationListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MyAdapter adapter;
    private List<BVActivity> BVActivityList;

    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    private double lat = 0.0, lon = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.volunteer_help);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("提供帮助");

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout.setOnRefreshListener(() -> {
            startLocation(); // 下拉时重新定位并刷新列表
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_help);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_help) {
                return true;
            } else if (id == R.id.nav_map) {
                startActivity(new Intent(this, Volunteer_homeActivity.class));
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

        startLocation();
    }

    private void startLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        try {
            mLocationClient = new AMapLocationClient(getApplicationContext());
            mLocationClient.setLocationListener(this);

            mLocationOption = new AMapLocationClientOption();
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setOnceLocation(true);
            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.startLocation();
        } catch (Exception e) {
            e.printStackTrace();
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, "初始化定位失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                lat = amapLocation.getLatitude();
                lon = amapLocation.getLongitude();
                Log.d("Volunteer_helpActivity", "定位成功: " + lat + "," + lon);
                getVolunteerHelpList();
            } else {
                swipeRefreshLayout.setRefreshing(false);
                Log.e("Volunteer_helpActivity", "定位失败：" + amapLocation.getErrorInfo());
                Toast.makeText(this, "定位失败：" + amapLocation.getErrorInfo(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getVolunteerHelpList() {
        swipeRefreshLayout.setRefreshing(true); // 每次都先开始动画
        String getUrl = serverURL + "/help/volunteer?lat=" + lat + "&lon=" + lon + "&id=" + User.getUserId();

        new Thread(() -> {
            try {
                String response = NetworkHandler.getWithTimeout(getUrl, 5000);
                if (response != null && !response.isEmpty()) {
                    runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false); // 成功后停止动画
                        BVActivityList = JsonParser.parseActivities(response);
                        adapter = new MyAdapter(BVActivityList, this);
                        recyclerView.setAdapter(adapter);
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("可帮助总数：" + BVActivityList.size());
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false); // 没有数据也要停止动画
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("当前无可接订单");
                        }
                        Toast.makeText(this, "暂无数据", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false); // 出错也要停止动画！！
                    Toast.makeText(this, "连接服务器失败，请检查网络！", Toast.LENGTH_SHORT).show();
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("网络异常");
                    }
                });
                Log.e("Volunteer_helpActivity", "网络请求出错", e);
            }
        }).start();
    }


    public static class JsonParser {
        public static List<BVActivity> parseActivities(String jsonString) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<BVActivity>>() {}.getType();
            return gson.fromJson(jsonString, listType);
        }
    }

    @SuppressWarnings("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // 禁用返回键
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
    }
}
