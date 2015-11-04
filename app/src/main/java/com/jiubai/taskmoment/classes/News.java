package com.jiubai.taskmoment.classes;

import java.io.Serializable;

/**
 * 消息类
 */
public class News implements Serializable{
    private String senderID;
    private String type;
    private Task task;

    public News() {
    }

    public News(String senderID, String type, Task task) {
        this.senderID = senderID;
        this.type = type;
        this.task = task;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
