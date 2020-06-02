package com.google.sps.model;

import java.util.Date;

public class Comment {
    private String content;
    private String userName;
    private Date time;
    private int like;

    public Comment(String userName, String content) {
        this.content = content;
        this.userName = userName;
        this.time = new Date();
        this.like = 0; 
    }

    public void like() { 
        this.like ++;
    }

    public String getContent() { 
        return this.content; 
    }
    public String getUserName() {
        return this.userName; 
    }
    public Date getTime() { 
        return this.time; 
    }
    public int getLike() { 
        return this.like; 
    }
}
