package com.google.sps.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CommentManager {
    private List<Comment> commentList; 

    public List<Comment> getCommentList() {
        return commentList;
    }

    public List<Comment> getNRecentComments(int count) {
        Collections.sort(commentList, 
            (comment1, comment2) -> ((int) (comment1.getTime() - comment2.getTime()))
        );
        return commentList.subList(0, Math.min(commentList.size(), count));
    }

    public void setCommentList(List<Comment> commentList) {
        this.commentList = commentList;
    }

    public void addComment(Comment comment) {
        commentList.add(comment);
    }

    /* Singleton Support */
    private static CommentManager commentManager; 

    private CommentManager()  {
        commentList = new ArrayList<>();
    }

    public static CommentManager getInstance() {
        if (commentManager == null) {
            commentManager = new CommentManager();
        }
        return commentManager;
    }
}