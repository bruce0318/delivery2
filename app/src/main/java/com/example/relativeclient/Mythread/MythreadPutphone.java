package com.example.relativeclient.Mythread;



import static com.example.tools.Constants.serverURL;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.tools.NetworkHandler;
import com.example.users.User;

import java.lang.ref.WeakReference;

public class MythreadPutphone extends Thread{
    int user_id;
    String user_phone;//需要的信息是用户id和手机号
    private WeakReference<Activity> activityRef;
    public MythreadPutphone(int user, String phone,Activity activity)
    {
        user_id = user;
        user_phone=phone;
        this.activityRef = new WeakReference<>(activity);
    }

    @Override
    public void run() {
        try{
        //向服务器发送数据
        JSONObject jsonPost = new JSONObject();
        jsonPost.put("username",user_phone);//应该是与user类的声明保持一致
        jsonPost.put("id",user_id);
        jsonPost.put("name",User.getUserName());//应该是与user类的声明保持一致
        jsonPost.put("password",User.getPASSWORD());
        jsonPost.put("userrole",User.getROLE());
        jsonPost.put("role","USER");

        // JSON -> String
        String jsonBody = jsonPost.toJSONString();

        // POST URL
        String postURL = serverURL + "/user";
        String putReturnBody = NetworkHandler.put(postURL, jsonBody);
        Log.d("MythreadPutphone:", "正在向服务器传输修改后的手机号");
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
