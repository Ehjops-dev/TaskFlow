package com.example.taskflow;

// ============================================================
//  ReminderScheduler.java  —  AlarmManager helper
//  TEAM MEMBER: assign to Member B  (same as ReminderReceiver)
//
//  Responsibilities:
//    • Schedule an exact alarm for a task's reminderTime
//    • Cancel an alarm when a task is deleted or reminder removed
//    • Re-schedule daily alarms 24 h after firing
// ============================================================

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public class ReminderScheduler {

    /**
     * Schedule (or re-schedule) an alarm for the given task.
     * If reminderTime is empty or task is already completed, does nothing.
     */
    public static void schedule(Context context, Task task) {
        if (!task.hasReminder() || task.isCompleted()) return;

        String[] parts = task.getReminderTime().split(":");
        if (parts.length != 2) return;

        int hour, minute;
        try {
            hour   = Integer.parseInt(parts[0]);
            minute = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return;
        }

        Calendar cal = Calendar.getInstance();

        if (task.isOneTime() && !task.getEventDate().isEmpty()) {
            // Parse yyyy-MM-dd
            String[] dateParts = task.getEventDate().split("-");
            if (dateParts.length == 3) {
                try {
                    cal.set(Calendar.YEAR,        Integer.parseInt(dateParts[0]));
                    cal.set(Calendar.MONTH,       Integer.parseInt(dateParts[1]) - 1);
                    cal.set(Calendar.DAY_OF_MONTH,Integer.parseInt(dateParts[2]));
                } catch (NumberFormatException ignored) {}
            }
        } else {
            // Daily: fire today if time hasn't passed, otherwise tomorrow
            Calendar now = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE,      minute);
            cal.set(Calendar.SECOND,      0);
            if (cal.before(now)) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE,      minute);
        cal.set(Calendar.SECOND,      0);
        cal.set(Calendar.MILLISECOND, 0);

        PendingIntent pi = buildPendingIntent(context, task);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            // Fallback to inexact if exact-alarm permission not granted
            am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        }
    }

    /** Cancel any alarm that was previously set for this task. */
    public static void cancel(Context context, Task task) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        am.cancel(buildPendingIntent(context, task));
    }

    /**
     * Called from ReminderReceiver to re-fire a daily alarm exactly 24 h later.
     * The original Intent from the receiver is reused so all extras are preserved.
     */
    public static void rescheduleDaily(Context context, Intent originalIntent) {
        long taskId     = originalIntent.getLongExtra(ReminderReceiver.EXTRA_TASK_ID, -1);
        String title    = originalIntent.getStringExtra(ReminderReceiver.EXTRA_TASK_TITLE);
        String colorHex = originalIntent.getStringExtra(ReminderReceiver.EXTRA_TASK_COLOR);

        if (taskId == -1) return;

        Intent alarmIntent = new Intent(context, ReminderReceiver.class);
        alarmIntent.putExtra(ReminderReceiver.EXTRA_TASK_ID,    taskId);
        alarmIntent.putExtra(ReminderReceiver.EXTRA_TASK_TITLE, title);
        alarmIntent.putExtra(ReminderReceiver.EXTRA_IS_DAILY,   true);
        alarmIntent.putExtra(ReminderReceiver.EXTRA_TASK_COLOR, colorHex);

        PendingIntent pi = PendingIntent.getBroadcast(
                context, (int) taskId, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long nextFire = System.currentTimeMillis() + AlarmManager.INTERVAL_DAY;

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.set(AlarmManager.RTC_WAKEUP, nextFire, pi);
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextFire, pi);
        }
    }

    // ── Private helper ────────────────────────────────────────

    private static PendingIntent buildPendingIntent(Context context, Task task) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(ReminderReceiver.EXTRA_TASK_ID,    task.getId());
        intent.putExtra(ReminderReceiver.EXTRA_TASK_TITLE, task.getTitle());
        intent.putExtra(ReminderReceiver.EXTRA_IS_DAILY,   task.isDaily());
        intent.putExtra(ReminderReceiver.EXTRA_TASK_COLOR, task.getColor());

        return PendingIntent.getBroadcast(
                context, (int) task.getId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
