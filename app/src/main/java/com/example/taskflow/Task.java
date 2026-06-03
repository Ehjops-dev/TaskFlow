package com.example.taskflow;

// ============================================================
//  Task.java  —  Data model
//  TEAM MEMBER: assign to Member A
//  Changes from original:
//    + taskType  ("daily" | "one_time")
//    + reminderTime  (e.g. "08:00")
//    + eventDate  (e.g. "2025-07-04")  — used for one-time tasks
//    + color  (hex string for card accent, e.g. "#6366F1")
// ============================================================

public class Task {

    // ── Task types ────────────────────────────────────────────
    public static final String TYPE_DAILY    = "daily";
    public static final String TYPE_ONE_TIME = "one_time";

    // ── Fields ────────────────────────────────────────────────
    private String  title;
    private String  priority;
    private boolean isCompleted;
    private final long id;

    /** "daily" or "one_time" */
    private String taskType;

    /** HH:mm format, e.g. "08:30". Empty string means no reminder. */
    private String reminderTime;

    /** yyyy-MM-dd format, only used when taskType == "one_time". */
    private String eventDate;

    /** Hex colour string for the left accent strip, e.g. "#6366F1". */
    private String color;

    // ── Constructors ──────────────────────────────────────────

    /** Convenience constructor (backwards-compatible, no reminder). */
    public Task(String title, String priority) {
        this(title, priority, false, System.currentTimeMillis(),
             TYPE_DAILY, "", "", "#6366F1");
    }

    /** Full constructor used when loading from SharedPreferences. */
    public Task(String title, String priority, boolean isCompleted, long id,
                String taskType, String reminderTime, String eventDate, String color) {
        this.title        = title;
        this.priority     = priority;
        this.isCompleted  = isCompleted;
        this.id           = id;
        this.taskType     = taskType     != null ? taskType     : TYPE_DAILY;
        this.reminderTime = reminderTime != null ? reminderTime : "";
        this.eventDate    = eventDate    != null ? eventDate    : "";
        this.color        = color        != null ? color        : "#6366F1";
    }

    /** Legacy constructor kept for backwards compatibility. */
    public Task(String title, String priority, boolean isCompleted, long id) {
        this(title, priority, isCompleted, id, TYPE_DAILY, "", "", "#6366F1");
    }

    // ── Getters & Setters ─────────────────────────────────────

    public String  getTitle()        { return title; }
    public String  getPriority()     { return priority; }
    public boolean isCompleted()     { return isCompleted; }
    public long    getId()           { return id; }
    public String  getTaskType()     { return taskType; }
    public String  getReminderTime() { return reminderTime; }
    public String  getEventDate()    { return eventDate; }
    public String  getColor()        { return color; }

    public void setTitle(String title)               { this.title        = title; }
    public void setPriority(String priority)         { this.priority     = priority; }
    public void setCompleted(boolean completed)      { this.isCompleted  = completed; }
    public void setTaskType(String taskType)         { this.taskType     = taskType; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }
    public void setEventDate(String eventDate)       { this.eventDate    = eventDate; }
    public void setColor(String color)               { this.color        = color; }

    // ── Helpers ───────────────────────────────────────────────

    public boolean isDaily()   { return TYPE_DAILY.equals(taskType); }
    public boolean isOneTime() { return TYPE_ONE_TIME.equals(taskType); }
    public boolean hasReminder() { return reminderTime != null && !reminderTime.isEmpty(); }
}
