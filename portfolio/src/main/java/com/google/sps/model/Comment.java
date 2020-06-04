package com.google.sps.model;

import java.util.Date;

public class Comment {
  private long id;
  private String content;
  private String userName;
  private Date creationTime;
  private long likeCount;

  public Comment(long id, String userName, String content, Date creationTime, long likeCount) {
    this.id = id;
    this.content = content;
    this.userName = userName;
    this.creationTime = creationTime;
    this.likeCount = likeCount;
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
  public Date getCreationTime() {
    return this.creationTime;
  }
  public long getLikeCount() {
    return this.likeCount;
  }
}