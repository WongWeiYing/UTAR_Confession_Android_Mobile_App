package my.edu.utar.utarcommunity;

import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.marlonlom.utilities.timeago.TimeAgo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class LikedPost extends Fragment {

    ArrayList<Post> newPosts;
    String userID;
    public LikedPost(String userID) {
        this.userID = userID;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        UUID uuid = UUID.fromString(userID);

        newPosts = new ArrayList<>();

        ReadPost readPostThread = new ReadPost();
        ReadUser readUserThread = new ReadUser();

        try {
            readPostThread.start();
            readPostThread.join();
            readUserThread.start();
            readUserThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        View rootView = inflater.inflate(R.layout.fragment_liked_post, container, false);
        List<VideoItem> videoList = generateVideoItems();
        RecyclerView homeRecyclerView = rootView.findViewById(R.id.myLikedPost_recycler_view);
        homeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        VideoAdapter videoAdapter = new VideoAdapter(videoList, uuid);
        homeRecyclerView.setAdapter(videoAdapter);

        return rootView;

    }
    private List<VideoItem> generateVideoItems(){

        List<VideoItem> videoItems = new ArrayList<>();
        for(int i = 0; i < newPosts.size(); i++) {

            Post newPost = newPosts.get(i);
            String user_DisplayName;

            if (!newPost.getIsAnonymous())
                user_DisplayName = newPost.getUser().getDisplayName();
            else
                user_DisplayName = "Anonymous User";

            String post_Code = "#UC" + newPost.getID();
            String[] tagArray = newPost.getTags().split(", ");
            StringBuilder result = new StringBuilder();

            for (String tag : tagArray) {
                result.append("#").append(tag).append(" ");
            }

            String post_Tags = result.toString().trim();

            String formattedDateTime = TimeAgo.using(newPost.getCreated().getTime());


            // TODO: Replace profile and post image from newPost

            VideoItem videoItem = new VideoItem(
                    R.drawable.baseline_account_circle_24,
                    user_DisplayName,
                    formattedDateTime,
                    post_Code,
                    newPost.getCaption(),
                    post_Tags,
                    Integer.toString(newPost.getLike()),
                    Integer.toString(newPost.getComment())

            );

            videoItem.setID(newPost.getID());
            videoItems.add(videoItem);

        }

        return videoItems;

    }
    private class ReadPost extends Thread {

        @Override
        public void run() {

            try {

                URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/LIKED_POSTS?user_ID=eq."+userID+"&select=*");
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
                    JSONObject returnPost = ja.getJSONObject(i);
                    int postID = returnPost.getInt("post_ID");

                    url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/POSTS?post_ID=eq."+postID+"&select=*");
                    hc = (HttpURLConnection) url.openConnection();
                    hc.setRequestProperty("apikey", getString(R.string.SUPABASE_KEY));
                    hc.setRequestProperty("Authorization", "Bearer " + getString(R.string.SUPABASE_KEY));
                    is = hc.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(is));
                    stringBuilder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    jsonString = stringBuilder.toString();
                    JSONArray jsonArray = new JSONArray(jsonString);

                    returnPost = jsonArray.getJSONObject(0);
                    // postID = returnPost.getInt("post_ID");

                    Post newPost = new Post();
                    User user = new User(UUID.fromString(returnPost.getString("user_ID")));
                    newPost.setUser(user);
                    newPost.setID(returnPost.getInt("post_ID"));
                    newPost.setTags(returnPost.optString("post_Tags", ""));
                    newPost.setLike(returnPost.getInt("post_Like"));
                    newPost.setComment(returnPost.getInt("post_Comment"));
                    newPost.setCaption(returnPost.getString("post_Caption"));
                    newPost.setIsAnonymous(returnPost.getBoolean("post_isAnonymous"));
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
                    newPost.setCreated(sdf.parse(returnPost.getString("post_Created")));
                    //String createdString = returnPost.getString("post_Created");

                    /*
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        newPost.setCreated(LocalDateTime.parse(createdString));*/

                    newPosts.add(newPost);

                }

                is.close();
                hc.disconnect();

            } catch (IOException | JSONException | ParseException e) {

                e.printStackTrace();
            }
        }
    }

    private class ReadUser extends Thread {

        @Override
        public void run() {

            for (int i = 0; i < newPosts.size(); i++) {

                Post newPost = newPosts.get(i);

                try {

                    URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/USERS?id=eq." + newPost.getUser().getID() + "&select=*");
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

                    newPost.getUser().setEmail(returnUser.getString("user_Email"));
                    newPost.getUser().setDisplayName(returnUser.getString("user_DisplayName"));
                    newPost.getUser().setCaption(returnUser.getString("user_Caption"));
                    newPost.getUser().setName(returnUser.getString("user_Name"));
                    newPost.getUser().setGender(returnUser.getString("user_Gender"));

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