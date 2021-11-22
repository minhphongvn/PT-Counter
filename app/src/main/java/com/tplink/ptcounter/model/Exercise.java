package com.tplink.ptcounter.model;

public class Exercise {
    private int resId;
    private String name;
    private String description;
    private String className;
    private boolean active;

    public Exercise() {
    }

    public Exercise(int resId, String name, String description, String className, boolean active) {
        this.resId = resId;
        this.name = name;
        this.description = description;
        this.className = className;
        this.active = active;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
