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

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskActionListener {
        void onEdit(Task task, int position);
        void onTaskChanged();
    }

    private final List<Task> taskList;
    private final OnTaskActionListener listener;
    private boolean isDarkMode = false;

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

        int priorityColor = 0xFF6B7280; // Default Gray

        switch (task.getPriority()) {
            case "High":
                priorityColor = 0xFFDC2626; // Red
                break;
            case "Medium":
                priorityColor = 0xFFD97706; // Amber
                break;
            case "Low":
                priorityColor = 0xFF16A34A; // Green
                break;
        }

        holder.priority.setTextColor(priorityColor);
        holder.priorityIndicator.setBackgroundColor(priorityColor);

        // Dark Mode support
        com.google.android.material.card.MaterialCardView card = (com.google.android.material.card.MaterialCardView) holder.itemView;
        if (isDarkMode) {
            card.setCardBackgroundColor(android.graphics.Color.parseColor("#1E1E1E"));
            card.setStrokeColor(android.graphics.Color.parseColor("#333333"));
            holder.title.setTextColor(android.graphics.Color.WHITE);
            // Ensure priority text is visible in dark mode
            if (priorityColor == 0xFF6B7280) { // Default Gray
                holder.priority.setTextColor(android.graphics.Color.parseColor("#9CA3AF"));
            } else {
                holder.priority.setTextColor(priorityColor);
            }
        } else {
            card.setCardBackgroundColor(android.graphics.Color.WHITE);
            card.setStrokeColor(android.graphics.Color.parseColor("#E5E7EB"));
            holder.title.setTextColor(android.graphics.Color.parseColor("#111827"));
            holder.priority.setTextColor(priorityColor);
        }

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isCompleted());

        if (task.isCompleted()) {

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

            int pos = holder.getBindingAdapterPosition();

            if (pos != RecyclerView.NO_POSITION) {

                taskList.get(pos).setCompleted(isChecked);

                taskList.sort((t1, t2) -> Boolean.compare(t1.isCompleted(), t2.isCompleted()));

                notifyDataSetChanged();

                listener.onTaskChanged();
            }
        });

        holder.deleteButton.setOnClickListener(v -> {

            int pos = holder.getBindingAdapterPosition();

            if (pos != RecyclerView.NO_POSITION) {

                taskList.remove(pos);

                notifyItemRemoved(pos);

                listener.onTaskChanged();
            }
        });

        holder.itemView.setOnLongClickListener(v -> {

            int pos = holder.getBindingAdapterPosition();

            if (pos != RecyclerView.NO_POSITION) {
                listener.onEdit(taskList.get(pos), pos);
            }

            return true;
        });
    }

    public void setDarkMode(boolean darkMode) {
        this.isDarkMode = darkMode;
        notifyDataSetChanged();
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
        View priorityIndicator;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.taskTitle);
            priority = itemView.findViewById(R.id.taskPriority);
            checkBox = itemView.findViewById(R.id.taskCheck);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);
        }
    }
}