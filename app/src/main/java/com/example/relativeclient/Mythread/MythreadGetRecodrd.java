package com.example.relativeclient.Mythread;


import static com.example.tools.Constants.serverURL;

import android.app.Activity;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.relativeclient.entity.activities;
import com.example.tools.NetworkHandler;
import com.example.users.User;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MythreadGetRecodrd extends Thread{

    int findID;
    public static List<activities> records=new ArrayList<>();
    private WeakReference<Activity> activityRef;


    public MythreadGetRecodrd(int id,Activity activity) {
        findID=id;
        this.activityRef = new WeakReference<>(activity);
    }


    @Override
    public void run() {

        try{
        String url=serverURL+"/relation/gethelpinfo?bid="+findID;



         //GET
        String returnBody = NetworkHandler.get(url);
        System.out.println(url);
        System.out.println("returnBody:**************");
        System.out.println(returnBody);
        System.out.println("还是写个中文醒目些");

////        //解析json语句
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

        {   //存在帮扶记录
            String jsarr=root.getString("data");
            records.clear();
            if(jsarr!=null) {
                activities.setAc_exist(true);
                List<activities> activitiesList = new ArrayList<activities>();
                JSONArray jsonBigArray = JSON.parseArray(jsarr);
                int size = jsonBigArray.size();//活动记录数
                for (int i = 0; i < size; i++) {
                    // 获取JSON小数组对象
                    JSONObject jsonObject = jsonBigArray.getJSONObject(i);
                    // 得到输出对象
                    activities record = new activities(jsonObject.getInteger("aid"),
                            jsonObject.getInteger("vid"),jsonObject.getDouble("lat")
                            ,jsonObject.getDouble("lon"));//活动id，志愿者id，纬度，经度
                    if(jsonObject.getTimestamp("stime")!=null)//活动开始时间
                    {
                        record.setStart_time(jsonObject.getTimestamp("stime").getTime());
                    }
                    else {
                        record.setStart_time(-1);
                    }
                    if(jsonObject.getTimestamp("etime")!=null)//活动结束时间
                    {record.setEnd_time(jsonObject.getTimestamp("etime").getTime());}
                    else{record.setEnd_time(-1);}
                    if(jsonObject.getTimestamp("btime")!=null)//活动结束时间
                    {record.setB_time(jsonObject.getTimestamp("btime").getTime());}
                    else{record.setEnd_time(-1);}

                    //活动帮助状态：帮助中，等待帮助中，超时，结束帮助
                    record.setAc_status(jsonObject.getInteger("status"));
                    record.setAddress(jsonObject.getString("address"));//帮助地点

//                    // 将输出对象添加到集合中
                    activitiesList.add(record);
                    records.add(record);
                    System.out.println(record.getStart_time());
                    System.out.println(record.getEnd_time());
                    System.out.println(record);
                }
            }
            else {
                activities.setAc_exist(false);
                System.out.println("数据为空！！！");
            }


        }
        else
        {
            System.out.println("暂无盲人帮扶记录");

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
