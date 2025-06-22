//package com.example.users;
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
//import android.os.Handler;
//import android.os.Message;
//import android.text.TextUtils;
//import android.widget.Toast;
//import android.app.Activity;
//
//import androidx.core.content.ContextCompat;
//
//import com.example.relativeclient.InformationActivity;
//import com.example.relativeclient.Mythread.MythreadGetexist;
//import com.example.relativeclient.Mythread.MythreadPutphone;
//import com.example.soniceyes.R;
//import com.example.relativeclient.Relative_homeActivity;
//
//import cn.smssdk.EventHandler;
//import cn.smssdk.SMSSDK;
//
//
//public class Get_user_numActivity extends Activity implements View.OnClickListener {
//
//    EventHandler eventHandler;
//    private EditText edit_phone;
//    private EditText edit_cord;
//    private Button btn_getCord;
//    private Button btn_bind;
//    private String phone_number;
//    private String cord_number;
//    private boolean flag=true;
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.get_user_num);
//        //解决刘海颜色
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(ContextCompat.getColor(this, R.color.defaultblue));
//        }
//        getId();//控件初始化
//
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
//    }
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        SMSSDK.unregisterEventHandler(eventHandler);
//    }
//
///////////////使用Handler来分发Message对象到主线程中，处理事件///////////
//
//    @SuppressLint("HandlerLeak")
//    Handler handler=new Handler()
//    {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            int event=msg.arg1;
//            int result=msg.arg2;
//            Object data=msg.obj;
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
//            if(result==SMSSDK.RESULT_COMPLETE)
//            {
//                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
//                    Toast.makeText(getApplicationContext(), "验证码输入正确",
//                            Toast.LENGTH_LONG).show();
//
//                    initUser();
//                    //传入数据库之前先获取该用户的id
//                    //这个已经保存在了tempdata里面
//                    //更新数据库中手机号码信息
//                    MythreadPutphone t=new MythreadPutphone(User.getUserId(),phone_number);
//                    t.start();
//                    try {
//                        t.join();
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//
//                    User.setUserPhone(edit_phone.getText().toString().trim());
//                    saveTempdata();
//
//                    //保存到手机中
//
//                    //跳转界面
//                    finish();
//                    Intent intent = new Intent(Get_user_numActivity.this, InformationActivity.class);
//                    startActivity(intent);
//                }
//            }
//            else
//            {
//                if(flag)
//                {
//                    btn_getCord.setVisibility(View.VISIBLE);
//                    Toast.makeText(getApplicationContext(),"验证码获取失败请重新获取", Toast.LENGTH_LONG).show();
//                    edit_phone.requestFocus();
//
//                }
//                else
//                {
//                    Toast.makeText(getApplicationContext(),"验证码输入错误", Toast.LENGTH_LONG).show();
//                }
//            }
//
//        }
//
//    };
//
//    ////////////////初始化///////////////////////////////
//    private void getId()
//    {
//        edit_phone=findViewById(R.id.edit_phone);
//        edit_cord=findViewById(R.id.edit_yzm);
//        btn_getCord=findViewById(R.id.button_getyzm);
//        btn_bind=findViewById(R.id.button_bind);
//        btn_getCord.setOnClickListener(this);
//        btn_bind.setOnClickListener(this);
//    }
//
//    ///////////////////////////////////按钮事件////////////////////////////////////
//    public void onClick(View v)
//    {
//
//        MythreadGetexist t=new MythreadGetexist(edit_phone.getText().toString().trim());
//        t.start();
//        try {
//            t.join();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println("主线程已经执行到这");
//
//        if(v.getId()==R.id.button_getyzm)
//        {
//            if(judPhone())//去掉左右空格获取字符串
//            {
//                SMSSDK.getVerificationCode("86",phone_number);
//                Toast.makeText(getApplicationContext(),"验证码已发送", Toast.LENGTH_LONG).show();
//                edit_cord.requestFocus();
//            }
//
//        } else if (v.getId()==R.id.button_bind) {
//            if(judCord())
//                SMSSDK.submitVerificationCode("86",phone_number,cord_number);
//            flag=false;
//
//        }
//    }
//
//
//
//
//
///////////////////////手机号和验证码的判断//////////////////////////
//    private boolean judPhone()
//    {
//
//        //判断输入手机号没有被绑定
//        if(User.isExisting())//已经存在
//        {
//            Toast.makeText(Get_user_numActivity.this,"该手机号已被注册，请重新输入",Toast.LENGTH_LONG).show();
//            return false;
//
//        }
//        if(TextUtils.isEmpty(edit_phone.getText().toString().trim()))
//        {
//            Toast.makeText(Get_user_numActivity.this,"请输入您的电话号码",Toast.LENGTH_LONG).show();
//            edit_phone.requestFocus();
//            return false;
//        }
//        else if(edit_phone.getText().toString().trim().length()!=11)
//        {
//
//            Toast.makeText(Get_user_numActivity.this,"您的电话号码位数不正确",Toast.LENGTH_LONG).show();
//            edit_phone.requestFocus();
//            return false;
//        }
//        else
//        {
//            phone_number=edit_phone.getText().toString().trim();
//            String num="[1][345678]\\d{9}";
//            if(phone_number.matches(num))
//                return true;
//            else
//            {
//                Toast.makeText(Get_user_numActivity.this,"请输入正确的手机号码",Toast.LENGTH_LONG).show();
//                return false;
//            }
//        }
//    }
//
//    private boolean judCord()
//    {
//        judPhone();
//        if(TextUtils.isEmpty(edit_cord.getText().toString().trim()))
//        {
//            Toast.makeText(Get_user_numActivity.this,"请输入您的验证码",Toast.LENGTH_LONG).show();
//            edit_cord.requestFocus();
//            return false;
//        }
//        else if(edit_cord.getText().toString().trim().length()!=4)
//        {
//            Toast.makeText(Get_user_numActivity.this,"您的验证码位数不正确",Toast.LENGTH_LONG).show();
//            edit_cord.requestFocus();
//
//            return false;
//        }
//        else
//        {
//            cord_number=edit_cord.getText().toString().trim();
//            return true;
//        }
//
//    }
//
//    private void savePhonenum(String phone){
//        SharedPreferences sharedPreferences = getSharedPreferences("tempdata", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("phone",phone);
//        editor.commit();
//
//    }
//
//    private void initUser()
//    {
//        SharedPreferences sharedPreferences = getSharedPreferences("tempdata", MODE_PRIVATE);
//        User.setUserId(sharedPreferences.getInt("id",-1));
//        User.setUserName(sharedPreferences.getString("username",""));
//        User.setPASSWORD(sharedPreferences.getString("password",""));
//        User.setROLE(sharedPreferences.getString("role",""));
//        User.setUserPhone(sharedPreferences.getString("phone",""));
//
//
//    }
//
//    private void saveTempdata() {
//        // 使用SharedPreferences保存用户名和密码
//        SharedPreferences sharedPreferences = getSharedPreferences("tempdata", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("username", User.getUserName());
//        editor.putString("password", User.getPASSWORD());
//        editor.putString("phone",User.getUserPhone());
//        editor.putString("role",User.getROLE());
//        editor.putInt("id",User.getUserId());
//        editor.commit();
//    }
//
//
//
//
//}
//
//
//
//
//
//
//





