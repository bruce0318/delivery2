package com.example.volunteerclient;

import static com.example.tools.Constants.serverURL;


import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.soniceyes.R;
import com.example.tools.NetworkHandler;
import com.example.users.User;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private List<BVActivity> BVActivityList;
    private Context context;
    String phoneNumber = "";

    public MyAdapter(List<BVActivity> BVActivityList, Context context) {
        this.BVActivityList = BVActivityList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        BVActivity bvActivity = BVActivityList.get(position);

        holder.tvOrderNumber.setText("求助单号：#" + bvActivity.getId());
        holder.tvDistance.setText("距离：" + bvActivity.getDistance() + "km");
        holder.tvAddress.setText(bvActivity.getAddress());
        holder.tvTime.setText(bvActivity.getBtime());

        //确认接单
        holder.btn_take_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //发送接单请求
                String servicePathGet = "help/blind?aid=" + bvActivity.getId() + "&bid=" + bvActivity.getBid() + "&vid=" + User.getUserId();  //这里直接指定了一个vid来测试
                // 构建GET请求的URL
                String getUrl = serverURL + "/" + servicePathGet;
                // 执行GET请求的异步任务
                new NetworkTask("GET", null).execute(getUrl);

                new Thread(() -> {
                    try {
                        String Help = NetworkHandler.get(getUrl);
                        // 写入号码
                        JSONObject root = JSONObject.parseObject(Help);
                        String parsedPhoneNumber = root.getString("data");
                        phoneNumber = parsedPhoneNumber;

                        final String finalPhoneNumber = phoneNumber;
                        ((Activity)context).runOnUiThread(() -> {
                            Toast.makeText(context, "号码获取成功: " + finalPhoneNumber, Toast.LENGTH_SHORT).show();
                        });
                    } catch (Exception e) {
                        Log.e("OrderConfirmedActivity", "Error in network request", e);
                        ((Activity)context).runOnUiThread(() -> {
                            Toast.makeText(context, "网络请求错误", Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();

                new Thread(() -> {
                    try {
                        String Help = NetworkHandler.get(getUrl);
                        // 写入号码
                        JSONObject root = JSONObject.parseObject(Help);
                        int code = root.getInteger("code");
                        if (code == 200){
                            String parsedPhoneNumber = root.getString("data");
                            phoneNumber = parsedPhoneNumber;
                            ((Activity)context).runOnUiThread(() -> {
                                Toast.makeText(context, "号码获取成功: " + phoneNumber, Toast.LENGTH_SHORT).show();
                                Toast.makeText(context, "接单成功", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(context, OrderConfirmedActivity.class);
                                intent.putExtra("aid", bvActivity.getId());
                                intent.putExtra("bid", bvActivity.getBid());
                                intent.putExtra("address", bvActivity.getAddress());
                                intent.putExtra("blat", bvActivity.getLat());
                                intent.putExtra("blon", bvActivity.getLon());
                                intent.putExtra("btime", bvActivity.getBtime());
                                intent.putExtra("phoneNumber", phoneNumber);
                                intent.putExtra("distance", bvActivity.getDistance());
                                context.startActivity(intent);
                            });
                        }
                        else{
                            ((Activity)context).runOnUiThread(() -> {
                                Toast.makeText(context, "接单失败，请重新打开页面 " + phoneNumber, Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        Log.e("OrderConfirmedActivity", "Error in network request", e);
                    }
                }).start();
            }
        });

        //点击卡片查看详情
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("aid", bvActivity.getId());
            intent.putExtra("bid", bvActivity.getBid());
            intent.putExtra("address", bvActivity.getAddress());
            intent.putExtra("blat", bvActivity.getLat());
            intent.putExtra("blon", bvActivity.getLon());
            intent.putExtra("btime", bvActivity.getBtime());
            intent.putExtra("distance", bvActivity.getDistance());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return BVActivityList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvOrderNumber, tvDistance, tvAddress, tvTime;
        public Button btn_take_order;

        public MyViewHolder(View view) {
            super(view);
            tvOrderNumber = view.findViewById(R.id.tv_order_number);
            tvDistance = view.findViewById(R.id.tv_distance);
            tvAddress = view.findViewById(R.id.tv_address);
            tvTime = view.findViewById(R.id.tv_time);
            btn_take_order = view.findViewById(R.id.btn_take_order);
        }
    }

}