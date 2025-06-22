package com.example.volunteerclient;

import static com.example.tools.Constants.serverURL;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.alibaba.fastjson.JSONObject;
import com.example.soniceyes.R;
import com.example.tools.NetworkHandler;
import com.example.users.User;

public class Volunteer_stateActivity extends AppCompatActivity
{
    private RadioGroup stateGroup;
    private RadioButton state0, state1, state2;
    @SuppressLint("WrongViewCast")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.volunteer_state);

        // 初始化Toolbar并作为ActionBar使用
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 设置ActionBar的标题
        getSupportActionBar().setTitle("返回");

        // 启用返回键
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        stateGroup = (RadioGroup) findViewById(R.id.stateGroup);
        state1 = findViewById(R.id.state1);
        state0 = findViewById(R.id.state0);
        state2 = findViewById(R.id.state2);

        checkStatus();

        stateGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //获取被选中的radiobutton的id
                RadioButton rcheck = (RadioButton) findViewById(checkedId);
                //获取
                String checkText = rcheck.getText().toString();


                String servicePathPost = "online"; // 根据实际情况替换
                // 构建POST请求的URL和JSON体
                int state = 999;
                switch (checkText){
                    case "离线" : state = 0;
                        Toast.makeText(Volunteer_stateActivity.this, "当前状态为：" + checkText, Toast.LENGTH_SHORT).show();
                                break;
                    case "在线" : state = 1;
                        Toast.makeText(Volunteer_stateActivity.this, "当前状态为：" + checkText, Toast.LENGTH_SHORT).show();
                                break;
                    case "帮助中" :
                        Toast.makeText(Volunteer_stateActivity.this, "仅可切换在线与离线状态" , Toast.LENGTH_SHORT).show();
                        stateGroup.check(state1.getId());
                        state = 1;
                                break;
                }
                String jsonBody = "{\"id\":\"" + User.getUserId() + "\", \"status\": \"" + state + "\"}";
                String postUrl = serverURL + "/" + servicePathPost;
                // 执行POST请求的异步任务
                new NetworkTask("POST", jsonBody).execute(postUrl);
            }
        });


    }

    private void checkStatus(){
        new Thread(() -> {
            try {
                String getUrl = serverURL + "/online/volunteer?vid=" + User.getUserId() ;
                String check = NetworkHandler.get(getUrl);
                if (check != null) {
                    runOnUiThread(() -> {
                        JSONObject root = JSONObject.parseObject(check);
                        int status = root.getInteger("status");
                        switch (status){
                            case 0:
                                stateGroup.check(state0.getId());
                                break;
                            case 1:
                                stateGroup.check(state1.getId());
                                break;
                            case 2:
                                stateGroup.check(state2.getId());
                                break;
                            default:
                                Toast.makeText(this, "状态加载失败",Toast.LENGTH_SHORT).show();
                                break;
                        }
                    });
                } else {
                    Log.e("Volunteer_stateActivity", "Network response is null");

                }
            } catch (Exception e) {
                Log.e("Volunteer_stateActivity", "Error in network request", e);
            }
        }).start();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 处理返回键点击
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // 调用系统的返回键逻辑
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // 这里可以添加一些自定义的返回逻辑
        // 例如，弹出对话框确认退出
        super.onBackPressed(); // 调用父类的onBackPressed()方法来执行默认的返回操作
    }

}