package com.example.users;

import android.annotation.SuppressLint;
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
import android.app.Activity;

import androidx.core.content.ContextCompat;

import com.example.relativeclient.InformationActivity;
import com.example.relativeclient.Mythread.MythreadGetexist;
import com.example.relativeclient.Mythread.MythreadPutphone;
import com.example.relativeclient.Relative_homeActivity;
import com.example.soniceyes.R;

public class Get_user_numActivity extends Activity implements View.OnClickListener {

    private EditText edit_phone;
    private EditText edit_cord;
    private Button btn_getCord;
    private Button btn_bind;
    private String phone_number;
    private String cord_number;
    private boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_user_num);
        // 解决刘海颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.defaultblue));
        }
        getId(); // 控件初始化
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // 初始化控件
    private void getId() {
        edit_phone = findViewById(R.id.edit_phone);
        edit_cord = findViewById(R.id.edit_yzm);
        btn_getCord = findViewById(R.id.button_getyzm);
        btn_bind = findViewById(R.id.button_bind);
        btn_getCord.setOnClickListener(this);
        btn_bind.setOnClickListener(this);
    }

    // 按钮事件
    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {

        // 先检查手机号是否存在
        MythreadGetexist t = new MythreadGetexist(edit_phone.getText().toString().trim(),Get_user_numActivity.this);
        t.start();
        try {
            t.join(); // 等待线程完成
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (v.getId() == R.id.button_getyzm) {
            if (judPhone()) {
                // 模拟验证码发送
                Toast.makeText(getApplicationContext(), "验证码已发送（默认验证码：1234）", Toast.LENGTH_LONG).show();
                edit_cord.requestFocus();
            }
        } else if (v.getId() == R.id.button_bind) {
            if (judCord()) {
                // 验证码验证逻辑
                if (cord_number.equals("1234")) {
                    Toast.makeText(getApplicationContext(), "验证码输入正确", Toast.LENGTH_LONG).show();
                    initUser();
                    // 更新手机号到数据库
                    MythreadPutphone t2 = new MythreadPutphone(User.getUserId(), edit_phone.getText().toString().trim(),Get_user_numActivity.this);
                    t2.start();
                    try {
                        t2.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    User.setUserPhone(edit_phone.getText().toString().trim());
                    saveTempdata();
                    finish();
                    Intent intent = new Intent(Get_user_numActivity.this, InformationActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "验证码输入错误", Toast.LENGTH_LONG).show();
                    edit_cord.requestFocus();
                }
            }
            flag = false;
        }
    }

    // 手机号验证
    private boolean judPhone() {
        if (User.isExisting()) {
            Toast.makeText(this, "该手机号已被注册", Toast.LENGTH_LONG).show();
            return false;
        }
        if (TextUtils.isEmpty(edit_phone.getText().toString().trim())) {
            Toast.makeText(Get_user_numActivity.this, "请输入您的电话号码", Toast.LENGTH_LONG).show();
            edit_phone.requestFocus();
            return false;
        } else if (edit_phone.getText().toString().trim().length() != 11) {
            Toast.makeText(Get_user_numActivity.this, "您的电话号码位数不正确", Toast.LENGTH_LONG).show();
            edit_phone.requestFocus();
            return false;
        } else {
            phone_number = edit_phone.getText().toString().trim();
            String num = "[1][345678]\\d{9}";
            if (phone_number.matches(num)) {
                return true;
            } else {
                Toast.makeText(Get_user_numActivity.this, "请输入正确的手机号码", Toast.LENGTH_LONG).show();
                return false;
            }
        }
    }

    // 验证码验证
    private boolean judCord() {
        if (TextUtils.isEmpty(edit_cord.getText().toString().trim())) {
            Toast.makeText(Get_user_numActivity.this, "请输入您的验证码", Toast.LENGTH_LONG).show();
            edit_cord.requestFocus();
            return false;
        } else if (edit_cord.getText().toString().trim().length() != 4) {
            Toast.makeText(Get_user_numActivity.this, "您的验证码位数不正确", Toast.LENGTH_LONG).show();
            edit_cord.requestFocus();
            return false;
        } else {
            cord_number = edit_cord.getText().toString().trim();
            return true;
        }
    }

    // 保存手机号
    private void savePhonenum(String phone) {
        SharedPreferences sharedPreferences = getSharedPreferences("tempdata", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("phone", phone);
        editor.commit();
    }

    // 初始化用户信息
    private void initUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("tempdata", MODE_PRIVATE);
        User.setUserId(sharedPreferences.getInt("id", -1));
        User.setUserName(sharedPreferences.getString("username", ""));
        User.setPASSWORD(sharedPreferences.getString("password", ""));
        User.setROLE(sharedPreferences.getString("role", ""));
        User.setUserPhone(sharedPreferences.getString("phone", ""));
    }

    // 保存临时数据
    private void saveTempdata() {
        SharedPreferences sharedPreferences = getSharedPreferences("tempdata", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", User.getUserName());
        editor.putString("password", User.getPASSWORD());
        editor.putString("phone", User.getUserPhone());
        editor.putString("role", User.getROLE());
        editor.putInt("id", User.getUserId());
        editor.commit();
    }
}
