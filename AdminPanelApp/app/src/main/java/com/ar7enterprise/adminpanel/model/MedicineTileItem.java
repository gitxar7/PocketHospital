package com.ar7enterprise.adminpanel.model;

public class MedicineTileItem {
    private String id;
    private String name;
    private String text1;
    private String text2;
    private String status;

    public MedicineTileItem(String id, String name, String text1, String text2, String status) {
        this.id = id;
        this.name = name;
        this.text1 = text1;
        this.text2 = text2;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
