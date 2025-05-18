package my.edu.utar.utarcommunity;

import java.util.Date;

public class Post {
    private int ID;
    private User user;
    private String caption;
    private String tags;
    private int like;
    private int comment;
    private Date created;
    private boolean isAnonymous = false;
    public int getID() {
        return ID;
    }
    public void setID(int ID) {
        this.ID = ID;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public String getCaption() {
        return caption;
    }
    public void setCaption(String caption) {
        this.caption = caption;
    }
    public String getTags() {
        return tags;
    }
    public void setTags(String tags) {
        this.tags = tags;
    }
    public int getLike() {
        return like;
    }
    public void setLike(int like) {
        this.like = like;
    }
    public int getComment() {
        return comment;
    }
    public void setComment(int comment) {
        this.comment = comment;
    }
    public Date getCreated() {
        return created;
    }
    public void setCreated(Date created) {
        this.created = created;
    }
    public boolean getIsAnonymous() {
        return isAnonymous;
    }
    public void setIsAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

}