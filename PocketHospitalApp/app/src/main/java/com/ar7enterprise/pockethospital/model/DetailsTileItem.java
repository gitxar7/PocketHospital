package com.ar7enterprise.pockethospital.model;

public class DetailsTileItem {
    private String title;
    private String text1;
    private String text2;
    private String id;

    public DetailsTileItem(String title, String text1, String text2, String id) {
        this.title = title;
        this.text1 = text1;
        this.text2 = text2;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText1() {
        return text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
