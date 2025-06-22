package com.example.relativeclient.Mythread;


import static com.example.tools.Constants.serverURL;

import android.app.Activity;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.tools.NetworkHandler;
import com.example.users.User;

import java.lang.ref.WeakReference;

public class MythreadFeedback extends Thread{

    String feedback;
    int aid,uid;
    private WeakReference<Activity> activityRef;
    public MythreadFeedback(String feed_back, int aid, int uid, Activity activity){
        this.feedback=feed_back;
        this.aid=aid;
        this.uid=uid;
        this.activityRef = new WeakReference<>(activity);
    }

    @Override
    public void run() {

        try{
        super.run();
        JSONObject jsonPost = new JSONObject();
        jsonPost.put("content",feedback);//反馈信息
        jsonPost.put("uid", uid);//志愿者id
        jsonPost.put("aid",aid);//活动id
        jsonPost.put("status",0);//帮助状态


        // JSON -> String
        String jsonBody = jsonPost.toJSONString();

        // POST URL
        String postURL = serverURL + "/relation/feedback";
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
