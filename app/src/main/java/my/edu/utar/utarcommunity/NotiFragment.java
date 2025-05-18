package my.edu.utar.utarcommunity;

import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotiFragment extends Fragment {
    private ArrayList<Notify> notifys = new ArrayList<>();
    private ArrayList<Post> posts = new ArrayList<>();
    UUID user_ID;

    public NotiFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args != null)
            user_ID = UUID.fromString(args.getString("user_ID"));

        try {

            ReadNoti readNoti = new ReadNoti();
            readNoti.start();
            readNoti.join();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        View rootView = inflater.inflate(R.layout.fragment_noti, container, false);
        List<NotiItem> notiList = generateVideoItems();
        RecyclerView notiRecyclerView = rootView.findViewById(R.id.notiRecyclerView);
        notiRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        NotiAdapter notiAdapter = new NotiAdapter(notiList, user_ID);
        notiRecyclerView.setAdapter(notiAdapter);

        return rootView;
    }

    private class ReadNoti extends Thread {
        String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InN2Zm5qdndpcnRrbHd1dGxyeWVxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTI1NTcxOTUsImV4cCI6MjAyODEzMzE5NX0.kapUf05gM8cop4mtu15xwb5K6X3vsXYd341Fjh0EFxM";

        @Override
        public void run() {

            try {

                URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/POSTS?user_ID=eq." + user_ID.toString() + "&select=*");
                HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                hc.setRequestProperty("apikey", SUPABASE_KEY);
                hc.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
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

                    JSONObject returnPost = ja.getJSONObject(i);
                    Post newPost = new Post();
                    newPost.setID(returnPost.getInt("post_ID"));
                    posts.add(newPost);

                }

                Log.i("Supabase response", hc.getResponseMessage());

                is.close();
                hc.disconnect();

            } catch (IOException | JSONException e) {

                throw new RuntimeException(e);

            }

            for(int j = 0; j < posts.size(); j++) {

                Post post = posts.get(j);

                try {

                    URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/NOTIFICATIONS?post_ID=eq." + post.getID() + "&select=*");
                    HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                    hc.setRequestProperty("apikey", SUPABASE_KEY);
                    hc.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
                    InputStream is = hc.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder stringBuilder = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    String jsonString = stringBuilder.toString();
                    JSONArray ja = new JSONArray(jsonString);

                    for (int i = 0; i < ja.length(); i++) {

                        JSONObject returnPost = ja.getJSONObject(i);
                        Notify notify = new Notify();
                        User user = new User(UUID.fromString(returnPost.getString("notify_UserID")));
                        notify.setUser(user);
                        notify.setPost_ID(post.getID());
                        notify.setNotify_Type(returnPost.getString("notify_Type"));
                        notify.setAnonymous(returnPost.getBoolean("notify_isAnonymous"));
                        String createdString = returnPost.getString("notify_Created");

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            notify.setNotify_Created(LocalDateTime.parse(createdString));

                        notifys.add(notify);

                    }

                    Log.i("Supabase response", hc.getResponseMessage());

                    is.close();
                    hc.disconnect();

                } catch (JSONException | IOException e) {
                    throw new RuntimeException(e);
                }

            }

            for(int i = 0; i < notifys.size(); i++) {

                Notify notify = notifys.get(i);

                try {

                    URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/USERS?id=eq." + notify.getUser().getID() + "&select=*");
                    HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                    hc.setRequestProperty("apikey", SUPABASE_KEY);
                    hc.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
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

                    if(notify.isAnonymous())
                        notify.getUser().setDisplayName("Anonymous User");

                    else
                        notify.getUser().setDisplayName(returnUser.getString("user_DisplayName"));

                    // TODO: Fetch user's profile picture

                    Log.i("Supabase response", hc.getResponseMessage());

                    is.close();
                    hc.disconnect();

                } catch (IOException | JSONException e) {

                    throw new RuntimeException(e);

                }

            }

        }

    }
    private List<NotiItem> generateVideoItems(){

        List<NotiItem> notiItems = new ArrayList<>();

        for(int i = 0; i < notifys.size(); i++) {

            Notify notify = notifys.get(i);

            String post_Code = "#UC" + notify.getPost_ID();

            // TODO: Calculate time difference
            String createdString = notify.getNotify_Created().toString();
            LocalDateTime dateTime;
            String formattedDateTime = "";

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                dateTime = LocalDateTime.parse(createdString);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                formattedDateTime = dateTime.format(formatter);

            }

            NotiItem notiItem = new NotiItem(
                    R.drawable.baseline_account_circle_24,
                    notify.getUser().getDisplayName(),
                    formattedDateTime,
                    post_Code,
                    notify.getNotify_Type(),
                    R.drawable.baseline_photo_library_24);

            notiItem.setID(notify.getPost_ID());
            notiItems.add(notiItem);

        }

        return notiItems;
    }

}