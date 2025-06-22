package com.example.relativeclient.Mythread;
import static com.example.tools.Constants.serverURL;
import static com.example.relativeclient.PhoneActivity.helped_VphoneNum;

import android.app.Activity;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.tools.NetworkHandler;
import com.example.users.User;

import java.lang.ref.WeakReference;

public class MythreadGetVphone extends Thread{

    int findID;//志愿者的id
    private WeakReference<Activity> activityRef;


    public MythreadGetVphone(int id,Activity activity) {
        findID=id;
        this.activityRef = new WeakReference<>(activity);
    }


    @Override
    public void run() {
        try{
        String url=serverURL+"/relation/getvinfo?vid="+findID;


        // GET
        String returnBody = NetworkHandler.get(url);
        System.out.println(url);
        System.out.println("returnBody:**************");
        System.out.println(returnBody);
        System.out.println("正在获取志愿者电话");

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
               helped_VphoneNum=user.getString("username");
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
