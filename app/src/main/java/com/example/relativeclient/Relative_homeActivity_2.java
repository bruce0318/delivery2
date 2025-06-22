package com.example.relativeclient;

import static com.example.tools.Constants.GaodeAPI_key;
import static com.example.tools.Constants.serverURL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.example.relativeclient.Mythread.MythreadGetRecodrd;
import com.example.relativeclient.entity.activities;
import com.example.soniceyes.R;
import com.example.tools.NetworkHandler;
import com.example.users.User;
import android.view.View.OnClickListener;


import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;


import android.app.Activity;
import android.os.Bundle;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.CameraUpdateFactory;
//import com.amap.api.maps.LatLng;



import android.Manifest;
import android.os.Build;


import java.util.Timer;
import java.util.TimerTask;

public class Relative_homeActivity_2 extends Activity{
    Button button_home, button_home_bind, button_information,
            button_call_v, button_call_v_record, button_refresh;
    TextView clue_pic, blind_onlinestatuse;

    private long firstTimeBackPressed = 0;
    private static final int BACK_KEY_INTERVAL = 2000; // 两次按键的时间间隔


//    private MapView m_mapView;
    int mDynPointID = -1;
    public static boolean getgps = true;

    boolean convert_pos=false;
    boolean staying = false;
    double history_lat = 0;
    double history_lon = 0;
    int history_count = 0;

