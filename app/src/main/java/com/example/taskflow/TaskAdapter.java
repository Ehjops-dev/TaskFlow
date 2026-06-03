package com.example.taskflow;

// ============================================================
//  TaskAdapter.java  —  RecyclerView Adapter
//  TEAM MEMBER: assign to Member C
//
//  Changes from original:
//    + Renders the task's custom colour on the left strip
//    + Shows a "🔁 Daily" or "📅 One-Time" badge
//    + Shows the reminder time below the title
//    + Dark-mode colours updated to work with new fields
// ============================================================

import android.graphics.Color;
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

        // ── Title ─────────────────────────────────────────────
        holder.title.setText(task.getTitle());

        // ── Priority colour ───────────────────────────────────
        int priorityColor;
        switch (task.getPriority()) {
            case "High":   priorityColor = 0xFFDC2626; break;
            case "Medium": priorityColor = 0xFFD97706; break;
            case "Low":    priorityColor = 0xFF16A34A; break;
            default:       priorityColor = 0xFF6B7280;
        }
        holder.priority.setText(task.getPriority());
        holder.priority.setTextColor(priorityColor);

        // ── Custom colour strip ───────────────────────────────
        int accentColor = priorityColor; // fallback to priority colour
        if (task.getColor() != null && !task.getColor().isEmpty()) {
            try { accentColor = Color.parseColor(task.getColor()); }
            catch (IllegalArgumentException ignored) {}
        }
        holder.priorityIndicator.setBackgroundColor(accentColor);

        // ── Type badge & reminder time ────────────────────────
        if (task.isDaily()) {
            holder.typeBadge.setText("🔁 Daily");
            holder.typeBadge.setTextColor(Color.parseColor("#6366F1")); // indigo
        } else {
            holder.typeBadge.setText("📅 One-Time");
            holder.typeBadge.setTextColor(Color.parseColor("#0EA5E9")); // sky blue
        }

        if (task.hasReminder()) {
            holder.reminderTime.setVisibility(View.VISIBLE);
            holder.reminderTime.setText("⏰ " + task.getReminderTime());
        } else {
            holder.reminderTime.setVisibility(View.GONE);
        }

        if (task.isOneTime() && !task.getEventDate().isEmpty()) {
            holder.eventDate.setVisibility(View.VISIBLE);
            holder.eventDate.setText("📆 " + task.getEventDate());
        } else {
            holder.eventDate.setVisibility(View.GONE);
        }

        // ── Dark mode card styling ────────────────────────────
        com.google.android.material.card.MaterialCardView card =
                (com.google.android.material.card.MaterialCardView) holder.itemView;
        if (isDarkMode) {
            card.setCardBackgroundColor(Color.parseColor("#1E1E1E"));
            card.setStrokeColor(Color.parseColor("#333333"));
            holder.title.setTextColor(Color.WHITE);
            holder.reminderTime.setTextColor(Color.parseColor("#9CA3AF"));
            holder.eventDate.setTextColor(Color.parseColor("#9CA3AF"));
        } else {
            card.setCardBackgroundColor(Color.WHITE);
            card.setStrokeColor(Color.parseColor("#E5E7EB"));
            holder.title.setTextColor(Color.parseColor("#111827"));
            holder.reminderTime.setTextColor(Color.parseColor("#6B7280"));
            holder.eventDate.setTextColor(Color.parseColor("#6B7280"));
        }

        // ── Strike-through for completed tasks ────────────────
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isCompleted());
        if (task.isCompleted()) {
            holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            card.setAlpha(0.6f);
        } else {
            holder.title.setPaintFlags(holder.title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            card.setAlpha(1f);
        }

        // ── Checkbox ──────────────────────────────────────────
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            taskList.get(pos).setCompleted(isChecked);

            // Match MainActivity's dual-sorting logic
            taskList.sort((t1, t2) -> {
                int comp = Boolean.compare(t1.isCompleted(), t2.isCompleted());
                if (comp != 0) return comp;

                // Helper to get priority value for sorting
                int p1 = getPrioritySortValue(t1.getPriority());
                int p2 = getPrioritySortValue(t2.getPriority());
                return Integer.compare(p1, p2);
            });

            notifyDataSetChanged();
            listener.onTaskChanged();
        });

        // ── Delete button ─────────────────────────────────────
        holder.deleteButton.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            // Cancel alarm before removing
            ReminderScheduler.cancel(v.getContext(), taskList.get(pos));
            taskList.remove(pos);
            notifyItemRemoved(pos);
            listener.onTaskChanged();
        });

        // ── Long-press to edit ────────────────────────────────
        holder.itemView.setOnLongClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onEdit(taskList.get(pos), pos);
            }
            return true;
        });
    }

    private int getPrioritySortValue(String p) {
        switch (p) {
            case "High":   return 1;
            case "Medium": return 2;
            case "Low":    return 3;
            default:       return 4;
        }
    }

    public void setDarkMode(boolean darkMode) {
        this.isDarkMode = darkMode;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() { return taskList.size(); }

    // ── ViewHolder ────────────────────────────────────────────
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView  title;
        TextView  priority;
        TextView  typeBadge;
        TextView  reminderTime;
        TextView  eventDate;
        CheckBox  checkBox;
        ImageView deleteButton;
        View      priorityIndicator;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title             = itemView.findViewById(R.id.taskTitle);
            priority          = itemView.findViewById(R.id.taskPriority);
            typeBadge         = itemView.findViewById(R.id.taskTypeBadge);
            reminderTime      = itemView.findViewById(R.id.taskReminderTime);
            eventDate         = itemView.findViewById(R.id.taskEventDate);
            checkBox          = itemView.findViewById(R.id.taskCheck);
            deleteButton      = itemView.findViewById(R.id.deleteButton);
            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);
        }
    }
}
