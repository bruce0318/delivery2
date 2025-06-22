package com.example.relativeclient;

import static com.example.relativeclient.Mythread.MythreadGetRecodrd.records;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.relativeclient.Mythread.MythreadGetRecodrd;
import com.example.relativeclient.Mythread.MythreadGetVphone;
import com.example.relativeclient.entity.activities;
import com.example.soniceyes.R;
import com.example.users.User;

public class PhoneActivity extends AppCompatActivity {
    public static String helped_VphoneNum="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.relative_home);

        //进入这个活动的前提是盲人处于在线状态
        MythreadGetRecodrd t=new MythreadGetRecodrd(User.getBlindId(),PhoneActivity.this);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //判断返回的记录
        activities aim_record=null;
        for(int i=0;i<records.size();i++)
        {
            activities newrecord=records.get(i);
            System.out.println(newrecord.getStart_time());
            System.out.println(newrecord.getEnd_time());
            if(newrecord.getAc_status()==1)//正在处于帮扶状态
            {
                if(newrecord.getEnd_time()==null)
                {aim_record=newrecord;}
            }

        }

//        if(aim_record==null)
//        {
//            Toast.makeText(PhoneActivity.this, "当前没有志愿者帮助", Toast.LENGTH_SHORT).show();
//            return;
//        }

        int volunteerId=aim_record.getVolunteer_id();//找到志愿者id

        //通过志愿者id获得志愿者电话
        MythreadGetVphone t2=new MythreadGetVphone(volunteerId,PhoneActivity.this);
        t2.start();
        try {
            t2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        if(shouldAskPermissions()){
            askPermissions();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.addCategory(intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("tel:" + helped_VphoneNum));
        startActivity(intent);
        finish();//完成通话，自动跳转回首页

        };

    public boolean shouldAskPermissions(){
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }
    public  void askPermissions() {
        String[] permissions = {
                "android.permission.CALL_PHONE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }
}


