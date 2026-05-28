package com.example.taskflow;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskActionListener {
        void onEdit(Task task, int position);
        void onTaskChanged();
    }

    private List<Task> taskList;
    private OnTaskActionListener listener;

    public TaskAdapter(List<Task> taskList, OnTaskActionListener listener) {
        this.taskList = taskList;
        this.listener = listener;
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
        holder.priority.setText(task.getPriority());

        switch (task.getPriority()) {

            case "High":
                holder.priority.setTextColor(0xFFDC2626);
                break;

            case "Medium":
                holder.priority.setTextColor(0xFFD97706);
                break;

            case "Low":
                holder.priority.setTextColor(0xFF16A34A);
                break;
        }

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isDone());

        if (task.isDone()) {

            holder.title.setPaintFlags(
                    holder.title.getPaintFlags()
                            | Paint.STRIKE_THRU_TEXT_FLAG
            );

        } else {

            holder.title.setPaintFlags(
                    holder.title.getPaintFlags()
                            & (~Paint.STRIKE_THRU_TEXT_FLAG)
            );
        }

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            int pos = holder.getAdapterPosition();

            if (pos != RecyclerView.NO_POSITION) {

                taskList.get(pos).setDone(isChecked);

                Collections.sort(taskList, new Comparator<Task>() {
                    @Override
                    public int compare(Task t1, Task t2) {
                        return Boolean.compare(t1.isDone(), t2.isDone());
                    }
                });

                notifyDataSetChanged();

                listener.onTaskChanged();
            }
        });

        holder.deleteButton.setOnClickListener(v -> {

            int pos = holder.getAdapterPosition();

            if (pos != RecyclerView.NO_POSITION) {

                taskList.remove(pos);

                notifyItemRemoved(pos);

                listener.onTaskChanged();
            }
        });

        holder.itemView.setOnLongClickListener(v -> {

            int pos = holder.getAdapterPosition();

            if (pos != RecyclerView.NO_POSITION) {
                listener.onEdit(taskList.get(pos), pos);
            }

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView priority;
        CheckBox checkBox;
        ImageView deleteButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.taskTitle);
            priority = itemView.findViewById(R.id.taskPriority);
            checkBox = itemView.findViewById(R.id.taskCheck);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}