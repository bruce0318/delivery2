package com.example.relativeclient;

import static com.example.relativeclient.Mythread.MythreadGetRecodrd.records;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.relativeclient.Mythread.MythreadGetRecodrd;
import com.example.relativeclient.entity.activities;
import com.example.soniceyes.R;
import com.example.users.User;



public class Helped_historyActivity extends Activity {

    RecyclerView mRecyclerView;
    MyAdapter mMyAdapter ;

    public static int post_aid=0;
    Button button_home,button_bind,button_phone,button_infor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //解决刘海颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.defaultblue));
        }
        setContentView(R.layout.helped_history);{
            //导航栏通用功能
            button_home=findViewById(R.id.button_home2);
            button_bind=findViewById(R.id.button_home_bind2);
            button_phone=findViewById(R.id.button_call_v2);
            button_infor=findViewById(R.id.button_information2);
            button_bind.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (User.getBlindId() != -1) {
                        Toast.makeText(Helped_historyActivity.this, "您已绑定过账号，如要修改请转至个人信息界面", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(Helped_historyActivity.this, BindingActivity.class);
                        startActivity(intent);
                    }
                }
            });

            button_home.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Helped_historyActivity.this, Relative_homeActivity.class);
                    startActivity(intent);

                }
            });

            button_infor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Helped_historyActivity.this, InformationActivity.class);
                    startActivity(intent);

                }
            });

            button_phone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (User.getBlindState() == 2)//帮扶中
                    {
                        Intent intent = new Intent(Helped_historyActivity.this, PhoneActivity.class);
                        startActivity(intent);
                    } else if (User.getBlindState() == 3) {//在线但未被帮助
                        Toast.makeText(Helped_historyActivity.this,
                                "盲人目前未被帮助，无法与相应志愿者取得联系", Toast.LENGTH_SHORT).show();
                    } else if (User.getBlindState() == 1) {
                        Toast.makeText(Helped_historyActivity.this,
                                "盲人目前未被帮助，无法与相应志愿者取得联系", Toast.LENGTH_SHORT).show();
                    } else {//离线状态
                        Toast.makeText(Helped_historyActivity.this,
                                "盲人目前处于离线状态", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        mRecyclerView = findViewById(R.id.call_history);
        // YYL这里需要从服务器获取消息
        //尝试一下服务器回写
        //1.这边先发出申请
        int blind_id= User.getBlindId();//引用静态变量
        MythreadGetRecodrd t=new MythreadGetRecodrd(blind_id,Helped_historyActivity.this);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        mMyAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mMyAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(Helped_historyActivity.this);
        mRecyclerView.setLayoutManager(layoutManager);
    }



    class MyAdapter extends RecyclerView.Adapter<MyViewHoder> {

        @NonNull
        @Override//item_list决定排布方式，helped_record决定list的整体设计
        public MyViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 使用 parent 作为根容器，确保宽度正确
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list, parent, false);
            return new MyViewHoder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHoder holder, int position) {
            activities news = records.get(position);
            System.out.println(news.getStart_time());
            holder.mTitleTv.setText("帮助记录id:  "+String.valueOf(news.getActivity_id()));
            holder.mTitleContent1.setText("志愿者id:  "+String.valueOf(news.getVolunteer_id()));
            holder.mTitleContent2.setText(news.getAddress());
            holder.mTitleContent3.setText(news.getStart_time());
            holder.mTitleContent5.setText(news.getB_time());
            holder.mTitleContent6.setText(news.getEnd_time());

            String activity_status="";
            if(news.getAc_status()==0)
            {
                activity_status="等待帮助中";
                holder.mTitleContent4.setImageResource(R.drawable.waiting_blind);
            } else if (news.getAc_status()==1) {
                activity_status="帮助中";
                holder.mTitleContent4.setImageResource(R.drawable.help_copy);
            } else if (news.getAc_status()==2) {
                activity_status="结束帮助";
                holder.mTitleContent4.setImageResource(R.drawable.loveper_copy);
            }
            else{activity_status="活动超时";
                holder.mTitleContent4.setImageResource(R.drawable.timeout);}

            if(!String.valueOf(news.getActivity_id()).equals("null"))
            {post_aid=Integer.parseInt(String.valueOf(news.getActivity_id()));}
            holder.mButnJubao.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    Intent intent = new Intent(Helped_historyActivity.this, FeedbackActivity.class);
                    startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount() {
            return records.size();
        }
    }

    class MyViewHoder extends RecyclerView.ViewHolder {
        TextView mTitleTv;
        TextView mTitleContent1,mTitleContent2,mTitleContent3,mTitleContent5,mTitleContent6;
        ImageView mTitleContent4;
        Button mButnJubao;

        public MyViewHoder(@NonNull View itemView) {
            super(itemView);
            mTitleTv = itemView.findViewById(R.id.tv_aid2);
            mTitleContent1 = itemView.findViewById(R.id.tv_vid2);
            mTitleContent2=itemView.findViewById(R.id.tv_address2);
            mTitleContent3=itemView.findViewById(R.id.tv_stime2);
            mTitleContent4=itemView.findViewById(R.id.iv_status);
            mButnJubao=itemView.findViewById(R.id.button_jubao);
            mTitleContent5=itemView.findViewById(R.id.tv_btime2);
            mTitleContent6=itemView.findViewById(R.id.tv_etime2);


        }
    }
    @Override
    public void onBackPressed() {
        finish();
//        Intent intent = new Intent(Helped_historyActivity.this, Relative_homeActivity.class);
//        startActivity(intent);
    }
}

