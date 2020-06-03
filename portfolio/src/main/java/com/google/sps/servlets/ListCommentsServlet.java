package com.google.sps.servlets;

import com.google.sps.model.Comment;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for listing tasks. */
@WebServlet("/list-comments")
public class ListCommentsServlet extends HttpServlet {

  private int requestCommentCount;

  @Override
  public void init() {
    requestCommentCount = 0;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Comment> commentList = new ArrayList<>();

    for (Entity entity: results.asIterable()) {

      long id = entity.getKey().getId();
      String userName = (String) entity.getProperty("userName");
      String content = (String) entity.getProperty("content");
      Date time = (Date) entity.getProperty("timestamp");
      long like = (long) entity.getProperty("like");

      commentList.add(new Comment(id, userName, content, time, like));
    }

    requestCommentCount = Math.min(commentList.size(), requestCommentCount);
    commentList = commentList.subList(0, requestCommentCount);

    String json = convertToJson(commentList);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    requestCommentCount = getRequestCount(request);
    response.sendRedirect("/blog.html");
  }

  private int getRequestCount(HttpServletRequest request) {

    String countString = request.getParameter("count");

    int requestCommentCount;
    try {
      requestCommentCount = Integer.parseInt(countString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + countString);
      return 0;
    }

    if (requestCommentCount<0 || requestCommentCount > 20) {
      System.err.println("Player choice is out of range: " + countString);
      return 0;
    }

    return requestCommentCount;
  }

  private String convertToJson(List<Comment> commentList) {
    Gson gson = new Gson();
    String json = gson.toJson(commentList);
    return json;
  }
}