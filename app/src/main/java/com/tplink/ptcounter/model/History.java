package com.tplink.ptcounter.model;

public class History {

    private String date;
    private String content;

    public History() {
    }

    public History(String date, String type) {
        this.date = date;
        this.content = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
