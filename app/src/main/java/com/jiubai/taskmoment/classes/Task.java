package com.jiubai.taskmoment.classes;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务类
 */
public class Task {
    private String portraitUrl;
    private String nickname;
    private String grade;
    private String desc;
    private ArrayList<String> pictures;
    private String date;
    private ArrayList<Comment> comments;

    public Task() {
    }

    public Task(String portraitUrl, String nickname, String grade,
                String desc, ArrayList<String> pictures, String date, ArrayList<Comment> comments) {
        this.portraitUrl = portraitUrl;
        this.nickname = nickname;
        this.grade = grade;
        this.desc = desc;
        this.pictures = pictures;
        this.date = date;
        this.comments = comments;
    }

    public String getPortraitUrl() {
        return portraitUrl;
    }

    public void setPortraitUrl(String portraitUrl) {
        this.portraitUrl = portraitUrl;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public ArrayList<String> getPictures() {
        return pictures;
    }

    public void setPictures(ArrayList<String> pictures) {
        this.pictures = pictures;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }
}
