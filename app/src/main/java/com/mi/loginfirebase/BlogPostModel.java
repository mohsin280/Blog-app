package com.mi.loginfirebase;

import java.util.Comparator;
import java.util.Date;

public class BlogPostModel extends BlogPostId {
    private String desc, image_url, thumb_url, user_id, grp_code;
    private Date timestamp;

    public BlogPostModel(){ }

    public BlogPostModel(String desc, String image_url, String thumb_url, String user_id, String grp_code, Date timestamp) {
        this.desc = desc;
        this.image_url = image_url;
        this.thumb_url = thumb_url;
        this.user_id = user_id;
        this.grp_code = grp_code;
        this.timestamp = timestamp;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getGrp_code() {
        return grp_code;
    }

    public void setGrp_code(String grp_code) {
        this.grp_code = grp_code;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    static class  SortByDate implements Comparator<BlogPostModel> {


        @Override
        public int compare(BlogPostModel o1, BlogPostModel o2) {
            if(o1==null || o2==null){
                return 0;
            }
            return Integer.compare(0, o1.timestamp.compareTo(o2.timestamp));
        }
    }

    @Override
    public String toString() {
        return "BlogPostModel{" +
                "desc='" + desc + '\'' +
                ", image_url='" + image_url + '\'' +
                ", thumb_url='" + thumb_url + '\'' +
                ", user_id='" + user_id + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
