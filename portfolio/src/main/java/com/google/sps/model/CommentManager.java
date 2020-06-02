package com.google.sps.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class CommentManager {
    private List<Comment> commentList = new ArrayList<>();

    public List<Comment> getCommentList() {
        return commentList;
    }

    public void addComment(Comment comment) {
        commentList.add(comment);
    }

    /* Singleton Support */
    private static CommentManager commentManager; 

    private CommentManager()  {}

    public static CommentManager getInstance() {
        if (commentManager == null) {
            commentManager = new CommentManager();
        }
        return commentManager;
    }
}