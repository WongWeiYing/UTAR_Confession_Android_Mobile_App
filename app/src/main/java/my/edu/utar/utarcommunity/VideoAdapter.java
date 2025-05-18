package my.edu.utar.utarcommunity;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.marlonlom.utilities.timeago.TimeAgo;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private final List<VideoItem>videoList;
    private final UUID user_ID;
    private String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InN2Zm5qdndpcnRrbHd1dGxyeWVxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTI1NTcxOTUsImV4cCI6MjAyODEzMzE5NX0.kapUf05gM8cop4mtu15xwb5K6X3vsXYd341Fjh0EFxM";
    public VideoAdapter(List<VideoItem> videoList, UUID user_ID) {

        this.videoList = videoList;
        this.user_ID = user_ID;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {

        VideoItem videoItem = videoList.get(position);
        holder.user.setImageResource(videoItem.getUser());
        holder.username.setText(videoItem.getUsername());
        holder.time.setText(videoItem.getTime());
        holder.code.setText(videoItem.getCode());
        holder.content.setText(videoItem.getContent());
        holder.tag.setText(videoItem.getTag());
        holder.like_num.setText(videoItem.getLike_num());
        holder.chat_num.setText(videoItem.getChat_num());

        try {

            ReadLiked readLiked = new ReadLiked(videoItem);
            readLiked.start();
            readLiked.join();

            if(videoItem.getIsLiked()) {

                holder.like_Btn.setColorFilter(Color.BLUE);
                holder.like_num.setTextColor(Color.GRAY);

            } else {

                holder.like_Btn.setColorFilter(Color.GRAY);
                holder.like_num.setTextColor(Color.GRAY);

            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        holder.like_Btn.setOnClickListener(e -> {

            videoItem.setIsLiked(!videoItem.getIsLiked());
            LikePost likePost = new LikePost(videoItem);

            try {

                likePost.start();
                likePost.join();

            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }

            holder.like_num.setText(videoItem.getLike_num());

            if(videoItem.getIsLiked()) {

                holder.like_Btn.setColorFilter(Color.BLUE);
                holder.like_num.setTextColor(Color.BLUE);

            } else {

                holder.like_Btn.setColorFilter(Color.GRAY);
                holder.like_num.setTextColor(Color.GRAY);

            }

        });

        holder.chat_Btn.setOnClickListener(e -> {
            Intent intent = new Intent(holder.itemView.getContext(), CommentArea.class);
            intent.putExtra("user_ID", user_ID.toString());
            intent.putExtra("post_ID", videoItem.getID());
            intent.putExtra("user_DisplayName", videoItem.getUsername());
            holder.itemView.getContext().startActivity(intent);
        });

        holder.share_Btn.setOnClickListener(v -> {
            Intent intent = new Intent (Intent.ACTION_SEND);
            intent.setType("text/plain");
            String body = videoItem.getContent();
            String sub = videoItem.getCode();
            intent.putExtra(Intent.EXTRA_SUBJECT, sub);
            intent.putExtra(Intent.EXTRA_TEXT, body);
            holder.itemView.getContext().startActivity((Intent.createChooser(intent, "Share using")));
        });

    }
    private class ReadLiked extends Thread {
        private VideoItem videoItem;
        public ReadLiked(VideoItem videoItem) {

            this.videoItem = videoItem;

        }

        @Override
        public void run() {

            try {

                URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/LIKED_POSTS?user_ID=eq." + user_ID + "&post_ID=eq." + videoItem.getID() + "&select=*");
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
                if (ja.length() != 0)
                    videoItem.setIsLiked(true);

            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }

        }

    }
    private class LikePost extends Thread {
        private VideoItem videoItem;
        public LikePost(VideoItem videoItem) {

            this.videoItem = videoItem;

        }

        @Override
        public void run() {

            int like = Integer.parseInt(videoItem.getLike_num());

            try {

                JSONObject post = new JSONObject();

                if(videoItem.getIsLiked())
                    like++;

                else
                    like--;

                String likeString = Integer.toString(like);
                videoItem.setLike_num(likeString);

                post.put("post_Like", like);
                URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/POSTS?post_ID=eq." + videoItem.getID());
                HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                hc.setRequestMethod("PATCH");
                hc.setRequestProperty("apikey", SUPABASE_KEY);
                hc.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
                hc.setRequestProperty("Content-Type", "application/json");
                hc.setRequestProperty("Prefer", "return=minimal");
                hc.setDoOutput(true);
                OutputStream os = hc.getOutputStream();
                os.write(post.toString().getBytes());
                os.flush();

                Log.i("Supabase Response", hc.getResponseMessage());

                if(videoItem.getIsLiked()) {

                    JSONObject notify = new JSONObject();
                    notify.put("notify_UserID", user_ID);
                    notify.put("post_ID", videoItem.getID());
                    notify.put("notify_Type", "Like");

                    url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/NOTIFICATIONS");
                    hc = (HttpURLConnection) url.openConnection();
                    hc.setRequestMethod("POST");
                    hc.setRequestProperty("apikey", SUPABASE_KEY);
                    hc.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
                    hc.setRequestProperty("Content-Type", "application/json");
                    hc.setRequestProperty("Prefer", "return=minimal");
                    hc.setDoOutput(true);
                    os = hc.getOutputStream();
                    os.write(notify.toString().getBytes());
                    os.flush();

                    Log.i("Supabase Response", hc.getResponseMessage());

                    JSONObject liked = new JSONObject();
                    liked.put("user_ID", user_ID);
                    liked.put("post_ID", videoItem.getID());

                    url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/LIKED_POSTS");
                    hc = (HttpURLConnection) url.openConnection();
                    hc.setRequestMethod("POST");
                    hc.setRequestProperty("apikey", SUPABASE_KEY);
                    hc.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
                    hc.setRequestProperty("Content-Type", "application/json");
                    hc.setRequestProperty("Prefer", "return=minimal");
                    hc.setDoOutput(true);
                    os = hc.getOutputStream();
                    os.write(liked.toString().getBytes());
                    os.flush();

                    Log.i("Supabase Response", hc.getResponseMessage());

                } else {

                    url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/NOTIFICATIONS?notify_UserID=eq." + user_ID + "&notify_Type=eq.Like");
                    hc = (HttpURLConnection) url.openConnection();
                    hc.setRequestMethod("DELETE");
                    hc.setRequestProperty("apikey", SUPABASE_KEY);
                    hc.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);

                    Log.i("Supabase Response", hc.getResponseMessage());

                    url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/LIKED_POSTS?user_ID=eq." + user_ID + "&post_ID=eq." + videoItem.getID());
                    hc = (HttpURLConnection) url.openConnection();
                    hc.setRequestMethod("DELETE");
                    hc.setRequestProperty("apikey", SUPABASE_KEY);
                    hc.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);

                    Log.i("Supabase Response", hc.getResponseMessage());

                }

                os.close();
                hc.disconnect();

            } catch (IOException | JSONException ex) {

                throw new RuntimeException(ex);

            }

        }

    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder{
        CircleImageView user;
        TextView username;
        TextView time;
        TextView code;
        TextView content;
        TextView tag;
        TextView like_num;
        TextView chat_num;
        ImageButton like_Btn;
        ImageButton chat_Btn;
        ImageButton share_Btn;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            user = itemView.findViewById(R.id.user);
            username = itemView.findViewById(R.id.username);
            time = itemView.findViewById(R.id.time);
            code = itemView.findViewById(R.id.code);
            content = itemView.findViewById(R.id.content);
            tag = itemView.findViewById(R.id.tag);
            like_num = itemView.findViewById(R.id.like_num);
            chat_num = itemView.findViewById(R.id.chat_num);
            like_Btn = itemView.findViewById(R.id.like_icon);
            chat_Btn = itemView.findViewById(R.id.chat_icon);
            share_Btn = itemView.findViewById(R.id.share_icon);

        }

    }
}
