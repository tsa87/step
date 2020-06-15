package com.google.sps.servlets;

import com.google.sps.model.Image;
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
@WebServlet("/list-images")
public class ListImagesServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    Query query = new Query("Image").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Image> imageList = new ArrayList<>();
    
    for (Entity entity: results.asIterable()) {
	  Image image = Image.toImage(entity);
      imageList.add(image);
    }

    String json = convertToJson(imageList);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  private String convertToJson(List<Image> imageList) {
    Gson gson = new Gson();
    String json = gson.toJson(imageList);
    return json;
  }
}