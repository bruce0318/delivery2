//package com.example.relativeclient;
//
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Build;
//import android.os.Bundle;
//import android.view.View;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.EditText;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.content.ContextCompat;
//
//import android.os.Handler;
//import android.os.Message;
//import android.text.TextUtils;
//import android.widget.Toast;
//
//import com.example.relativeclient.Mythread.MythreadGetbind;
//import com.example.relativeclient.Mythread.MythreadPostbind;
//import com.example.soniceyes.R;
//import com.example.users.User;
//import com.mob.MobSDK;
//
//import cn.smssdk.EventHandler;
//import cn.smssdk.SMSSDK;
//
//
//public class BindingActivity extends AppCompatActivity {
//    EventHandler eventHandler;
//    private EditText edit_phone;
//    private EditText edit_cord;
//    private Button btn_getCord;
//    private Button btn_bind;
//    private String phone_number;
//    private String cord_number;
//    private boolean flag = true;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.binding);
//        //解决刘海颜色
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(ContextCompat.getColor(this, R.color.defaultblue));
//        }
//
//        getId();//控件初始化
//
//        eventHandler = new EventHandler() {
//            public void afterEvent(int event, int result, Object data) {
//                Message msg=new Message();
//                msg.arg1=event;
//                msg.arg2=result;
//                msg.obj=data;
//                handler.sendMessage(msg);
//
//            }
//        };
//
//        SMSSDK.registerEventHandler(eventHandler);//handler:传入默认值
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        SMSSDK.unregisterEventHandler(eventHandler);
//    }
//
///////////////使用Handler来分发Message对象到主线程中，处理事件///////////
//
//    @SuppressLint("HandlerLeak")
//    Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            int event = msg.arg1;
//            int result = msg.arg2;
//            Object data = msg.obj;
//
//
//            if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
//                if (result == SMSSDK.RESULT_COMPLETE) {
//                    boolean smart = (Boolean) data;
//                    if (smart) {
//                        Toast.makeText(getApplicationContext(), "该手机号已经注册过，请重新输入",
//                                Toast.LENGTH_LONG).show();
//                        edit_phone.requestFocus();
//                        return;
//                    }
//                }
//            }
//            if (result == SMSSDK.RESULT_COMPLETE) {
//                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
//                    Toast.makeText(getApplicationContext(), "完成绑定",
//                            Toast.LENGTH_LONG).show();
//
//                    //更新数据及数据库(getbind中就已经更新了User的相关变量)
//                    MythreadPostbind t2=new MythreadPostbind(User.getMODE());
//                    t2.start();
//                    try {
//                        t2.join();
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//
//                    //保存数据
//                    saveBind();
//                    //跳转界面
//                    if(User.getMODE()==0)
//                    {
//                        finish();
//                    Intent intent = new Intent(BindingActivity.this, Relative_homeActivity.class);
//                    startActivity(intent);}
//                    else if (User.getMODE()==1) {
//                        finish();
//                        Intent intent = new Intent(BindingActivity.this, InformationActivity.class);
//                        startActivity(intent);
//
//                    }
//                }
//            } else {
//                if (flag) {
//                    btn_getCord.setVisibility(View.VISIBLE);
//                    Toast.makeText(getApplicationContext(), "验证码获取失败请重新获取", Toast.LENGTH_LONG).show();
//                    edit_phone.requestFocus();
//
//                } else {
//                    Toast.makeText(getApplicationContext(), "验证码输入错误", Toast.LENGTH_LONG).show();
//                }
//            }
//
//        }
//
//    };
//
//    ////////////////初始化///////////////////////////////
//    private void getId() {
//        edit_phone = findViewById(R.id.editTextPhone);
//        edit_cord = findViewById(R.id.yanzhengma);
//        btn_getCord = findViewById(R.id.button);
//        btn_bind = findViewById(R.id.buttonBound);
//        btn_getCord.setOnClickListener(this::onClick);
//        btn_bind.setOnClickListener(this::onClick);
//    }
//
//    ///////////////////////////////////按钮事件////////////////////////////////////
//    public void onClick(View v) {
//
//        //获取盲人账号信息 YYL首先手机号必须是盲人--->这一步获取盲人信息id,phone
//        MythreadGetbind t=new MythreadGetbind(edit_phone.getText().toString().trim(),-1);
//        t.start();
//        try {
//            t.join();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//
//        if (v.getId() == R.id.button) {
//            if (judPhone())//去掉左右空格获取字符串
//            {
//                MobSDK.submitPolicyGrantResult(true);
//                SMSSDK.getVerificationCode("86", phone_number);
//                Toast.makeText(getApplicationContext(), "验证码已发送", Toast.LENGTH_LONG).show();
//                edit_cord.requestFocus();
//            }
//
//        } else if (v.getId() == R.id.buttonBound) {
//            if (judCord())
//                SMSSDK.submitVerificationCode("86", phone_number, cord_number);
//            flag = false;
//
//            eventHandler = new EventHandler() {
//                public void afterEvent(int event, int result, Object data) {
//                    Message msg = new Message();
//                    msg.arg1 = event;
//                    msg.arg2 = result;
//                    msg.obj = data;
//                    handler.sendMessage(msg);
//
//                }
//            };
//
//            SMSSDK.registerEventHandler(eventHandler);//handler:传入默认值
//
//
//        }
//    }
///***/
//
//    /////////////////////手机号和验证码的判断//////////////////////////
//    private boolean judPhone() {
//        if(User.getBlindId()==-1)
//        {
//            Toast.makeText(BindingActivity.this, "该账户不存在，请重新输入手机号", Toast.LENGTH_LONG).show();
//            return false;
//        }
//        if (TextUtils.isEmpty(edit_phone.getText().toString().trim())) {
//            Toast.makeText(BindingActivity.this, "请输入您的电话号码", Toast.LENGTH_LONG).show();
//            edit_phone.requestFocus();
//            return false;
//        } else if (edit_phone.getText().toString().trim().length() != 11) {
//
//            Toast.makeText(BindingActivity.this, "您的电话号码位数不正确", Toast.LENGTH_LONG).show();
//            edit_phone.requestFocus();
//            return false;
//        } else {
//            phone_number = edit_phone.getText().toString().trim();
//            String num = "[1][345678]\\d{9}";
//            if (phone_number.matches(num))
//                return true;
//            else {
//                Toast.makeText(BindingActivity.this, "请输入正确的手机号码", Toast.LENGTH_LONG).show();
//                return false;
//            }
//        }
//    }
//
//    private boolean judCord() {
//        judPhone();
//        if (TextUtils.isEmpty(edit_cord.getText().toString().trim())) {
//            Toast.makeText(BindingActivity.this, "请输入您的验证码", Toast.LENGTH_LONG).show();
//            edit_cord.requestFocus();
//            return false;
//        } else if (edit_cord.getText().toString().trim().length() != 4) {
//            Toast.makeText(BindingActivity.this, "您的验证码位数不正确", Toast.LENGTH_LONG).show();
//            edit_cord.requestFocus();
//
//            return false;
//        } else {
//            cord_number = edit_cord.getText().toString().trim();
//            return true;
//        }
//
//    }
//
//    private void saveBind()
//    {
//        SharedPreferences sharedPreferences = getSharedPreferences("tempdata", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("blind_phone",User.getBlindPhone());
//        editor.putInt("blind_id",User.getBlindId());
//        editor.commit();
//    }
//
//
//}
//
//

