package com.mi.loginfirebase;

import java.util.Comparator;
import java.util.Date;

public class CommentModel {
    private String cmtId;
    private String comment;
    private Date timestamp;
    private String userId;
    private String postId;

    public CommentModel() {
    }

    public String getCmtId() {
        return cmtId;
    }

    public void setCmtId(String cmtId) {
        this.cmtId = cmtId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    @Override
    public String toString() {
        return "CommentModel{" +
                "cmtId='" + cmtId + '\'' +
                ", comment='" + comment + '\'' +
                ", timestamp=" + timestamp +
                ", userId='" + userId + '\'' +
                '}';
    }
}
