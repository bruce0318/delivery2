package com.example.relativeclient.Mythread;


import static com.example.tools.Constants.serverURL;

import android.app.Activity;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.tools.NetworkHandler;
import com.example.users.User;

import java.lang.ref.WeakReference;

public class MythreadGetrelation extends Thread{

    int UserId;//给定亲属id进行查询
    private WeakReference<Activity> activityRef;
    public MythreadGetrelation(int user_id,Activity activity) {
        UserId=user_id;
        this.activityRef = new WeakReference<>(activity);
    }

    //
    @Override
    public void run() {
        try{
//        String serverURL = "http://192.168.0.101:8080";//本机地址
        String url = serverURL + "/relation" + "/getblindid?guardian_id=" + UserId;

        // GET
        String returnBody = NetworkHandler.get(url);
        System.out.println(url);
        System.out.println("returnBody:**************");
        System.out.println(returnBody);
        System.out.println("正在查询该亲属绑定的盲人的ID");

        //解析json语句
        String jsonstr = returnBody;
        //最顶层的JSON对象
        JSONObject root = JSONObject.parseObject(jsonstr);
        System.out.println("json数据：" + root);
        //获得整型数据
        int code = root.getInteger("code");
        //获得字符串型数据
        String msg = root.getString("message");
        System.out.println("代码：" + code);
        System.out.println("消息：" + msg);

        if (code == 200)
        //获得对象
        {
            JSONObject user = root.getJSONObject("data");
            if (user != null) {
//                System.out.println("user_id：" + user.getInteger("guardianid"));
//                System.out.println("blind_id：" + user.getInteger("blindid"));
//                System.out.println("relation_id" + user.getInteger("relation_id"));
                //将解析结果赋值给User
               User.setBlindId(user.getInteger("blindid"));
               User.setBindging(true);
               User.setRelationId(user.getInteger("relation_id"));
               System.out.println("已将盲人数据赋值给User");

            } else {
                User.setBindging(false);
            }


        } else {//没查到绑定信息的话
            User.setBindging(false);
        }

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
