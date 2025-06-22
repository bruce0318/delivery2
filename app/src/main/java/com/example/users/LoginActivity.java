package com.example.users;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSONObject;
import com.example.blindclient.Blind_homeActivity;
import com.example.driverclient.Driver_homeActivity;
import com.example.managerclient.Manager_homeActivity;
import com.example.relativeclient.BindingActivity;
import com.example.relativeclient.Mythread.MythreadGet;
import com.example.relativeclient.Mythread.MythreadGetbind;
import com.example.relativeclient.Mythread.MythreadGetrelation;
import com.example.soniceyes.R;
import com.example.relativeclient.Relative_homeActivity;
import com.example.tools.NetworkHandler;



import java.io.File;
import java.util.List;

import static com.example.tools.Constants.serverURL;

public class LoginActivity extends Activity {
    @SuppressLint("WrongViewCast")
    public static EditText user, pwo;
    Button button5, buttonR;
    TextView tvC;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.defaultblue));
        }
        initView();

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = user.getText().toString().trim();
                String password = pwo.getText().toString().trim();

                if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "请输入电话号码和密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                login(phoneNumber, password);
            }
        });

        // 自动登录
        Auto_Login();
    }
    
    private void login(String phoneNumber, String password) {
        String url = serverURL + "/user/login";

        JSONObject requestBody = new JSONObject();
        requestBody.put("phoneNumber", phoneNumber);
        requestBody.put("password", password);
        String jsonBody = requestBody.toString();

        new Thread(() -> {
            try {
                String response = NetworkHandler.post(url, jsonBody);
                if (response != null && !response.isEmpty()) {
                    JSONObject responseJson = JSONObject.parseObject(response);
                    int rank = responseJson.getIntValue("rank");
                    int uid = responseJson.getIntValue("uid");
                    // 登录成功后，在UI线程中处理跳转
                    runOnUiThread(() -> {
                        // 可以将用户信息（如电话号码作为ID）保存在User类或SharedPreferences中
                        User.setUserId(uid);
                        User.setUserPhone(phoneNumber);
                        User.setROLE(String.valueOf(rank));
                        
                        // 也可以在这里保存凭据用于自动登录
                        saveCredentials(phoneNumber, password);
                        
                        switch (rank) {
                            case 1: // 经理
                                Toast.makeText(LoginActivity.this, "经理登录成功", Toast.LENGTH_SHORT).show();
                                Intent managerIntent = new Intent(LoginActivity.this, Manager_homeActivity.class);
                                startActivity(managerIntent);
                                finish();
                                break;
                            case 2: // 司机
                                Toast.makeText(LoginActivity.this, "司机登录成功", Toast.LENGTH_SHORT).show();
                                Intent driverIntent = new Intent(LoginActivity.this, Driver_homeActivity.class);
                                startActivity(driverIntent);
                                finish();
                                break;
                            default: // rank = -1 或其他值
                                Toast.makeText(LoginActivity.this, "登录失败，请检查账号或密码", Toast.LENGTH_SHORT).show();
                                Log.d("LoginActivity", response);
                                break;
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "登录失败，服务器无响应", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "登录请求失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                Log.e("LoginActivity", "Login request failed", e);
            }
        }).start();
    }


    @SuppressLint("WrongViewCast")
    private void initView() {
        user = findViewById(R.id.user);
        pwo = findViewById(R.id.pwo);
//        tvC = findViewById(R.id.tvRC);
        button5 = findViewById(R.id.button5);
        //点击注册跳转
        buttonR = findViewById(R.id.buttonR);
        buttonR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 如果注册页面也需要修改，可以在这里跳转
                // finish();
                // Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                // startActivity(intent);
                Toast.makeText(LoginActivity.this, "注册功能暂未开放", Toast.LENGTH_SHORT).show();
            }
        });

    }


    //保存账号数据
    private void saveCredentials(String username, String password) {
        SharedPreferences sharedPref = getSharedPreferences("user_credentials", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();
    }

    //删除保存的账号数据
    private void removeCredentials(){
        SharedPreferences sharedPref = getSharedPreferences("user_credentials", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("username");
        editor.remove("password");
        editor.apply();
    }

    //实现自动登录
    private void Auto_Login() {
        SharedPreferences sharedPref = getSharedPreferences("user_credentials", Context.MODE_PRIVATE);
        String phoneNumber = sharedPref.getString("username", null);
        String password = sharedPref.getString("password", null);

        if (phoneNumber != null && password != null) {
            Toast.makeText(this, "正在自动登录...", Toast.LENGTH_SHORT).show();
            user.setText(phoneNumber);
            pwo.setText(password);
            login(phoneNumber, password);
        }
    }

    private long firstTimeBackPressed = 0;
    private static final int BACK_KEY_INTERVAL = 2000; // 两次按键的时间间隔
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - firstTimeBackPressed < BACK_KEY_INTERVAL) {
            // 第二次按下返回键，退出应用
            super.onBackPressed();
            ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.AppTask> appTaskList = activityManager.getAppTasks();
            for (ActivityManager.AppTask appTask : appTaskList) {
                appTask.finishAndRemoveTask();
            }

        } else {
            // 第一次按下返回键，提示用户
            firstTimeBackPressed = System.currentTimeMillis();
            Toast.makeText(LoginActivity.this,
                    "再次点击返回退出应用", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 重置第一次按下返回键的时间
                    firstTimeBackPressed = 0;
                }
            }, BACK_KEY_INTERVAL);
        }
    }

    private void requestPermissions() { //Android6.0以上设备设置动态权限
        if (Build.VERSION.SDK_INT >= 23) {
            // 检查是否拥有权限
            String[] permissions = {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.RECORD_AUDIO,
            };
            String permission = null;
            int id = 0;
            boolean isBreak = false;
            int checkCallPhonePermission = 0;
            for (int i = 0; i < permissions.length; i++) {
                permission = permissions[i];
                checkCallPhonePermission = checkSelfPermission(permission);
                if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions, id);
                    isBreak = true;
                    break;
                }
            }
        }
    }

}