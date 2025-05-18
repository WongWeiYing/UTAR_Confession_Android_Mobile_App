package my.edu.utar.utarcommunity;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class VideoItem extends AppCompatActivity {
    ImageButton like;
    ImageButton chat;
    ImageButton share;
    private int ID;
    private final int user;
    private final String username;
    private final String time;
    private final String code;
    private final String content;
    private final String tag;
    private String like_num;
    private String chat_num;
    private boolean isLiked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_video);

        like = findViewById(R.id.like_icon);
        chat = findViewById(R.id.chat_icon);
        share = findViewById(R.id.share_icon);

    }
    public boolean getIsLiked() {
        return isLiked;
    }
    public void setIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }
    public int getID() {
        return ID;
    }
    public void setID(int ID) {
        this.ID = ID;
    }
    public int getUser() {
        return user;
    }
    public String getUsername() {
        return username;
    }
    public String getTime() {
        return time;
    }
    public String getCode() {
        return code;
    }
    public String getContent() {
        return content;
    }
    public ImageButton getLike() {
        return like;
    }
    public ImageButton getChat() {
        return chat;
    }
    public ImageButton getShare() {
        return share;
    }
    public String getTag() {
        return tag;
    }
    public String getLike_num() {
        return like_num;
    }
    public void setLike_num(String like_num) {

        this.like_num = like_num;
    }
    public String getChat_num() {
        return chat_num;
    }
    public VideoItem(int user, String username, String time, String code, String content, String tag, String like_num, String chat_num) {
        this.user = user;
        this.username = username;
        this.time = time;
        this.code = code;
        this.content = content;
        this.tag = tag;
        this.like_num = like_num;
        this.chat_num = chat_num;
    }
}

