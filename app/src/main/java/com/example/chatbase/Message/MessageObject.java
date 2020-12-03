package com.example.chatbase.Message;

import java.util.ArrayList;

public class MessageObject {
    String messageId, senderId, message, creatorName;
    
    ArrayList<String> mediaUrlList;
    int messageNumber;

    public MessageObject(int messageNumber, String messageId, String senderId,String creatorName,  String message, ArrayList<String> mediaUrlList) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.message = message;
        this.mediaUrlList = mediaUrlList;
        this.creatorName = creatorName;
        this.messageNumber = messageNumber;
    }

    public void setMessageNumber(int messageNumber) {
        this.messageNumber = messageNumber;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<String> getMediaUrlList() {
        return mediaUrlList;
    }
}