    NotificationCompat.Builder progressBuilder;
    String channelId = "channel_id";
    String channelName = "channel_name";
    Bitmap bitmap;
    NotificationManagerCompat notificationManager;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //申请后台运行
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);


        //申请通知权限
        if (ContextCompat.checkSelfPermission(Relative_homeActivity_2.this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Relative_homeActivity_2.this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        //创建channelId
        notificationManager = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName,
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            channel.enableVibration(true);
        }




        //解决刘海颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.theme_blue));
        }

        setContentView(R.layout.relative_home);
        {
//            button_home = findViewById(R.id.button_home);
//            button_home_bind = findViewById(R.id.button_home_bind);
//            button_information = findViewById(R.id.button_information);
//            button_call_v = findViewById(R.id.button_call_v);
//            button_call_v_record = findViewById(R.id.button_call_v_record);
            button_refresh = findViewById(R.id.button_refresh);

            //高德展示地图api调用
            // Call these methods before initializing the AMap SDK
            MapsInitializer.updatePrivacyShow(this, true, true);
            MapsInitializer.updatePrivacyAgree(this, true);
            //显示地图
            MapView mapView = (MapView) findViewById(R.id.mapView);
            mapView.onCreate(savedInstanceState);// 此方法必须重写
            AMap aMap = mapView.getMap();
          //显示一下亲属用户的定位？为后续导航实现——>导航到盲人位置

//            LatLng latLng = new LatLng(30.515977,114.414724);
//            final Marker marker = aMap.addMarker(new MarkerOptions().position(latLng).title("您守护的视障人士的位置").snippet("DefaultMarker"));
//            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));


            //分别完成跳转任务
            button_refresh.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    //首先判断是否在线
                    if (!getgps) {
                        Toast.makeText(getApplicationContext(), "当前盲人离线中，无法获取其位置信息",
                                Toast.LENGTH_LONG).show();
                        finish();
                        //刷新当前活动
                        Intent intent = new Intent(Relative_homeActivity_2.this, Relative_homeActivity_2.class);
                        startActivity(intent);
                    }
                    //打开地图
                    else {
                        System.out.println("主线程在这里打开地图");
                        //'''更新点坐标'''

                        LatLng latLng = new LatLng(User.getBlindLat(), User.getBlindLon());
                        LatLng desLatLng;
                        if(convert_pos){
                            //坐标转化
                            CoordinateConverter converter  = new CoordinateConverter(Relative_homeActivity_2.this);
                                // CoordType.GPS 待转换坐标类型
                            converter.from(CoordinateConverter.CoordType.GPS);
                                // sourceLatLng待转换坐标点 LatLng类型
                            converter.coord(latLng);
                                // 执行转换操作
                            desLatLng = converter.convert();
                        }
                        else{desLatLng =latLng;}
                            //画点
                        final Marker marker = aMap.addMarker(new MarkerOptions().position(desLatLng).title("您守护的视障人士的位置").snippet("DefaultMarker"));
                        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(desLatLng,16));
                    }
                }
            });

            button_home_bind.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (User.getBlindId() != -1) {
                        Toast.makeText(Relative_homeActivity_2.this, "您已绑定过账号，如要修改请转至个人信息界面", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(Relative_homeActivity_2.this, BindingActivity.class);
                        startActivity(intent);
                    }
                }
            });

            button_information.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Relative_homeActivity_2.this, InformationActivity.class);
                    startActivity(intent);
                }
            });

            button_call_v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (User.getBlindState() == 2)//帮扶中
                    {
                        Intent intent = new Intent(Relative_homeActivity_2.this, PhoneActivity.class);
                        startActivity(intent);
                    } else if (User.getBlindState() == 3) {//在线但未被帮助
                        Toast.makeText(Relative_homeActivity_2.this,
                                "盲人目前未被帮助，无法与相应志愿者取得联系", Toast.LENGTH_SHORT).show();
                    } else if (User.getBlindState() == 1) {
                        Toast.makeText(Relative_homeActivity_2.this,
                                "盲人目前未被帮助，无法与相应志愿者取得联系", Toast.LENGTH_SHORT).show();
                    } else {//离线状态
                        Toast.makeText(Relative_homeActivity_2.this,
                                "盲人目前处于离线状态", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            button_call_v_record.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    int blind_id= User.getBlindId();//引用静态变量
                    MythreadGetRecodrd t=new MythreadGetRecodrd(blind_id,Relative_homeActivity_2.this);
                    t.start();
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (activities.isAc_exist() == true) {
                        Intent intent = new Intent(Relative_homeActivity_2.this, Helped_historyActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(Relative_homeActivity_2.this,
                                "暂无求助记录", Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }
        ;

    }


    private void requestPermissions() { //Android6.0以上设备设置动态权限
        if (Build.VERSION.SDK_INT >= 23) {
            // 检查是否拥有权限
            String[] permissions = {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
            };
            String permission = null;
            int id = 0;
            boolean isBreak = false;
            int checkCallPhonePermission = 0;
            for (int i = 0; i < permissions.length; i++) {
                permission = permissions[i];
                checkCallPhonePermission = checkSelfPermission(permission);
                if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions, id);
                    isBreak = true;
                    break;
                }
            }
        }

    }

    private void updatePoint() {
        //循环获取盲人实时位置
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                String url = serverURL + "/location" + "/" + User.getBlindId();

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
                        if (user.getInteger("status") == 0||(user.getDouble("lon")==0&&user.getDouble("lat")==0))//如果获取的位置表示的是下线状态
                        {
                            User.setBlindLat(0);
                            User.setBlindLon(0);
                            User.setBlindState(user.getInteger("status"));
                            history_count=0;
                            getgps = false;
                        } else {

                            getgps = true;
                            User.setBlindLat(user.getDouble("lat"));//这个是盲人电话
                            User.setBlindLon(user.getDouble("lon"));
                            User.setBlindState(user.getInteger("status"));
                            System.out.println("lat：" + user.getDouble("lat"));
                            System.out.println("lon：" + user.getDouble("lon"));
                            System.out.println("status: " + user.getInteger("status"));
                            System.out.println(Thread.currentThread().getName());

                            if (history_count == 0)//如果更新位置了
                            {
                                history_lat = user.getDouble("lat");
                                history_lon = user.getDouble("lon");
                            }
                            if (testStay(history_lat, history_lon, user.getDouble("lat"), user.getDouble("lon"))) {
                                history_count += 1;
                            } else {
                                history_count = 0;
                            }

                        }

                        if (history_count >= 60)//以每20s收集一次的话
                        {
                            //要发信息
                            setNotification();
                            //发完信息
                            history_count -= 8;//设置过3分钟又来发信息

                        }
                    }
                    if (!getgps) {
                        history_count = 0;
                        this.cancel();
                        System.out.println("循环结束，盲人端已经下线");
                    }

                } else {
                    System.out.println("该用户不存在");
                    this.cancel();
                    System.out.println("循环结束，盲人端已经下线");
                }


            }

        }, 1000, 20000);
        timer = null;


    }


    public interface OnLocationConvertListener {
        void onLocationConvert(double lon, double lat);
    }

