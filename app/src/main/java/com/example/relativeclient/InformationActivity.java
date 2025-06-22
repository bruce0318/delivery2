package com.example.relativeclient;


import static com.example.tools.Constants.serverURL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.relativeclient.Mythread.MythreadGetRecodrd;
import com.example.relativeclient.entity.activities;
import com.example.soniceyes.R;
import com.example.users.LoginActivity;
import com.example.users.User;
import com.example.volunteerclient.NetworkTask;
import com.example.volunteerclient.Vedit_informationActivity;

public class InformationActivity extends Activity {
    @SuppressLint("WrongViewCast")

    TextView text_inf_tiltle,textView_role,textView_id,textView_phonenum,textView_bind,textView_binding;
    ImageView img_touxiang;
    Button button_feedback,button_logoff,button_edit;
    Button button_home,button_bind,button_phone,button_get_record;

    //从服务器获取用户信息
    String user_id,user_role,user_phone,bind_id;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //解决刘海颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.defaultblue));
        }
        setContentView(R.layout.information);
        {
            text_inf_tiltle=findViewById(R.id.text_inf_title);
            textView_id=findViewById(R.id.textView_userid);
            textView_role=findViewById(R.id.textView_userrole);
            textView_phonenum=findViewById(R.id.phonenum);
            textView_bind=findViewById(R.id.textView_bingdingid);
            img_touxiang=findViewById(R.id.img_touxiang);
            button_edit=findViewById(R.id.button_edit_infor);
            button_feedback=findViewById(R.id.button_feedback);
            button_logoff=findViewById(R.id.button_logoff);
            textView_binding=findViewById(R.id.textView_binding);
            //导航栏通用功能
            button_home=findViewById(R.id.button_home1);
            button_bind=findViewById(R.id.button_home_bind1);
            button_phone=findViewById(R.id.button_call_v1);
            button_get_record=findViewById(R.id.button_call_v_record1);
            Get_information();

            button_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(User.getROLE().trim().equals("guardian"))
                    {   finish();
                        Intent intent = new Intent(InformationActivity.this, Edit_informationActivity.class);
                        startActivity(intent);
                    }
                    else if(User.getROLE().trim().equals("volunteer"))
                    {   finish();
                        Intent intent = new Intent(InformationActivity.this, Vedit_informationActivity.class);
                        startActivity(intent);
                    }
                }
            });

            button_logoff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(User.getROLE().trim().equals("volunteer"))
                    {
                        String jsonBody = "{\"id\":\"" + User.getUserId() + "\", \"status\": \"0\"}";
                        String postUrl =serverURL + "/online";
                        // 执行POST请求的异步任务
                        new NetworkTask("POST", jsonBody).execute(postUrl);
                        removeCredentials();
                        finish();
                        Intent intent = new Intent(InformationActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                    else{
                    removeCredentials();
                    finish();
                    Intent intent = new Intent(InformationActivity.this, LoginActivity.class);
                    startActivity(intent);}
                }
            });

            button_feedback.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                    Intent intent = new Intent(InformationActivity.this, FeedbackActivity.class);
                    startActivity(intent);
                }
            });
        };

        button_bind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (User.getBlindId() != -1) {
                    Toast.makeText(InformationActivity.this, "您已绑定过账号，如要修改请转至个人信息界面", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(InformationActivity.this, BindingActivity.class);
                    startActivity(intent);
                }
            }
        });

        button_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InformationActivity.this, Relative_homeActivity.class);
                startActivity(intent);

            }
        });

        button_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (User.getBlindState() == 2)//帮扶中
                {
                    Intent intent = new Intent(InformationActivity.this, PhoneActivity.class);
                    startActivity(intent);
                } else if (User.getBlindState() == 3) {//在线但未被帮助
                    Toast.makeText(InformationActivity.this,
                            "盲人目前未被帮助，无法与相应志愿者取得联系", Toast.LENGTH_SHORT).show();
                } else if (User.getBlindState() == 1) {
                    Toast.makeText(InformationActivity.this,
                            "盲人目前未被帮助，无法与相应志愿者取得联系", Toast.LENGTH_SHORT).show();
                } else {//离线状态
                    Toast.makeText(InformationActivity.this,
                            "盲人目前处于离线状态", Toast.LENGTH_SHORT).show();
                }
            }
        });

        button_get_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int blind_id= User.getBlindId();//引用静态变量
                MythreadGetRecodrd t=new MythreadGetRecodrd(blind_id,InformationActivity.this);
                t.start();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (activities.isAc_exist() == true) {
                    Intent intent = new Intent(InformationActivity.this, Helped_historyActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(InformationActivity.this,
                            "暂无求助记录", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void Get_information(){
        //获取SharedPreferences对象
        SharedPreferences sharedPreferences = getSharedPreferences("tempdata",MODE_PRIVATE);
        String saved_username=sharedPreferences.getString("username","");
        String saved_role=sharedPreferences.getString("role","");
        String saved_phonenum=sharedPreferences.getString("phone","");

        textView_id.setText(User.getUserName());//用这个小文件主要是为了防止自动登录出现问题
        if(User.getROLE().trim().equals("guardian"))
        {textView_role.setText("亲属");}
        else if (User.getROLE().trim().equals("volunteer")) {
            textView_role.setText("志愿者");
        }
        else {textView_role.setText("盲人");}
        textView_phonenum.setText(User.getUserPhone());
        if(User.getROLE().trim().equals("guardian"))
        {textView_bind.setText(User.getBlindPhone());}//YYL?视障绑定手机号是？
        else if (User.getROLE().trim().equals("volunteer")) {
            textView_bind.setText("");
            textView_binding.setText("");
        }

    }

    private void removeCredentials(){
        //获取SharedPreferences对象
        SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
        //获取Editor对象的引用
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //将获取过来的值放入文件
        editor.remove("username");
        editor.remove("password");
        // 提交数据
        editor.commit();
    }

    @Override
    public void onBackPressed() {
        if(User.getROLE().trim().equals("guardian"))
        {   this.finish();
//            Intent intent = new Intent(InformationActivity.this, Relative_homeActivity.class);
//        startActivity(intent);
        }
        else {
            super.onBackPressed();
        }
    }


}



