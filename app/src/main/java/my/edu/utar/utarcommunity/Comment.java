package my.edu.utar.utarcommunity;

import java.time.LocalDateTime;
public class Comment {
    private int ID;
    private User user;
    private String content;
    private LocalDateTime created;

    private boolean isAnonymous;

    private int post_ID;

    public int getPost_ID() {
        return post_ID;
    }

    public void setPost_ID(int post_ID) {
        this.post_ID = post_ID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getID() {
        return ID;
    }

    public String getContent() {
        return content;
    }
    public LocalDateTime getCreated() {
        return created;
    }
    public boolean isAnonymous() {
        return isAnonymous;
    }
    public void setID(int ID) {
        this.ID = ID;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }
    public boolean getAnonymous() {

        return isAnonymous;

    }

}