package com.google.sps.model;

import java.util.Date;

public class Comment {
  private long id;
  private String content;
  private String userName;
  private Date time;
  private long like;

  public Comment(long id, String userName, String content, Date time, long like) {
    this.id = id;
    this.content = content;
    this.userName = userName;
    this.time = time;
    this.like = like;
  }

  public void like() {
    this.like++;
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
  public Date getTime() {
    return this.time;
  }
  public long getLike() {
    return this.like;
  }
}