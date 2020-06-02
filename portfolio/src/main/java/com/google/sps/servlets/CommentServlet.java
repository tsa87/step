package com.google.sps.servlets;
 
import com.google.sps.model.CommentManager;
import com.google.sps.model.Comment;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
@WebServlet("/comment")
public class CommentServlet extends HttpServlet {
 
    private CommentManager commentManager;
 
    @Override
    public void init() {
        commentManager = CommentManager.getInstance();
    }
 
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        String content = getParameter(request, "text-comment", "this message was empty...");
        String userName = getParameter(request, "text-name", "anonymous");
        boolean isAnonymous = Boolean.parseBoolean(getParameter(request, "anonymous", "false"));
        userName = isAnonymous ? "anonymous" : userName;

        commentManager.addComment(new Comment(userName, content));
        
        response.sendRedirect("/blog.html");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String json = convertToJson(commentManager);
 
        response.setContentType("application/json;");
        response.getWriter().println(json);
    }
 
    private String convertToJson(CommentManager commentManager) {
        Gson gson = new Gson();
        String json = gson.toJson(commentManager.getCommentList());
        return json;
    }

    private String getParameter(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        value = (value == null) ? defaultValue : value;
        return value;
    }   
}
 

