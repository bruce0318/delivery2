package com.example.relativeclient.Mythread;


import static com.example.tools.Constants.serverURL;

import android.app.Activity;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.tools.NetworkHandler;
import com.example.users.User;

import java.lang.ref.WeakReference;

public class MythreadGetbind extends Thread{

    String findphone;
    int findID;
    private WeakReference<Activity> activityRef;

    public MythreadGetbind(String phone,int id,Activity activity) {
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
            System.out.println("本次查询方式：手机号");
        }
        else
        {
            System.out.println("本次查询失败，用户不存在");
            return;
        }

        // GET
        String returnBody = NetworkHandler.get(url);
        System.out.println(url);
        System.out.println("returnBody:**************");
        System.out.println(returnBody);
        System.out.println("正在获取关系绑定信息");

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

            if(user!=null){
            //是不是盲人捏
            if(user.getString("userrole").trim().equals("blind"))
            {
                User.setBlindPhone(user.getString("username"));//这个是盲人电话
                //盲人id入库
                User.setBlindId(user.getInteger("id"));
                System.out.println("id：" + user.getInteger("id"));
                System.out.println("用户名：" + user.getString("name"));
                System.out.println("密码" + user.getString("password"));
                System.out.println("手机号" + user.getString("username"));
                System.out.println("身份" + user.getString("userrole"));
            }
        }}
        else
        {
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
