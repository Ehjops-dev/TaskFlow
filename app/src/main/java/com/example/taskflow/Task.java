package com.example.taskflow;

public class Task {

    private String title;
    private String priority;
    private boolean isDone;
    private long id;

    public Task(String title, String priority) {
        this.title = title;
        this.priority = priority;
        this.isDone = false;
        this.id = System.currentTimeMillis();
    }

    public Task(String title, String priority, boolean isDone, long id) {
        this.title = title;
        this.priority = priority;
        this.isDone = isDone;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getPriority() {
        return priority;
    }

    public boolean isDone() {
        return isDone;
    }

    public long getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setDone(boolean done) {
        isDone = done;
    }
}