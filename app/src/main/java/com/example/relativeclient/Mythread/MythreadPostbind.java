package com.example.relativeclient.Mythread;



import static com.example.tools.Constants.serverURL;

import android.app.Activity;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.tools.NetworkHandler;
import com.example.users.User;

import java.lang.ref.WeakReference;


//在Getbind线程中就获取了绑定信息
public class MythreadPostbind extends Thread{
    int blind_id,guardian_id,relation_id;
    int mode;
    private WeakReference<Activity> activityRef;
    public MythreadPostbind(int Mode,Activity activity){
        mode=Mode;//0表示上传，1表示修改
        this.activityRef = new WeakReference<>(activity);
    }
    @Override
    public void run() {

        try{
        //根据电话获取blind_id-->User.BLIND_ID
        //直接引用User.id来获取该账号的id
        blind_id= User.getBlindId();
        guardian_id=User.getUserId();
        relation_id=User.getRelationId();

        //向服务器发送数据

        if(mode==0)
        {
            JSONObject jsonPost = new JSONObject();
            jsonPost.put("blindid",blind_id);
            jsonPost.put("guardianid",guardian_id);

            // JSON -> String
            String jsonBody = jsonPost.toJSONString();

            // POST URL
            String postURL = serverURL + "/relation";
            String putReturnBody = NetworkHandler.post(postURL, jsonBody);
        }
        else

        {   JSONObject jsonPost = new JSONObject();
            jsonPost.put("blindid",blind_id);//应该是与user类的声明保持一致
            jsonPost.put("guardianid",guardian_id);
            jsonPost.put("relation_id",relation_id);


            // JSON -> String
            String jsonBody = jsonPost.toJSONString();

            // POST URL
            String postURL = serverURL + "/relation";
            String putReturnBody = NetworkHandler.put(postURL, jsonBody);}

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
