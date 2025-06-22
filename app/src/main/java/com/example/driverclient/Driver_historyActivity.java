package com.example.driverclient;

import static com.example.tools.Constants.serverURL;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

public class Driver_historyActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TaskAdapter adapter;
    private List<DriverTask> taskList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("历史任务");

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(taskList, this);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::fetchHistoryTasks);

        setupBottomNavigation();

        fetchHistoryTasks();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_history);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_history) {
                return true;
            } else if (id == R.id.nav_task_list) {
                startActivity(new Intent(this, Driver_taskListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_map) {
                startActivity(new Intent(this, Driver_homeActivity.class));
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

    private void fetchHistoryTasks() {
        swipeRefreshLayout.setRefreshing(true);
        String url = serverURL + "/driver/all_work";
        int uid = User.getUserId();

        JSONObject requestBody = new JSONObject();
        requestBody.put("uid", String.valueOf(uid));
        String jsonBody = requestBody.toString();

        new Thread(() -> {
            try {
                String response = NetworkHandler.post(url, jsonBody);
                if (response == null || response.isEmpty()) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "暂无历史任务", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    });
                    return;
                }

                JSONObject responseJson = JSONObject.parseObject(response);
                if (responseJson.getInteger("status_code") != 200) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "获取历史任务失败", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    });
                    return;
                }

                JSONArray tasksArray = responseJson.getJSONArray("tasks");
                List<DriverTask> newTaskList = new ArrayList<>();
                if (tasksArray != null) {
                    for (int i = 0; i < tasksArray.size(); i++) {
                        JSONObject taskJson = tasksArray.getJSONObject(i);
                        DriverTask task = new DriverTask(
                                taskJson.getString("start_id"),
                                taskJson.getString("end_id"),
                                taskJson.getString("date"),
                                taskJson.getIntValue("driver_order")
                        );
                        // 同步获取起终点信息
                        fetchPointDetails(task);
                        newTaskList.add(task);
                    }
                }

                // 排序
                newTaskList.sort((t1, t2) -> {
                    int dateCompare = t2.getDate().compareTo(t1.getDate());
                    if (dateCompare == 0) {
                        return Integer.compare(t1.getDriverOrder(), t2.getDriverOrder());
                    }
                    return dateCompare;
                });

                runOnUiThread(() -> {
                    taskList.clear();
                    taskList.addAll(newTaskList);
                    adapter.notifyDataSetChanged();
                    getSupportActionBar().setTitle("历史任务 (" + taskList.size() + ")");
                    swipeRefreshLayout.setRefreshing(false);
                    if (taskList.isEmpty()) {
                        Toast.makeText(this, "暂无历史任务", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e("Driver_history", "Failed to fetch history", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "获取历史任务失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        }).start();
    }

    private void fetchPointDetails(DriverTask task) {
        // 获取起点信息
        if (task.getStartId() != null) {
            fetchSinglePoint(task.getStartId(), (name, x, y) -> {
                task.setStartPointName(name);
                task.setStartCoords(x, y);
            });
        }
        // 获取终点信息
        if (task.getEndId() != null) {
            fetchSinglePoint(task.getEndId(), (name, x, y) -> {
                task.setEndPointName(name);
                task.setEndCoords(x, y);
            });
        }
    }

    private void fetchSinglePoint(String pid, PointDetailsCallback callback) {
        try {
            String url = serverURL + "/point";
            JSONObject requestBody = new JSONObject();
            requestBody.put("pid", pid);
            String response = NetworkHandler.post(url, requestBody.toString());
            if (response != null && !response.isEmpty()) {
                JSONObject json = JSONObject.parseObject(response);
                if (json.getIntValue("status_code") == 200) {
                    callback.onDetailsFetched(json.getString("name"), json.getDoubleValue("x"), json.getDoubleValue("y"));
                }
            }
        } catch (Exception e) {
            Log.e("Driver_history", "Failed to fetch point details", e);
        }
    }

    interface PointDetailsCallback {
        void onDetailsFetched(String name, double x, double y);
    }
} 