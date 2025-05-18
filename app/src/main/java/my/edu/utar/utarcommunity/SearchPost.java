package my.edu.utar.utarcommunity;

import java.util.Date;

public class SearchPost {
    private int post_ID;
    private String post_Caption;
    private String post_Tags;
    private int post_Like;
    private int post_Comment;
    private Date post_Created;
    private boolean post_isAnonymous;
    private String user_id;
    private String user_Name;
    private boolean liked_ByUser;

    public boolean isLiked_ByUser() {
        return liked_ByUser;
    }

    public void setLiked_ByUser(boolean liked_ByUser) {
        this.liked_ByUser = liked_ByUser;
    }

    public int getPost_ID() {
        return post_ID;
    }

    public void setPost_ID(int post_ID) {
        this.post_ID = post_ID;
    }

    public String getPost_Caption() {
        return post_Caption;
    }

    public void setPost_Caption(String post_Caption) {
        this.post_Caption = post_Caption;
    }

    public String getPost_Tags() {
        return post_Tags;
    }

    public void setPost_Tags(String post_Tags) {
        this.post_Tags = post_Tags;
    }

    public int getPost_Like() {
        return post_Like;
    }

    public void setPost_Like(int post_Like) {
        this.post_Like = post_Like;
    }

    public int getPost_Comment() {
        return post_Comment;
    }

    public void setPost_Comment(int post_Comment) {
        this.post_Comment = post_Comment;
    }

    public Date getPost_Created() {
        return post_Created;
    }

    public void setPost_Created(Date post_Created) {
        this.post_Created = post_Created;
    }

    public boolean isPost_isAnonymous() {
        return post_isAnonymous;
    }

    public void setPost_isAnonymous(boolean post_isAnonymous) {
        this.post_isAnonymous = post_isAnonymous;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_Name() {
        return user_Name;
    }

    public void setUser_Name(String user_Name) {
        this.user_Name = user_Name;
    }


    @Override
    public String toString() {
        return "SearchPost{" +
                "post_ID=" + post_ID +
                ", post_Caption='" + post_Caption + '\'' +
                ", post_Tags='" + post_Tags + '\'' +
                ", post_Like=" + post_Like +
                ", post_Comment=" + post_Comment +
                ", post_Created=" + post_Created +
                ", post_isAnonymous=" + post_isAnonymous +
                ", user_id='" + user_id + '\'' +
                ", user_Name='" + user_Name + '\'' +
                ", liked_ByUser=" + liked_ByUser +
                '}';
    }
}