package com.example.driverclient;

import static com.example.tools.Constants.serverURL;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.soniceyes.R;
import com.example.tools.NetworkHandler;
import com.example.users.User;
import com.example.volunteerclient.Vedit_informationActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Driver_taskListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TaskAdapter adapter;
    private final List<DriverTask> taskList = new ArrayList<>();
    private final Map<String, PointDetails> pointDetailsCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_task_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("今日任务列表");

        TextView tvDateHeader = findViewById(R.id.tv_date_header);
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        tvDateHeader.setText("今日任务: " + date);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(taskList, this);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::fetchTodayTasks);

        setupBottomNavigation();

        // Initial fetch
        fetchTodayTasks();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_task_list);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_task_list) {
                return true;
            } else if (id == R.id.nav_map) {
                startActivity(new Intent(this, Driver_homeActivity.class));
                finish();
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
    }

    private void fetchTodayTasks() {
        swipeRefreshLayout.setRefreshing(true);
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
                if (response == null || response.isEmpty() || response.startsWith("IOException")) {
                    handleFetchError("网络请求失败或无数据");
                    return;
                }

                JSONObject responseJson = JSONObject.parseObject(response);
                if (responseJson.getInteger("status_code") != 200) {
                    handleFetchError("获取任务失败: " + responseJson.getString("message"));
                    return;
                }

                List<DriverTask> newTaskList = new ArrayList<>();
                Map<String, PointDetailsCallback> pendingDetails = new HashMap<>();

                // Parse transporting tasks
                parseTasksFromGeoJson(responseJson.getJSONObject("transporting_geojson"), newTaskList, pendingDetails, date);
                // Parse transferring tasks
                parseTasksFromGeoJson(responseJson.getJSONObject("transferring_geojson"), newTaskList, pendingDetails, date);

                if (pendingDetails.isEmpty()) {
                    updateTaskList(newTaskList);
                } else {
                    fetchAllPointDetails(pendingDetails, newTaskList);
                }

            } catch (Exception e) {
                Log.e("Driver_taskList", "Failed to fetch tasks", e);
                handleFetchError("解析任务数据时出错");
            }
        }).start();
    }

    private void parseTasksFromGeoJson(JSONObject geojson, List<DriverTask> newTaskList, Map<String, PointDetailsCallback> pendingDetails, String date) {
        if (geojson == null) return;
        JSONArray features = geojson.getJSONArray("features");
        if (features == null) return;

        for (int i = 0; i < features.size(); i++) {
            JSONObject feature = features.getJSONObject(i);
            JSONObject properties = feature.getJSONObject("properties");
            if (properties == null) {
                Log.w("Driver_taskList", "Feature with null properties: " + feature.toJSONString());
                continue;
            }

            String startId = properties.getString("start_id");
            String endId = properties.getString("end_id");
            int order = properties.getIntValue("driver_order");

            DriverTask task = new DriverTask(startId, endId, date, order);
            newTaskList.add(task);

            if (startId != null && !pointDetailsCache.containsKey(startId)) {
                pendingDetails.put(startId, details -> task.setStartPointName(details.name));
            } else if (startId != null) {
                task.setStartPointName(pointDetailsCache.get(startId).name);
            }

            if (endId != null && !pointDetailsCache.containsKey(endId)) {
                pendingDetails.put(endId, details -> task.setEndPointName(details.name));
            } else if (endId != null) {
                task.setEndPointName(pointDetailsCache.get(endId).name);
            }
        }
    }

    private void handleFetchError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            // Optionally clear list if error means no data
            if (!"网络请求失败或无数据".equals(message)) {
                 updateTaskList(new ArrayList<>());
            }
        });
    }

    private void updateTaskList(List<DriverTask> newTasks) {
        runOnUiThread(() -> {
            newTasks.sort((t1, t2) -> Integer.compare(t1.getDriverOrder(), t2.getDriverOrder()));
            taskList.clear();
            taskList.addAll(newTasks);
            adapter.notifyDataSetChanged();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("今日任务 (" + taskList.size() + ")");
            }
            if (taskList.isEmpty()) {
                Toast.makeText(this, "今日无任务", Toast.LENGTH_SHORT).show();
            }
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void fetchAllPointDetails(Map<String, PointDetailsCallback> pendingDetails, List<DriverTask> newTaskList) {
        AtomicInteger counter = new AtomicInteger(pendingDetails.size());
        if (counter.get() == 0) {
            updateTaskList(newTaskList);
            return;
        }
        for (Map.Entry<String, PointDetailsCallback> entry : pendingDetails.entrySet()) {
            String pid = entry.getKey();
            PointDetailsCallback callback = entry.getValue();

            new Thread(() -> {
                try {
                    String url = serverURL + "/point";
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("pid", pid);
                    String response = NetworkHandler.post(url, requestBody.toString());
                    if (response != null && !response.isEmpty() && !response.startsWith("IOException")) {
                        JSONObject json = JSONObject.parseObject(response);
                        if (json != null && json.containsKey("name")) {
                            PointDetails details = new PointDetails(json.getString("name"));
                            pointDetailsCache.put(pid, details);
                            callback.onDetailsFetched(details);
                        }
                    }
                } catch (Exception e) {
                    Log.e("Driver_taskList", "Failed to fetch point details for pid: " + pid, e);
                } finally {
                    if (counter.decrementAndGet() == 0) {
                        // All details fetched (or failed), now update the entire list
                        runOnUiThread(() -> updateTaskList(newTaskList));
                    }
                }
            }).start();
        }
    }

    // Helper interface and class for fetching point details
    interface PointDetailsCallback {
        void onDetailsFetched(PointDetails details);
    }

    static class PointDetails {
        String name;
        PointDetails(String name) {
            this.name = name;
        }
    }
} 