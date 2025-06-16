package com.ar7enterprise.pockethospital.model;

import java.util.Date;

public class User {
    private int id;
    private String name;
    private String mobile;
    private String password;
    private Date birthday;
    private Date registered_date;
    private Gender gender;
    private City city;

    private Status status;

    public User() {
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMobile() {
        return mobile;
    }

    public String getPassword() {
        return password;
    }

    public Date getBirthday() {
        return birthday;
    }

    public Date getRegistered_date() {
        return registered_date;
    }

    public Gender getGender() {
        return gender;
    }

    public City getCity() {
        return city;
    }
}
