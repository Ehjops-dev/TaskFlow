package com.example.taskflow;

// ============================================================
//  BootReceiver.java  —  Re-schedule alarms after reboot
//  TEAM MEMBER: assign to Member B
//
//  Android cancels all alarms when the device reboots.
//  This receiver fires on BOOT_COMPLETED and re-schedules
//  every task that has a reminder set.
// ============================================================

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        SharedPreferences prefs = context.getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE);
        String tasksJson = prefs.getString("tasks", null);
        if (tasksJson == null) return;

        try {
            JSONArray array = new JSONArray(tasksJson);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Task task = new Task(
                        obj.getString("title"),
                        obj.getString("priority"),
                        obj.optBoolean("isCompleted", false),
                        obj.getLong("id"),
                        obj.optString("taskType", Task.TYPE_DAILY),
                        obj.optString("reminderTime", ""),
                        obj.optString("eventDate", ""),
                        obj.optString("color", "#6366F1")
                );
                // Re-schedule only if task has a reminder and isn't done
                if (task.hasReminder() && !task.isCompleted()) {
                    ReminderScheduler.schedule(context, task);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
