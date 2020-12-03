package com.example.chatbase.Chat;

import com.example.chatbase.User.UserObjects;
import com.onesignal.OneSignal;

import java.io.Serializable;
import java.util.ArrayList;

public class ChatObject implements Serializable {
    private String name;
    private String chatId;

    private ArrayList<UserObjects> userObjectsArrayList = new ArrayList<>();
    public ChatObject(String chatId) {
        this.chatId = chatId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public ArrayList<UserObjects> getUserObjectsArrayList() {
        return userObjectsArrayList;
    }
    public void addUserToArrayList(UserObjects mUser){
        userObjectsArrayList.add(mUser);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
