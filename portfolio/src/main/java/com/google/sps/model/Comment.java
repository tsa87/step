package com.google.sps.model;

import com.google.sps.model.Comment;
import java.util.Date;
import com.google.appengine.api.datastore.Entity;

public class Comment {
    
  private long id;
  private String content;
  private String userName;
  private Date creationTime;
  private long likeCount;
  private Double sentimentScore;

  private Comment(
		long id,
		String userName,
		String content,
		Date creationTime,
		long likeCount,
		Double sentimentScore
	) {

    this.id = id;
    this.content = content;
    this.userName = userName;
    this.creationTime = creationTime;
    this.likeCount = likeCount;
    this.sentimentScore = sentimentScore;

  }

  public static Entity toEntity(String userName, String content, float sentimentScore) {
    Entity commentEntity = new Entity("Comment");

    commentEntity.setProperty("userName", userName);
    commentEntity.setProperty("content", content);
    commentEntity.setProperty("timestamp", new Date());
    commentEntity.setProperty("like", 0);
    commentEntity.setProperty("sentiment", sentimentScore);

    return commentEntity;
  }

  public static Comment toComment(Entity commentEntity) {
    long id = commentEntity.getKey().getId();
    String userName = (String) commentEntity.getProperty("userName");
    String content = (String) commentEntity.getProperty("content");
    Date creationTime = (Date) commentEntity.getProperty("timestamp");
    long likeCount = (long) commentEntity.getProperty("like");
    Double sentimentScore = (Double) commentEntity.getProperty("sentiment");

    return new Comment(id, userName, content, creationTime, likeCount, sentimentScore);
  }

}
