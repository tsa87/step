package com.google.sps.model;

public class Comment {
    private long id;
    private String content;
    private String userName;
    private long time;
    private long like;

    public Comment(long id, String userName, String content, long time, long like) {
        this.id = id;
        this.content = content;
        this.userName = userName;
        this.time = time;
        this.like = like; 
    }

    public void like() { 
        this.like ++;
    }

    public long getID() {
        return this.id;
    }

    public String getContent() { 
        return this.content; 
    }
    public String getUserName() {
        return this.userName; 
    }
    public long getTime() { 
        return this.time; 
    }
    public long getLike() { 
        return this.like; 
    }
}
