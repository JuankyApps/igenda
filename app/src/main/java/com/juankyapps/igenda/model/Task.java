package com.juankyapps.igenda.model;

import java.util.UUID;

public class Task {
    private String id;
    private String title;
    private String desc;
    private boolean favorite;
    private boolean completed;

    public Task(String id, String title, String desc, boolean favorite, boolean completed) {
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.favorite = favorite;
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
