package my.edu.utar.utarcommunity;

import java.time.LocalDateTime;

public class Notify {
    private int post_ID;
    private User user;
    private LocalDateTime notify_Created;
    private String notify_Type;

    private boolean isAnonymous;

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    public void setPost_ID(int post_ID) {
        this.post_ID = post_ID;
    }
    public int getPost_ID() {
        return post_ID;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
    public LocalDateTime getNotify_Created() {
        return notify_Created;
    }

    public void setNotify_Created(LocalDateTime notify_Created) {
        this.notify_Created = notify_Created;
    }

    public String getNotify_Type() {
        return notify_Type;
    }

    public void setNotify_Type(String notify_Type) {
        this.notify_Type = notify_Type;
    }

    public Notify(){


    }
}