package com.example.managerclient;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.soniceyes.R;
import com.example.tools.NetworkHandler;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Manager_assignActivity extends AppCompatActivity {
    
    private TextView dateTextView;
    private Button selectDateButton;
    private Button assignButton;
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_assign);
        
        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("经理端 - 分配任务");
        }
        
        // 初始化视图
        dateTextView = findViewById(R.id.date_text_view);
        selectDateButton = findViewById(R.id.select_date_button);
        assignButton = findViewById(R.id.assign_button);
        
        // 初始化日期格式
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = Calendar.getInstance();
        
        // 设置当前日期
        updateDateDisplay();
        
        // 设置点击事件
        selectDateButton.setOnClickListener(v -> showDatePicker());
        assignButton.setOnClickListener(v -> assignTasks());
        
        // 设置底部导航
        setupBottomNavigation();
    }
    
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_assign_task);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_add_task) {
                startActivity(new Intent(this, Manager_homeActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_assign_task) {
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, Manager_profileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
    
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateDisplay();
                }
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private void updateDateDisplay() {
        String dateString = dateFormat.format(selectedDate.getTime());
        dateTextView.setText("选择的日期: " + dateString);
    }
    
    private void assignTasks() {
        String selectedDateString = dateFormat.format(selectedDate.getTime());
        
        // 显示加载提示
        Toast.makeText(this, "正在分配任务...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                String url = "http://47.111.136.83:8081/api/tasks/assign?date=" + selectedDateString;
                String response = NetworkHandler.post(url, "date:2025-02-19");
                
                runOnUiThread(() -> {
                    if (response != null && response.toLowerCase().contains("success")) {
                        Toast.makeText(Manager_assignActivity.this, 
                            "任务分配成功！", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(Manager_assignActivity.this, 
                            "任务分配失败: " + response, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(Manager_assignActivity.this, 
                        "网络错误: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
} 