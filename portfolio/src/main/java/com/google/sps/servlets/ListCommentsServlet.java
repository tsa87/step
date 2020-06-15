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

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Comment> commentList = new ArrayList<>();
    
    for (Entity entity: results.asIterable()) {
      Comment comment = Comment.toComment(entity);
      commentList.add(comment);
    }

		int requestCommentCount = getRequestCount(request);		
    if (requestCommentCount < commentList.size()) {
	    commentList = commentList.subList(0, requestCommentCount);
    }

    String json = convertToJson(commentList);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  private int getRequestCount(HttpServletRequest request) {

    String countString = request.getParameter("count");
    int requestCommentCount = Integer.parseInt(countString);
	  
    if (requestCommentCount < 0 || requestCommentCount > 20) {
      throw new IllegalArgumentException("request comment count must be in between 0 and 20");
    }

    return requestCommentCount;
  }

  private String convertToJson(List<Comment> commentList) {
    Gson gson = new Gson();
    String json = gson.toJson(commentList);
    return json;
  }
}
