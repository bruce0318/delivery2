package com.example.volunteerclient;

import static com.example.tools.Constants.serverURL;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.relativeclient.Mythread.MythreadPutname;
import com.example.relativeclient.Mythread.MythreadPutphone;
import com.example.soniceyes.R;
import com.example.users.LoginActivity;
import com.example.users.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class Vedit_informationActivity extends AppCompatActivity {

    private TextView originPhone, originName;
    private View buttonUpdatePhone, buttonUpdateName, buttonLogoff;
    private Dialog loadingDialog;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vedit_information);

        initViews();
        initUser();
        setupBottomNavigation();
        setupListeners();
        setupLoadingDialog();
    }

    private void initViews() {
        originName = findViewById(R.id.textview_name1);
        originPhone = findViewById(R.id.textView_phone1);
        buttonUpdateName = findViewById(R.id.button_update_name1);
        buttonUpdatePhone = findViewById(R.id.button_update_phone1);
        buttonLogoff = findViewById(R.id.button_logoff);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_help) {
                startActivity(new Intent(this, Volunteer_helpActivity.class));
                return true;
            } else if (id == R.id.nav_map) {
                startActivity(new Intent(this, Volunteer_homeActivity.class));
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, Volunteer_historyActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    private void setupListeners() {
        originPhone.setText(User.getUserPhone());
        originName.setText(User.getUserName());

        buttonUpdateName.setOnClickListener(view -> {
            showInputDialog("修改姓名", "请输入新姓名", inputText -> {
                if (!inputText.isEmpty()) {
                    updateName(inputText);
                } else {
                    showSnackbar("姓名不能为空");
                }
            });
        });

        buttonUpdatePhone.setOnClickListener(view -> {
            showInputDialog("修改手机号", "请输入新手机号", inputText -> {
                if (!inputText.isEmpty()) {
                    updatePhone(inputText);
                } else {
                    showSnackbar("手机号不能为空");
                }
            });
        });

        buttonLogoff.setOnClickListener(view -> {
            if ("volunteer".equals(User.getROLE().trim())) {
                String jsonBody = "{\"id\":\"" + User.getUserId() + "\", \"status\": \"0\"}";
                String postUrl = serverURL + "/online";
                new NetworkTask("POST", jsonBody).execute(postUrl);
            }
            removeCredentials();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void updateName(String newName) {
        showLoading();
        new Thread(() -> {
            MythreadPutname thread = new MythreadPutname(User.getUserId(), newName,Vedit_informationActivity.this);
            thread.run();
            runOnUiThread(() -> {
                hideLoading();
                User.setUserName(newName);
                saveTempData();
                originName.setText(newName);
                showSnackbar("姓名修改成功！");
            });
        }).start();
    }

    private void updatePhone(String newPhone) {
        showLoading();
        new Thread(() -> {
            MythreadPutphone thread = new MythreadPutphone(User.getUserId(), newPhone,Vedit_informationActivity.this);
            thread.run();
            runOnUiThread(() -> {
                hideLoading();
                User.setUserPhone(newPhone);
                saveTempData();
                originPhone.setText(newPhone);
                showSnackbar("手机号修改成功！");
            });
        }).start();
    }

    private void setupLoadingDialog() {
        loadingDialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null);
        loadingDialog.setContentView(view);
        loadingDialog.setCancelable(false);
    }

    private void showLoading() {
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void hideLoading() {
        if (loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void showInputDialog(String title, String hint, InputDialogListener listener) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setBackgroundResource(R.drawable.edit_text_border);

        editText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        editText.setPadding(40, 30, 40, 30);
        editText.setTextSize(16);

        // 用一个小的容器包住 EditText，方便设置内边距和背景
        LinearLayout layout = new LinearLayout(this);
        layout.setPadding(50, 30, 50, 10);
        layout.addView(editText);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomDialog)
                .setTitle(title)
                .setView(layout)
                .setPositiveButton("确认", (dialogInterface, which) -> {
                    String input = editText.getText().toString().trim();
                    listener.onInputConfirmed(input);
                })
                .setNegativeButton("取消", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.show();

        // 修改按钮颜色
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.theme_blue));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.gray));
    }


    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    private void initUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("tempdata", MODE_PRIVATE);
        User.setUserId(sharedPreferences.getInt("id", -1));
        User.setUserName(sharedPreferences.getString("username", ""));
        User.setPASSWORD(sharedPreferences.getString("password", ""));
        User.setROLE(sharedPreferences.getString("role", ""));
        User.setUserPhone(sharedPreferences.getString("phone", ""));
    }

    private void saveTempData() {
        SharedPreferences sharedPreferences = getSharedPreferences("tempdata", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", User.getUserName());
        editor.putString("password", User.getPASSWORD());
        editor.putString("phone", User.getUserPhone());
        editor.putString("role", User.getROLE());
        editor.putInt("id", User.getUserId());
        editor.apply();
    }

    private void removeCredentials() {
        SharedPreferences sharedPreferencesLogin = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor loginEditor = sharedPreferencesLogin.edit();
        loginEditor.clear();
        loginEditor.apply();

        SharedPreferences sharedPreferencesTemp = getSharedPreferences("tempdata", MODE_PRIVATE);
        SharedPreferences.Editor tempEditor = sharedPreferencesTemp.edit();
        tempEditor.clear();
        tempEditor.apply();
    }

    interface InputDialogListener {
        void onInputConfirmed(String inputText);
    }

    @SuppressWarnings("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // 禁用返回键
    }
}