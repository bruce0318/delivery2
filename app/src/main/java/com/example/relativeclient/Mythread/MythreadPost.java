package com.example.relativeclient.Mythread;

import static com.example.tools.Constants.serverURL;

import android.app.Activity;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.tools.NetworkHandler;

import java.lang.ref.WeakReference;


public class MythreadPost extends Thread{
    String user_id, password, user_phone, user_role;
    private WeakReference<Activity> activityRef;
    public MythreadPost(String user, String pwo, String phone, String role,Activity activity)
    {
        user_id = user;
        password = pwo;
        user_phone = phone;
        user_role = role;
        this.activityRef = new WeakReference<>(activity);
    }
    @Override
    public void run() {
        //向服务器发送数据
        try{
        JSONObject jsonPost = new JSONObject();
        jsonPost.put("name",user_id);//应该是与user类的声明保持一致
        jsonPost.put("password",password);
        jsonPost.put("username",user_phone);
        jsonPost.put("userrole",user_role);
        // JSON -> String
        String jsonBody = jsonPost.toJSONString();
        // POST URL
        String postURL = serverURL + "/user";
        String putReturnBody = NetworkHandler.post(postURL, jsonBody);
    } catch (Exception e) { // 捕获所有异常
        e.printStackTrace();
        Activity activity = activityRef.get();
        if (activity != null) {
            activity.runOnUiThread(() ->
                    Toast.makeText(activity, "错误: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }


    }
    }
}




