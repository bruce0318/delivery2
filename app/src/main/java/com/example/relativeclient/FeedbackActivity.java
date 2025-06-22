package com.example.relativeclient;

import static com.example.relativeclient.Helped_historyActivity.post_aid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.relativeclient.Mythread.MythreadFeedback;
import com.example.soniceyes.R;
import com.example.users.Get_user_numActivity;
import com.example.users.User;

public class FeedbackActivity extends Activity {

    @SuppressLint("WrongViewCast")
    EditText content;
    Button push_content;

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
        setContentView(R.layout.feed_back);
        {
            //初始化控件
            content=findViewById(R.id.editText_feedback);
            push_content=findViewById(R.id.push_feedback);


            //提交内容
            push_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String feedback=content.getText().toString().trim();
                    if(feedback.equals(""))
                    {
                        Toast.makeText(FeedbackActivity.this, "请写下您的反馈再点击提交", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //向数据库post
                        if(feedback.length()>200)
                        {
                            Toast.makeText(FeedbackActivity.this, "反馈不能超过200字，请删减后重新提交", Toast.LENGTH_SHORT).show();

                        }
                        else{

                            MythreadFeedback t=new MythreadFeedback(feedback,post_aid,User.getUserId(),FeedbackActivity.this);
                            t.start();
                            try {
                                t.join();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                            post_aid=0;//初始化
                            System.out.println("已经初始化");
                            Toast.makeText(FeedbackActivity.this, "提交成功", Toast.LENGTH_SHORT).show();}
                    }
                }
            });




        };



    }
    @Override
    public void onBackPressed() {
        if(User.getROLE().trim().equals("guardian"))
        {  finish();
            Intent intent = new Intent(FeedbackActivity.this, InformationActivity.class);
            startActivity(intent);}
        else {
            super.onBackPressed();
        }
    }


}
