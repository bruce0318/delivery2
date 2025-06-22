package com.example.relativeclient.Mythread;

import static com.example.relativeclient.Relative_homeActivity.getgps;
import static com.example.tools.Constants.serverURL;

import android.app.Activity;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.tools.NetworkHandler;
import com.example.users.User;

import java.lang.ref.WeakReference;
import  java.util.Timer;
import  java.util.TimerTask;

public class MythreadGetgps extends Thread {

    int findID;//绑定的盲人id
    private WeakReference<Activity> activityRef;

    public MythreadGetgps(int id, Activity activity) {
        findID = id;
        this.activityRef = new WeakReference<>(activity);
    }


    @Override
    public void run() {
        try {
            String url = serverURL + "/location" + "/" + findID;
            // GET
            String returnBody = NetworkHandler.get(url);
            System.out.println(url);
            System.out.println("returnBody:**************");
            System.out.println(returnBody);
            System.out.println("正在获取盲人信息");

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
                    User.setBlindLat(user.getDouble("lat"));//这个是盲人电话
                    User.setBlindLon(user.getDouble("lon"));
                    User.setBlindState(user.getInteger("status"));
                    User.setBlindLoc(user.getString("address"));
                    System.out.println("lat：" + user.getDouble("lat"));
                    System.out.println("lon：" + user.getDouble("lon"));
                    System.out.println("status: " + user.getInteger("status"));
                    System.out.println("loc:" + user.getString("address"));
                    System.out.println("出了什么问题");

                } else//如果获取的位置表示的是下线状态
                {
                    User.setBlindLat(0);//这个是盲人电话
                    User.setBlindLon(0);
                    User.setBlindState(user.getInteger("status"));
                    User.setBlindLoc("离线中");
                    getgps = false;

                }
            } else {
                System.out.println("该用户不存在");
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
