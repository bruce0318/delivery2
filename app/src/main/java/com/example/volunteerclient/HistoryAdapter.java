package com.example.volunteerclient;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soniceyes.R;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {
    private List<BVActivity> BVActivityList;
    private Context context;

    public HistoryAdapter(List<BVActivity> BVActivityList, Context context) {
        this.BVActivityList = BVActivityList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        BVActivity bvActivity = BVActivityList.get(position);

        holder.tv_aid.setText("帮扶单号： #" + bvActivity.getId());
        holder.tv_bid.setText("求助者ID： #" + bvActivity.getBid());
        String btime = bvActivity.getBtime();
        holder.tv_btime.setText(btime.substring(0, btime.length() - 3));
        String stime = bvActivity.getStime();
        holder.tv_stime.setText(stime.substring(0, stime.length() - 3));
        String etime = bvActivity.getEtime();
        holder.tv_etime.setText(etime.substring(0, etime.length() - 3));
        holder.tv_address.setText(bvActivity.getAddress());

        //点赞
        holder.btn_like.setOnClickListener(v -> {
            // 每次点击都触发动画

            // 让按钮变色（可以反复点，持续是亮的）
            holder.btn_like.setColorFilter(ContextCompat.getColor(context, R.color.defaultblue));
            holder.btn_like.setImageResource(R.drawable.ic_thumb_up);

            // 点击缩放动画
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(holder.btn_like, "scaleX", 1f, 1.5f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(holder.btn_like, "scaleY", 1f, 1.5f, 1f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.setDuration(300);
            animatorSet.start();

            // 爆炸动画
//            holder.like_explosion.setVisibility(View.VISIBLE);
//            holder.like_explosion.animate()
//                    .alpha(1f)
//                    .setDuration(150)
//                    .withEndAction(() -> holder.like_explosion.setVisibility(View.GONE))
//                    .start();
        });


    }

    @Override
    public int getItemCount() {
        return BVActivityList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_aid, tv_bid, tv_stime, tv_etime, tv_btime, tv_address;
        public ImageButton btn_like;
        public View like_explosion;

        public MyViewHolder(View view) {
            super(view);
            tv_aid = view.findViewById(R.id.tv_aid);
            tv_bid = view.findViewById(R.id.tv_bid);
            tv_stime = view.findViewById(R.id.tv_stime);
            tv_etime = view.findViewById(R.id.tv_etime);
            tv_btime = view.findViewById(R.id.tv_btime);
            tv_address = view.findViewById(R.id.tv_address);
            btn_like = view.findViewById(R.id.btn_like);  // 绑定点赞按钮
//            like_explosion = view.findViewById(R.id.like_explosion);
        }
    }
}
