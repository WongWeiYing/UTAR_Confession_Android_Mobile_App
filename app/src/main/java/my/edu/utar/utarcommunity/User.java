package my.edu.utar.utarcommunity;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private UUID ID;
    private String email;
    private String displayName;
    private String caption;
    private LocalDateTime created;
    private String name;
    private String gender;
    public User(UUID ID) {

        this.ID = ID;

    }
    public UUID getID() {
        return ID;
    }
    public String getEmail() {
        return email;
    }
    public String getDisplayName() {
        return displayName;
    }
    public String getCaption() {
        return caption;
    }
    public LocalDateTime getCreated() {
        return created;
    }
    public String getName() {
        return name;
    }
    public String getGender() {
        return gender;
    }
    public void setID(UUID ID) {
        this.ID = ID;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public void setCaption(String caption) {
        this.caption = caption;
    }
    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

}