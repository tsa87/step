package com.google.sps.model;

import java.util.Date;
import com.google.appengine.api.datastore.Entity;

public class Image {
    
  private long id;
  private String imageUrl;
  private String userName;
  private String caption;
  private Date creationTime;
  private long likeCount;

  private Image(long id, String userName, String imageUrl, String caption, Date creationTime, long likeCount) {
    this.id = id;
    this.imageUrl = imageUrl;
    this.userName = userName;
    this.caption = caption;
    this.creationTime = creationTime;
    this.likeCount = likeCount;
  }

  public static Entity toEntity(String imageUrl, String userName, String caption) {
    Entity imageEntity = new Entity("Image");

    imageEntity.setProperty("userName", userName);
    imageEntity.setProperty("imageUrl", imageUrl);
    imageEntity.setProperty("caption", caption);
    imageEntity.setProperty("timestamp", new Date());
    imageEntity.setProperty("like", 0);

    return imageEntity;
  }

  public static Image toImage(Entity imageEntity) {
    long id = imageEntity.getKey().getId();
    String userName = (String) imageEntity.getProperty("userName");
    String imageUrl = (String) imageEntity.getProperty("imageUrl");
    String caption = (String) imageEntity.getProperty("caption");
    Date creationTime = (Date) imageEntity.getProperty("timestamp");
    long likeCount = (long) imageEntity.getProperty("like");

    return new Image(id, userName, imageUrl, caption, creationTime, likeCount);
  }

}