package com.jiubai.taskmoment.classes;

/**
 * 消息类
 */
public class News {
    private String portrait;
    private String sender;
    private String senderID;
    private String content;
    private long create_time;
    private String picture;
    private String taskID;

    public News() {
    }

    public News(String portrait, String sender, String senderID,
                String content, long create_time, String picture, String taskID) {
        this.portrait = portrait;
        this.sender = sender;
        this.senderID = senderID;
        this.content = content;
        this.create_time = create_time;
        this.picture = picture;
        this.taskID = taskID;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreate_time() {
        return create_time;
    }

    public void setCreate_time(long create_time) {
        this.create_time = create_time;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }
}
