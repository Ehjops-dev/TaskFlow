package com.example.taskflow;

// ============================================================
//  ReminderReceiver.java  —  BroadcastReceiver for alarms
//  TEAM MEMBER: assign to Member B
//
//  Responsibilities:
//    • Receives alarms set by ReminderScheduler
//    • Builds and posts a colourful notification for the task
//    • Re-schedules the alarm 24 h later for daily tasks
// ============================================================

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID   = "taskflow_reminders";
    public static final String CHANNEL_NAME = "Task Reminders";

    public static final String EXTRA_TASK_ID    = "task_id";
    public static final String EXTRA_TASK_TITLE = "task_title";
    public static final String EXTRA_IS_DAILY   = "is_daily";
    public static final String EXTRA_TASK_COLOR = "task_color";

    @Override
    public void onReceive(Context context, Intent intent) {

        long   taskId    = intent.getLongExtra(EXTRA_TASK_ID, -1);
        String taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE);
        boolean isDaily  = intent.getBooleanExtra(EXTRA_IS_DAILY, false);
        String colorHex  = intent.getStringExtra(EXTRA_TASK_COLOR);

        if (taskId == -1 || taskTitle == null) return;

        // ── Build notification ────────────────────────────────
        createNotificationChannel(context);

        Intent openApp = new Intent(context, MainActivity.class);
        openApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, (int) taskId, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        int accentColor = Color.parseColor("#6366F1"); // default indigo
        if (colorHex != null && !colorHex.isEmpty()) {
            try { accentColor = Color.parseColor(colorHex); }
            catch (IllegalArgumentException ignored) {}
        }

        String subText = isDaily ? "🔁 Daily Reminder" : "📅 One-Time Event";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(taskTitle)
                .setContentText(subText)
                .setColor(accentColor)
                .setColorized(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(subText + "\n\nTap to open TaskFlow."));

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int) taskId, builder.build());

        // ── Re-schedule daily tasks for tomorrow ──────────────
        if (isDaily) {
            ReminderScheduler.rescheduleDaily(context, intent);
        }
    }

    // ── Helper: create notification channel (required API 26+) ─
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Daily and one-time task reminders from TaskFlow");
            channel.enableLights(true);
            channel.setLightColor(Color.parseColor("#6366F1"));
            channel.enableVibration(true);

            NotificationManager nm =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(channel);
        }
    }
}
