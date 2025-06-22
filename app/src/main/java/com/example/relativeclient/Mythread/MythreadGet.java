package com.example.relativeclient.Mythread;


import static com.example.tools.Constants.serverURL;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.tools.NetworkHandler;

import com.example.users.User;

import java.lang.ref.WeakReference;


public class MythreadGet extends Thread {

    String findphone;
    int findID;
    private WeakReference<Activity> activityRef;
    public MythreadGet(String phone,int id,Activity activity) {
        findphone = phone;
        findID=id;
        this.activityRef = new WeakReference<>(activity);
    }

    @Override
    public void run() {

        try{
        String url="";
        if(findphone.equals("")&&findID!=-1)
        {
            url = serverURL + "/user" + "/" + findID;
            System.out.println("本次查询方式：用户id");
        }
        else if (findID==-1&&!findphone.equals("")) {
            url = serverURL + "/user" + "/s/" + findphone;
            System.out.println("本次查询方式：手机号"+findphone);
        }


        // GET
        String returnBody = NetworkHandler.get(url);
        System.out.println(url);
        System.out.println("returnBody:**************");
        System.out.println(returnBody);
        System.out.println("还是写个中文醒目些");

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

        if(code==200)
        //获得对象
        {
            JSONObject user = root.getJSONObject("data");
            if(user!=null)
            {   User.setExisting(true);//存在该用户
//                System.out.println("id：" + user.getInteger("id"));
//                System.out.println("用户名：" + user.getString("name"));
//                System.out.println("密码" + user.getString("password"));
//                System.out.println("手机号" + user.getString("username"));
//                System.out.println("身份" + user.getString("userrole"));
                //将解析结果赋值给User
                User.setUserName(user.getString("name"));
                User.setPASSWORD(user.getString("password"));
                User.setUserPhone(user.getString("username"));
                User.setROLE(user.getString("userrole"));
                User.setUserId(user.getInteger("id"));}
            else {
                User.setExisting(false);
                System.out.println("数据为空！！！");
            }
        }
        else
        {
            User.setExisting(false);
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
