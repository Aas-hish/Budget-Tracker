package com.example.budgettracker;
public class Message {
    private String senderId;
    private String senderName;
    private String messageText;
    private String timestamp;

    public Message() { }

    public Message(String senderId, String senderName, String messageText, String timestamp) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getTimestamp() {
        return timestamp;

    }
}