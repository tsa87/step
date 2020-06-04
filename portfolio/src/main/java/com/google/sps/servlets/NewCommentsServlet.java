package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import java.util.Date;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/new-comments")
public class NewCommentsServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    String content = getParameter(request, "text-comment", "this message was empty...");
    String userName = getParameter(request, "text-name", "anonymous");
    boolean isAnonymous = Boolean.parseBoolean(getParameter(request, "anonymous", "false"));
    userName = isAnonymous ? "anonymous" : userName;

    // MAKE INTO A STATIC METHOD
    Entity commentEntity = new Entity("Comment");

    commentEntity.setProperty("userName", userName);
    commentEntity.setProperty("content", content);
    commentEntity.setProperty("timestamp", new Date());
    commentEntity.setProperty("like", 0);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    response.sendRedirect("/blog.html");
  }

  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    value = (value == null) ? defaultValue : value;
    return value;
  }
}