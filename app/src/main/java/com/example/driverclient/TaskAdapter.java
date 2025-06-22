package com.example.driverclient;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soniceyes.R;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<DriverTask> taskList;
    private Context context;

    public TaskAdapter(List<DriverTask> taskList, Context context) {
        this.taskList = taskList;
        this.context = context;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_driver_task, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        DriverTask task = taskList.get(position);

        holder.tvTaskTitle.setText("运输任务 (顺序: " + task.getDriverOrder() + ")");
        holder.tvStartPoint.setText("起点: " + task.getStartPointName());
        holder.tvEndPoint.setText("终点: " + task.getEndPointName());

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(task.getDate());
            holder.tvTaskDate.setText("日期: " + outputFormat.format(date));
        } catch (Exception e) {
            holder.tvTaskDate.setText("日期: " + task.getDate());
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Driver_taskDetailActivity.class);
            intent.putExtra("startName", task.getStartPointName());
            intent.putExtra("endName", task.getEndPointName());
            intent.putExtra("date", task.getDate());
            intent.putExtra("order", task.getDriverOrder());
            intent.putExtra("startX", task.getStartX());
            intent.putExtra("startY", task.getStartY());
            intent.putExtra("endX", task.getEndX());
            intent.putExtra("endY", task.getEndY());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTaskTitle, tvStartPoint, tvEndPoint, tvTaskDate;

        public TaskViewHolder(View view) {
            super(view);
            tvTaskTitle = view.findViewById(R.id.tv_task_title);
            tvStartPoint = view.findViewById(R.id.tv_start_point);
            tvEndPoint = view.findViewById(R.id.tv_end_point);
            tvTaskDate = view.findViewById(R.id.tv_task_date);
        }
    }
} 