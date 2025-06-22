package com.example.driverclient;

import static com.example.tools.Constants.serverURL;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;
import android.app.DatePickerDialog;
import java.util.Calendar;
import androidx.appcompat.app.AlertDialog;
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
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Driver_historyActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TaskAdapter adapter;
    private List<DriverTask> taskList = new ArrayList<>();
    private TextView tvTaskStats;
    private Button btnSelectDate;
    private String selectedDate;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_history);
        initProgressDialog();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("历史任务");

        tvTaskStats = findViewById(R.id.tv_task_stats);
        btnSelectDate = findViewById(R.id.btn_select_date);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(taskList, this);
        recyclerView.setAdapter(adapter);

        // Set current date as default
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = sdf.format(new java.util.Date());
        btnSelectDate.setText(selectedDate);

        swipeRefreshLayout.setOnRefreshListener(() -> fetchHistoryTasks(selectedDate));
        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());

        setupBottomNavigation();

        // Initial fetch for today
        fetchHistoryTasks(selectedDate);
    }

    private void initProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(R.layout.dialog_loading);
        progressDialog = builder.create();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year1, monthOfYear, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedDate = sdf.format(newDate.getTime());
                    btnSelectDate.setText(selectedDate);
                    fetchHistoryTasks(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
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

    private void fetchHistoryTasks(String date) {
        progressDialog.show();
        swipeRefreshLayout.setRefreshing(true);
        String url = serverURL + "/driver/all_work";
        int uid = User.getUserId();

        JSONObject requestBody = new JSONObject();
        requestBody.put("uid", String.valueOf(uid));
        // The /all_work endpoint doesn't need a date, filtering will be done client-side
        String jsonBody = requestBody.toString();

        new Thread(() -> {
            try {
                String response = NetworkHandler.post(url, jsonBody);
                if (response == null || response.isEmpty() || response.startsWith("IOException")) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "获取历史任务失败或网络错误", Toast.LENGTH_SHORT).show();
                        updateTaskList(new ArrayList<>());
                    });
                    return;
                }

                JSONObject responseJson = JSONObject.parseObject(response);
                if (responseJson.getInteger("status_code") != 200) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "获取历史任务失败: " + responseJson.getString("message"), Toast.LENGTH_SHORT).show();
                        updateTaskList(new ArrayList<>());
                    });
                    return;
                }

                JSONArray tasksArray = responseJson.getJSONArray("tasks");
                List<DriverTask> allTasks = new ArrayList<>();
                if (tasksArray != null) {
                    CountDownLatch latch = new CountDownLatch(tasksArray.size());
                    for (int i = 0; i < tasksArray.size(); i++) {
                        JSONObject taskJson = tasksArray.getJSONObject(i);
                        DriverTask task = new DriverTask(
                                taskJson.getString("task_id"), // task_id may be null, which is fine
                                String.valueOf(taskJson.getInteger("start_id")),
                                String.valueOf(taskJson.getInteger("end_id")),
                                null, null, // Names will be fetched
                                taskJson.getString("date"),
                                taskJson.getIntValue("driver_order"),
                                0, 0, 0, 0 // Coords will be fetched
                        );
                        allTasks.add(task);
                        fetchPointDetails(task, latch);
                    }
                    latch.await(); // Wait for all point details to be fetched
                }
                
                // Filter tasks by the selected date
                List<DriverTask> filteredTasks = new ArrayList<>();
                for(DriverTask task : allTasks){
                    // The date format from API is "yyyy-MM-dd", same as our selected date string.
                    // A simple string comparison is enough.
                    if(date.equals(task.getDate())){
                        filteredTasks.add(task);
                    }
                }

                // Sort the filtered list
                filteredTasks.sort((t1, t2) -> Integer.compare(t1.getDriverOrder(), t2.getDriverOrder()));

                runOnUiThread(() -> updateTaskList(filteredTasks));

            } catch (Exception e) {
                Log.e("Driver_history", "Failed to fetch history", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "处理历史任务数据失败", Toast.LENGTH_SHORT).show();
                    updateTaskList(new ArrayList<>());
                });
            } finally {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        }).start();
    }
    
    private void updateTaskList(List<DriverTask> tasks) {
        taskList.clear();
        taskList.addAll(tasks);
        adapter.notifyDataSetChanged();
        tvTaskStats.setText("当日任务总数: " + taskList.size());
        if (taskList.isEmpty()) {
            Toast.makeText(this, "所选日期无历史任务", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchPointDetails(DriverTask task, CountDownLatch latch) {
        AtomicInteger pointLatch = new AtomicInteger(2);
        
        // Fetch start point
        fetchSinglePoint(task.getStartId(), (name, x, y) -> {
            task.setStartPointName(name);
            task.setStartCoords(x, y);
            if(pointLatch.decrementAndGet() == 0) latch.countDown();
        });
        
        // Fetch end point
        fetchSinglePoint(task.getEndId(), (name, x, y) -> {
            task.setEndPointName(name);
            task.setEndCoords(x, y);
            if(pointLatch.decrementAndGet() == 0) latch.countDown();
        });
    }

    private void fetchSinglePoint(String pid, PointDetailsCallback callback) {
        new Thread(() -> {
            try {
                String url = serverURL + "/point";
                JSONObject requestBody = new JSONObject();
                requestBody.put("pid", pid);
                String response = NetworkHandler.post(url, requestBody.toString());
                if (response != null && !response.startsWith("IOException")) {
                    JSONObject json = JSONObject.parseObject(response);
                    if (json.getIntValue("status_code") == 200) {
                        callback.onDetailsFetched(json.getString("name"), json.getDoubleValue("x"), json.getDoubleValue("y"));
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e("Driver_history", "Failed to fetch point details for pid: " + pid, e);
            }
            callback.onDetailsFetched("未知", 0, 0);
        }).start();
    }

    interface PointDetailsCallback {
        void onDetailsFetched(String name, double x, double y);
    }
} 