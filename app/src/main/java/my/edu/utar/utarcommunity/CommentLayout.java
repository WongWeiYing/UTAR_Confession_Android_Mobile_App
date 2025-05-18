package my.edu.utar.utarcommunity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class CommentLayout extends AppCompatActivity {
    private int ID;
    private final int userimage;
    private final String username;
    private final String time;
    private final String comments;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getUserimage() {
        return userimage;
    }

    public String getUsername() {
        return username;
    }

    public String getTime() {
        return time;
    }

    public String getComments() {
        return comments;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_comment);

    }
    public CommentLayout(int userimage, String username, String time, String comments) {
        this.userimage = userimage;
        this.username = username;
        this.time = time;
        this.comments = comments;
    }
}

