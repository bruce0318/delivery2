package com.example.relativeclient;

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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.relativeclient.Mythread.MythreadPutname;
import com.example.soniceyes.R;
import com.example.users.Get_user_numActivity;
import com.example.users.User;

public class Edit_informationActivity extends Activity {

    @SuppressLint("WrongViewCast")
    EditText edit_name;
    TextView origin_phone,origin_bind;
    Button button_update_phonenum,button_update_name,button_update_bind;

    //从数据库获取原信息
    String origin_phonenum,origin_bindnid;




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
        setContentView(R.layout.edit_information);
        {
            initUser();//获取原来的信息
            edit_name=findViewById(R.id.edit_name);
            button_update_name=findViewById(R.id.button_update_name);
            button_update_phonenum=findViewById(R.id.button_update_phone);
            button_update_bind=findViewById(R.id.button_update_bind);
            origin_phone=findViewById(R.id.textView_editphone);
            origin_phone.setText(User.getUserPhone());
            origin_bind=findViewById(R.id.textView_editbind);
            origin_bind.setText(User.getBlindPhone());


//更新姓名直接上传给服务器
            button_update_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!edit_name.getText().toString().trim().equals(""))
                    {   MythreadPutname t=new MythreadPutname(User.getUserId(),edit_name.getText().toString().trim(),Edit_informationActivity.this);
                        t.start();
                        try {
                            t.join();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        //更新
                        User.setUserName(edit_name.getText().toString().trim());
                        saveTempdata();

                        Toast.makeText(Edit_informationActivity.this, "修改成功", Toast.LENGTH_SHORT).show();}
                    else {Toast.makeText(Edit_informationActivity.this, "请输入要修改的姓名", Toast.LENGTH_SHORT).show();}

                }
            });

//跳转到绑定手机号的界面更新手机号
            button_update_phonenum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                    Intent intent = new Intent(Edit_informationActivity.this, Get_user_numActivity.class);
                    startActivity(intent);
                }
            });
//跳转到绑定盲人账号界面绑定账号
            button_update_bind.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    User.setMODE(1);
                    finish();
                    Intent intent = new Intent(Edit_informationActivity.this, BindingActivity.class);
                    startActivity(intent);
                }
            });



        };




}

    private void initUser()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("tempdata", MODE_PRIVATE);
        User.setUserId(sharedPreferences.getInt("id",-1));
        User.setUserName(sharedPreferences.getString("username",""));
        User.setPASSWORD(sharedPreferences.getString("password",""));
        User.setROLE(sharedPreferences.getString("role",""));
        User.setUserPhone(sharedPreferences.getString("phone",""));


    }
    private void saveTempdata() {
        // 使用SharedPreferences保存用户名和密码
        SharedPreferences sharedPreferences = getSharedPreferences("tempdata", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", User.getUserName());
        editor.putString("password", User.getPASSWORD());
        editor.putString("phone",User.getUserPhone());
        editor.putString("role",User.getROLE());
        editor.putInt("id",User.getUserId());
        editor.commit();
    }

    @Override
    public void onBackPressed() {
        if(User.getROLE().trim().equals("guardian"))
        {  finish();
            Intent intent = new Intent(Edit_informationActivity.this, InformationActivity.class);
            startActivity(intent);}
        else {
            super.onBackPressed();
        }
    }
}
