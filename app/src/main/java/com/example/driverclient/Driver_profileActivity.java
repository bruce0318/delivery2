package com.example.driverclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.soniceyes.R;
import com.example.users.LoginActivity;
import com.example.users.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Driver_profileActivity extends AppCompatActivity {

    private TextView textviewName, textViewPhone, textViewIdentity;
    private Button buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_profile);

        Toolbar toolbar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("个人信息");
        }

        textviewName = findViewById(R.id.textview_name);
        textViewPhone = findViewById(R.id.textView_phone);
        textViewIdentity = findViewById(R.id.textView_identity);
        buttonLogout = findViewById(R.id.button_logout);

        populateUserInfo();

        buttonLogout.setOnClickListener(v -> {
            logout();
        });

        setupBottomNavigation();
    }

    private void populateUserInfo() {
        // Fetch user info from the static User class
        String userName = User.getUserName() + "司机您好！";
        textviewName.setText(userName);
        textViewPhone.setText(User.getUserPhone());
        textViewIdentity.setText("身份: 司机");
    }

    private void logout() {
        // Clear SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("user_credentials", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("username");
        editor.remove("password");
        editor.apply();

        // Navigate to LoginActivity
        Intent intent = new Intent(Driver_profileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                return true;
            } else if (id == R.id.nav_task_list) {
                startActivity(new Intent(this, Driver_taskListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_map) {
                startActivity(new Intent(this, Driver_homeActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_history) {
                 startActivity(new Intent(this, Driver_historyActivity.class));
                 finish();
                return true;
            }
            return false;
        });
    }
} 