//    private void convertLocation(double lon, double lat,
//                                 final Relative_homeActivity_2.OnLocationConvertListener listener) {
//        String getUrl = "https://restapi.amap.com/v3/assistant/coordinate/convert?locations="
//                + lon + "," + lat + "&coordsys=gps&key="+GaodeAPI_key;
//        new Thread(() -> {
//            try {
//                String change = NetworkHandler.get(getUrl);
//                if (change != null) {
//                    final String finalChange = change;
//                    runOnUiThread(() -> {
//                        try {
//                            JSONObject root = JSONObject.parseObject(finalChange);
//                            int status = root.getInteger("status");
//                            if (status == 1) {
//                                String location = root.getString("locations");
//                                String[] parts = location.split(",");
//                                double newLon = Double.parseDouble(parts[0]);
//                                double newLat = Double.parseDouble(parts[1]);
//                                if (listener != null) {
//                                    listener.onLocationConvert(newLon, newLat);
//                                }
//                            } else {
//                                Log.e("Relative_homeActivity_2",
//                                        "API error: " + root.getString("info"));
//                            }
//                        } catch (Exception e) {
//                            Log.e("Relative_homeActivity_2",
//                                    "Error parsing JSON", e);
//                        }
//                    });
//                } else {
//                    Log.e("Relative_homeActivity_2",
//                            "Network response is null");
//                }
//            } catch (Exception e) {
//                Log.e("Relative_homeActivity_2",
//                        "Error in network request", e);
//            }
//        }).start();
//    }




    private boolean testStay(double historyLat, double historyLon, double currLat, double currLon) {
        if (Math.abs(historyLat - currLat) <= 0.5 && Math.abs(historyLon - currLon) <= 0.001)//视为没动
        {
            return true;
        } else {
            return false;
        }
    }

    private void setNotification() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification.Builder builder = new Notification.Builder(this, channelId);
            Intent intent = new Intent(this, Relative_homeActivity_2.class);
            int flag = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flag = flag | PendingIntent.FLAG_IMMUTABLE;
            }
            intent.putExtra("infor", System.currentTimeMillis());

            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, intent, flag);

            builder
                    .setVisibility(Notification.VISIBILITY_PRIVATE)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setContentTitle("长时间停留告知")
                    .setContentText("检测到您绑定的视障人士出现长期停留状况，点击消息查看详情")
                    .setWhen(System.currentTimeMillis())
                    .setColor(Color.rgb(255, 0, 0))
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.gps))
                    .setSmallIcon(R.mipmap.gps);

            Notification notification = builder.build();
            if (ContextCompat.checkSelfPermission(Relative_homeActivity_2.this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Relative_homeActivity_2.this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
            notificationManager.notify(100, notification);


        }

    }


    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - firstTimeBackPressed < BACK_KEY_INTERVAL) {
            // 第二次按下返回键，退出应用
            super.onBackPressed();
            finish();

        } else {
            // 第一次按下返回键，提示用户
            firstTimeBackPressed = System.currentTimeMillis();
            Toast.makeText(Relative_homeActivity_2.this,
                    "再次点击返回退出应用", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 重置第一次按下返回键的时间
                    firstTimeBackPressed = 0;
                }
            }, BACK_KEY_INTERVAL);
        }
    }

    public  void askPermissions() {
        String[] permissions = {
                "android.permission.CALL_PHONE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }
}
