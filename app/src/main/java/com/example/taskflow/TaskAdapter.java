package com.example.taskflow;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    List<Task> taskList;

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {

        holder.itemView.setTranslationX(0f);
        
        Task task = taskList.get(position);

        holder.title.setText(task.getTitle());

        // IMPORTANT: remove old listener first (prevents crashes)
        holder.checkBox.setOnCheckedChangeListener(null);

        holder.checkBox.setChecked(task.isDone());

        // Strike-through logic
        if (task.isDone()) {
            holder.title.setPaintFlags(holder.title.getPaintFlags()
                    | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.title.setPaintFlags(holder.title.getPaintFlags()
                    & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Safe click handling
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            int adapterPosition = holder.getAdapterPosition();

            if (adapterPosition != RecyclerView.NO_POSITION) {
                taskList.get(adapterPosition).setDone(isChecked);
                notifyItemChanged(adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        CheckBox checkBox;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.taskTitle);
            checkBox = itemView.findViewById(R.id.taskCheck);
        }
    }
}