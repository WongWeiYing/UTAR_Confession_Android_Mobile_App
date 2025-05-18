package my.edu.utar.utarcommunity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class NotiItem extends AppCompatActivity {
    private int ID;
    private final int userimage;
    private final String username;
    private final String time;
    private final String post;
    private final String noti_Action;


    public void setID(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }
    public String getNoti_Action() {
        return noti_Action;
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

    public String getPost() {
        return post;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_noti);

    }

    public NotiItem(int userimage, String username, String time, String post, String notiAction, int image) {
        this.userimage = userimage;
        this.username = username;
        this.time = time;
        this.post = post;
        this.noti_Action = notiAction;
    }
}

