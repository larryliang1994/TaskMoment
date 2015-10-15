package com.jiubai.taskmoment.classes;

/**
 * 评论类
 */
public class Comment {
    private String sender;
    private String senderId;
    private String receiver;
    private String receiverId;
    private String content;
    private String time;

    public Comment() {
    }

    public Comment(String sender, String senderId, String content, String time) {
        this.sender = sender;
        this.senderId = senderId;
        this.content = content;
        this.time = time;
    }

    public Comment(String sender, String senderId, String receiver,
                   String receiverId, String content, String time) {
        this.sender = sender;
        this.senderId = senderId;
        this.receiver = receiver;
        this.receiverId = receiverId;
        this.content = content;
        this.time = time;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
