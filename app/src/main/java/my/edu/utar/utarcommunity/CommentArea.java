package my.edu.utar.utarcommunity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommentArea extends AppCompatActivity {
    ArrayList<Comment> comments = new ArrayList<>();
    Comment userComment = new Comment();
    Post newPost = new Post();
    User user;
    int post_ID;
    String user_DisplayName;
    TextView tv_UserDisplayName;
    TextView time;
    TextView code;
    TextView content;
    TextView tag;
    EditText et_Caption;
    ImageButton bt_BackHome;
    ImageButton bt_send;
    ImageView iv_UserProfile;
    SwitchCompat com_IsAnonymous;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_area);

        Intent post = getIntent();
        String ID = post.getStringExtra("user_ID");
        post_ID = post.getIntExtra("post_ID", 0);
        user_DisplayName = post.getStringExtra("user_DisplayName");
        newPost.setID(post_ID);
        UUID user_UUID = UUID.fromString(post.getStringExtra("user_ID"));

        user = new User(user_UUID);
        userComment.setUser(user);
        userComment.setPost_ID(post_ID);

        bt_BackHome = findViewById(R.id.bt_BackHome);
        bt_send = findViewById(R.id.bt_send);
        tv_UserDisplayName = findViewById(R.id.tv_UserDisplayName);
        et_Caption = findViewById(R.id.et_Caption);
        time = findViewById(R.id.time);
        code = findViewById(R.id.code);
        content = findViewById(R.id.content);
        tag = findViewById(R.id.tag);
        iv_UserProfile = findViewById(R.id.iv_UserProfile);
        com_IsAnonymous = findViewById(R.id.commentIsAnonymous);

        ReadPost readPost = new ReadPost();
        ReadUser readUser = new ReadUser();
        ReadComment readComment = new ReadComment();

        try {

            readPost.start();
            readPost.join();
            readComment.start();
            readComment.join();
            readUser.start();
            readUser.join();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        com_IsAnonymous.setOnClickListener(e -> {

            com_IsAnonymous.setChecked(!userComment.getAnonymous());
            userComment.setAnonymous(com_IsAnonymous.isChecked());

        });

        bt_BackHome.setOnClickListener(e -> {
             super.onBackPressed();
        });

        bt_send.setOnClickListener(e -> {

            userComment.setContent(et_Caption.getText().toString());

            if (userComment.getContent().equals(""))
                Toast.makeText(this, "Some fields are not filled", Toast.LENGTH_SHORT).show();

            else{

                WriteComment writeComment = new WriteComment();

                try {

                     writeComment.start();
                     writeComment.join();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }

                Toast.makeText(this, "Successfully created comment", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CommentArea.this, CommentArea.class);
                intent.putExtra("post_ID", post_ID);
                intent.putExtra("user_ID", user.getID().toString());
                intent.putExtra("user_DisplayName", user_DisplayName);
                startActivity(intent);

            }

        });

        List<CommentLayout> commentList = generateCommentLayouts();
        RecyclerView commentRecyclerView = findViewById(R.id.commentRecyclerView);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        CommentAdapter commentAdapter = new CommentAdapter(this, commentList);
        commentRecyclerView.setAdapter(commentAdapter);

    }
    private List<CommentLayout> generateCommentLayouts(){

        List<CommentLayout> videoItems = new ArrayList<>();

        for(int i = 0; i < comments.size(); i++) {

            Comment comment = comments.get(i);
            String user_DisplayName;

            if (!comment.getAnonymous())
                user_DisplayName = comment.getUser().getDisplayName();
            else
                user_DisplayName = "Anonymous User";

            // TODO: Calculate time difference
            String createdString = comment.getCreated().toString();
            LocalDateTime dateTime;
            String formattedDateTime = "";

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                dateTime = LocalDateTime.parse(createdString);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                formattedDateTime = dateTime.format(formatter);

            }
            // TODO: Replace profile and post image from newPost

            CommentLayout commentLayout = new CommentLayout(
                    R.drawable.baseline_account_circle_24,
                    user_DisplayName,
                    formattedDateTime,
                    comment.getContent());

            commentLayout.setID(comment.getID());
            videoItems.add(commentLayout);

        }

        return videoItems;

    }
    private class WriteComment extends Thread {

        @Override
        public void run() {

            try {

                JSONObject comment = new JSONObject();
                comment.put("user_ID", userComment.getUser().getID());
                comment.put("com_Content", userComment.getContent());
                comment.put("com_isAnonymous", userComment.getAnonymous());
                comment.put("post_ID", userComment.getPost_ID());

                URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/COMMENTS");
                HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                hc.setRequestMethod("POST");
                hc.setRequestProperty("apikey", getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Authorization", "Bearer " + getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Content-Type", "application/json");
                hc.setRequestProperty("Prefer", "return=minimal");
                hc.setDoOutput(true);
                OutputStream os = hc.getOutputStream();
                os.write(comment.toString().getBytes());
                os.flush();

                Log.i("Supabase Response !!!!!!!!!!!!", hc.getResponseMessage());

                JSONObject notify = new JSONObject();
                notify.put("notify_UserID", user.getID());
                notify.put("post_ID", post_ID);
                notify.put("notify_Type", "Comment");
                notify.put("notify_isAnonymous", userComment.getAnonymous());

                url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/NOTIFICATIONS");
                hc = (HttpURLConnection) url.openConnection();
                hc.setRequestMethod("POST");
                hc.setRequestProperty("apikey", getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Authorization", "Bearer " + getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Content-Type", "application/json");
                hc.setRequestProperty("Prefer", "return=minimal");
                hc.setDoOutput(true);
                os = hc.getOutputStream();
                os.write(notify.toString().getBytes());
                os.flush();

                Log.i("Supabase Response !!!!!!!!!!!!", hc.getResponseMessage());

                int comNum = newPost.getComment();
                JSONObject newCom = new JSONObject();
                newCom.put("post_Comment", ++comNum);
                url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/POSTS?post_ID=eq." + post_ID);
                hc = (HttpURLConnection) url.openConnection();
                hc.setRequestMethod("PATCH");
                hc.setRequestProperty("apikey", getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Authorization", "Bearer " + getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Content-Type", "application/json");
                hc.setRequestProperty("Prefer", "return=minimal");
                hc.setDoOutput(true);
                os = hc.getOutputStream();
                os.write(newCom.toString().getBytes());
                os.flush();

                Log.i("Supabase Response !!!!!!!!!!!!", hc.getResponseMessage());

                os.close();
                hc.disconnect();

            } catch (IOException | JSONException ex) {

                throw new RuntimeException(ex);

            }

        }

    }
    private class ReadComment extends Thread {

        @Override
        public void run() {

            try {

                URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/COMMENTS?post_ID=eq." + post_ID + "&select=*");
                HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                hc.setRequestProperty("apikey", getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Authorization", "Bearer " + getString(R.string.SUPABASE_KEY));
                InputStream is = hc.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder stringBuilder = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String jsonString = stringBuilder.toString();
                JSONArray ja = new JSONArray(jsonString);

                for(int i = 0; i < ja.length(); i++) {

                    // TODO: Fetch user's profile picture, post image
                    JSONObject returnComment = ja.getJSONObject(i);
                    Comment comment = new Comment();
                    User user = new User(UUID.fromString(returnComment.getString("user_ID")));
                    comment.setUser(user);
                    comment.setID(returnComment.getInt("com_ID"));
                    comment.setContent(returnComment.getString("com_Content"));
                    comment.setAnonymous(returnComment.getBoolean("com_isAnonymous"));
                    comment.setPost_ID(returnComment.getInt("post_ID"));
                    String createdString = returnComment.getString("com_Created");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        comment.setCreated(LocalDateTime.parse(createdString));

                    comments.add(comment);

                }

                is.close();
                hc.disconnect();

            } catch (IOException | JSONException e) {

                throw new RuntimeException(e);

            }

        }

    }
    private class ReadPost extends Thread {
        @Override
        public void run() {

            try {

                URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/POSTS?post_ID=eq." + post_ID + "&select=*");
                HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                hc.setRequestProperty("apikey", getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Authorization", "Bearer " + getString(R.string.SUPABASE_KEY));
                InputStream is = hc.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder stringBuilder = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String jsonString = stringBuilder.toString();
                JSONArray ja = new JSONArray(jsonString);

                // TODO: Fetch user's profile picture, post image
                JSONObject returnPost = ja.getJSONObject(0);

                newPost.setLike(returnPost.getInt("post_Like"));
                newPost.setComment(returnPost.getInt("post_Comment"));

                iv_UserProfile.setImageResource(R.drawable.baseline_account_circle_24);

                boolean isAnonymous = returnPost.getBoolean("post_isAnonymous");

                if (isAnonymous)
                    tv_UserDisplayName.setText("Anonymous User");

                else
                    tv_UserDisplayName.setText(user_DisplayName);

                content.setText(returnPost.getString("post_Caption"));

                String[] tagArray = returnPost.getString("post_Tags").split(", ");
                StringBuilder result = new StringBuilder();
                for (String tag : tagArray) {
                    result.append("#").append(tag).append(" ");
                }

                String post_Tags = result.toString().trim();
                tag.setText(post_Tags);
                code.setText("#UC" + post_ID);

                String createdString = returnPost.getString("post_Created");
                LocalDateTime dateTime;
                String formattedDateTime = "";

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                    dateTime = LocalDateTime.parse(createdString);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    formattedDateTime = dateTime.format(formatter);

                }

                time.setText(formattedDateTime);

                is.close();
                hc.disconnect();

            } catch (IOException | JSONException e) {

                throw new RuntimeException(e);

            }

        }

    }
    private class ReadUser extends Thread {

        @Override
        public void run() {

            for(int i = 0; i < comments.size(); i++) {

                Comment comment = comments.get(i);

                try {

                    URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/USERS?id=eq." + comment.getUser().getID() + "&select=*");
                    HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                    hc.setRequestProperty("apikey", getString(R.string.SUPABASE_KEY));
                    hc.setRequestProperty("Authorization", "Bearer " + getString(R.string.SUPABASE_KEY));
                    InputStream is = hc.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder stringBuilder = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    String jsonString = stringBuilder.toString();
                    JSONArray ja = new JSONArray(jsonString);
                    JSONObject returnUser = ja.getJSONObject(0);

                    comment.getUser().setEmail(returnUser.getString("user_Email"));
                    comment.getUser().setDisplayName(returnUser.getString("user_DisplayName"));
                    comment.getUser().setCaption(returnUser.getString("user_Caption"));
                    comment.getUser().setName(returnUser.getString("user_Name"));
                    comment.getUser().setGender(returnUser.getString("user_Gender"));

                    // TODO: Fetch user's profile picture

//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                    String createdString = returnUser.getString("user_Created");
//                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
//                    user.setCreated(LocalDateTime.parse(createdString, formatter));
//                }

                    is.close();
                    hc.disconnect();

                } catch (IOException | JSONException e) {

                    throw new RuntimeException(e);

                }

            }

        }

    }

}