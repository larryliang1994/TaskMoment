package com.jiubai.taskmoment.classes;

/**
 * 公司类
 */
public class Company {
    private String name;
    private String cid;

    public Company(String name, String cid) {
        this.name = name;
        this.cid = cid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }
}