package com.example.relativeclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.relativeclient.Mythread.MythreadGetbind;
import com.example.relativeclient.Mythread.MythreadPostbind;
import com.example.soniceyes.R;
import com.example.users.User;

public class BindingActivity extends AppCompatActivity {
    private EditText edit_phone;
    private EditText edit_cord;
    private Button btn_getCord;
    private Button btn_bind;
    private String phone_number;
    private String cord_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.binding);

        // 修改状态栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.defaultblue));
        }

        getId(); // 初始化控件
    }

    private void getId() {
        edit_phone = findViewById(R.id.editTextPhone);
        edit_cord = findViewById(R.id.yanzhengma);
        btn_getCord = findViewById(R.id.button);
        btn_bind = findViewById(R.id.buttonBound);
        btn_getCord.setOnClickListener(this::onClick);
        btn_bind.setOnClickListener(this::onClick);
    }

    public void onClick(View v) {
        // 获取盲人账号信息
        MythreadGetbind t = new MythreadGetbind(edit_phone.getText().toString().trim(), -1,BindingActivity.this);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (v.getId() == R.id.button) { // 获取验证码按钮
            if (judPhone()) {
                Toast.makeText(getApplicationContext(), "验证码1234", Toast.LENGTH_LONG).show();
                edit_cord.requestFocus();
            }
        } else if (v.getId() == R.id.buttonBound) { // 绑定按钮
            if (judCord()) {
                Toast.makeText(getApplicationContext(), "完成绑定", Toast.LENGTH_LONG).show();

                // 更新数据及数据库
                User.setBlindPhone(edit_phone.getText().toString().trim());
                MythreadPostbind t2 = new MythreadPostbind(User.getMODE(),BindingActivity.this);
                t2.start();
                try {
                    t2.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                saveBind(); // 保存绑定数据

                // 跳转界面
                if (User.getMODE() == 0) {//即登陆之后的绑定，user_mode应该是0
                    finish();
                    startActivity(new Intent(BindingActivity.this, Relative_homeActivity.class));
                } else if (User.getMODE() == 1) {//在编辑信息界面，user_mode是1
                    finish();
                    startActivity(new Intent(BindingActivity.this, InformationActivity.class));
                }
            }
        }
    }

    private boolean judPhone() {
        if (User.getBlindId() == -1) {
            Toast.makeText(BindingActivity.this, "该账户不存在，请重新输入手机号", Toast.LENGTH_LONG).show();
            return false;
        }

        String phone = edit_phone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(BindingActivity.this, "请输入您的电话号码", Toast.LENGTH_LONG).show();
            edit_phone.requestFocus();
            return false;
        } else if (phone.length() != 11) {
            Toast.makeText(BindingActivity.this, "您的电话号码位数不正确", Toast.LENGTH_LONG).show();
            edit_phone.requestFocus();
            return false;
        } else {
            phone_number = phone;
            String num = "[1][345678]\\d{9}";
            if (phone_number.matches(num)) {
                return true;
            } else {
                Toast.makeText(BindingActivity.this, "请输入正确的手机号码", Toast.LENGTH_LONG).show();
                return false;
            }
        }
    }

    private boolean judCord() {
        judPhone();
        String cord = edit_cord.getText().toString().trim();
        if (TextUtils.isEmpty(cord)) {
            Toast.makeText(BindingActivity.this, "请输入您的验证码", Toast.LENGTH_LONG).show();
            edit_cord.requestFocus();
            return false;
        } else if (!cord.equals("1234")) {
            Toast.makeText(BindingActivity.this, "验证码错误，默认验证码是 1234", Toast.LENGTH_LONG).show();
            edit_cord.requestFocus();
            return false;
        } else {
            cord_number = cord;
            return true;
        }
    }

    private void saveBind() {
        SharedPreferences sharedPreferences = getSharedPreferences("tempdata", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("blind_phone", User.getBlindPhone());
        editor.putInt("blind_id", User.getBlindId());
        editor.commit();
    }
}
