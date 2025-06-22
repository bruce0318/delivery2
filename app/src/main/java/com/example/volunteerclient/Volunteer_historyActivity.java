package com.example.volunteerclient;

import static com.example.tools.Constants.serverURL;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout; // 加入下拉刷新

import com.example.soniceyes.R;
import com.example.tools.NetworkHandler;
import com.example.users.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;





public class Volunteer_historyActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<BVActivity> BVActivityList;
    private SwipeRefreshLayout swipeRefreshLayout; // 新加：SwipeRefreshLayout
    private ImageView badgeIcon;
    private TextView badgeText;
    private ProgressBar badgeProgress;
    private TextView progressHint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.volunteer_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout); // 绑定SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::getVolunteerHistoryList); // 下拉刷新时调用查询

        badgeIcon = findViewById(R.id.badge_icon);
        badgeText = findViewById(R.id.badge_text);
        badgeProgress = findViewById(R.id.badge_progress);
        progressHint = findViewById(R.id.progress_hint);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_history);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_help) {
                startActivity(new Intent(this, Volunteer_helpActivity.class));
                return true;
            } else if (id == R.id.nav_map) {
                startActivity(new Intent(this, Volunteer_homeActivity.class));
                return true;
            } else if (id == R.id.nav_history) {
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, Vedit_informationActivity.class));
                return true;
            }
            return false;
        });

        getVolunteerHistoryList(); // 初次加载
    }

    private void getVolunteerHistoryList() {
        swipeRefreshLayout.setRefreshing(true); // 开始刷新动画
        String getUrl = serverURL + "/help/volunteerhistory?vid=" + User.getUserId();

        new Thread(() -> {
            try {
                String history = NetworkHandler.getWithTimeout(getUrl, 5000); // 5秒超时
                runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false)); // 不管结果如何都先关闭刷新动画

                if (history != null && !history.isEmpty()) {
                    runOnUiThread(() -> {
                        BVActivityList = JsonParser.parseActivities(history);
                        adapter = new HistoryAdapter(BVActivityList, this);
                        recyclerView.setAdapter(adapter);

                        int count = BVActivityList.size();
                        Toast.makeText(this, "太棒了！你一共帮助了" + count + "人！", Toast.LENGTH_SHORT).show();
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("太棒了！你一共帮助了" + count + "人！");
                        }

                        updateBadgeProgress(count);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "暂无历史记录", Toast.LENGTH_SHORT).show();
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("暂无记录");
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false); // 异常也要关闭动画
                    Toast.makeText(this, "连接服务器失败，请检查网络！", Toast.LENGTH_SHORT).show();
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("网络异常");
                    }
                });
                Log.e("Volunteer_historyActivity", "网络请求出错", e);
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

    private void updateBadgeProgress(int count) {
        String badgeName;
        int nextThreshold;
        int progress;
        int badgeResId;

        if (count >= 100) {
            badgeName = "金牌志愿者";
            nextThreshold = 100;
            badgeResId = R.drawable.gold; // 需要准备这个图标
        } else if (count >= 50) {
            badgeName = "银牌志愿者";
            nextThreshold = 100;
            badgeResId = R.drawable.silver;
        } else if (count >= 10) {
            badgeName = "铜牌志愿者";
            nextThreshold = 50;
            badgeResId = R.drawable.bronze;
        } else {
            badgeName = "新手志愿者";
            nextThreshold = 10;
            badgeResId = R.drawable.greenhand;
        }

        badgeText.setText("徽章：" + badgeName);
        badgeIcon.setImageResource(badgeResId);
        badgeProgress.setMax(nextThreshold);
        badgeProgress.setProgress(Math.min(count, nextThreshold));

        int remain = nextThreshold - count;
        if (remain > 0) {
            progressHint.setText("还差 " + remain + " 次升级");
        } else {
            progressHint.setText("已达到最高等级！");
        }
    }

}
