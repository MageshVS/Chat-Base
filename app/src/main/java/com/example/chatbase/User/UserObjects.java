package com.example.chatbase.User;

import java.io.Serializable;

public class UserObjects implements Serializable {
    private String name, phone ,Uid, notificationKey;
    private boolean selected = false;
    public UserObjects(String uid, String name, String phone) {
        this.Uid = uid;
        this.name = name;
        this.phone = phone;
    }
    public UserObjects(String uid) {
        this.Uid = uid;
    }


    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setNotificationKey(String notificationKey) {
        this.notificationKey = notificationKey;
    }

    public String getNotificationKey() {
        return notificationKey;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
