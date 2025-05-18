package my.edu.utar.utarcommunity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.flexbox.FlexboxLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {
    private List<SearchPost> posts;
    private OnTextClickListener onTextClickListener;
    public ResultAdapter(List<SearchPost> posts, OnTextClickListener onTextClickListener) {
        this.posts = posts;
        this.onTextClickListener = onTextClickListener;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SearchPost post = posts.get(position);
        // Bind the Post data to the view holder
        if (post.isPost_isAnonymous()) {
            holder.username.setText("Anonymous User");
        } else {
            holder.username.setText(post.getUser_Name());
        }
        holder.time.setText(TimeAgo.using(post.getPost_Created().getTime()));
        holder.code.setText(String.format("#UC%05d", post.getPost_ID()));
        holder.content.setText(post.getPost_Caption());
        holder.like_num.setText(String.valueOf(post.getPost_Like()));
        holder.chat_num.setText(String.valueOf(post.getPost_Comment()));

        String[] hashtags = post.getPost_Tags().split(",");
        // Loop through each hashtag and create a TextView for each one
        for (String hashtag : hashtags) {
            TextView textView = new TextView(holder.itemView.getContext());
            textView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            textView.setClickable(true);
            textView.setPadding(0, 0, 10, 0); // Adjust padding as needed
            textView.setText("#" + hashtag.trim());
            textView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.link_blue)); // Use your color resource here
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22); // Set text size
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onTextClickListener.onTextClicked(textView.getText().toString().replace("#", ""));
                }
            });
            holder.hashtags_layout.addView(textView); // Add TextView to FlexboxLayout
        }

        // If current post is liked by current user, set logo and text to blue colour
        if (post.isLiked_ByUser()) {
            holder.like_icon.setColorFilter(Color.BLUE);
            holder.like_num.setTextColor(Color.BLUE);
        }
        holder.like_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                post.setLiked_ByUser(!post.isLiked_ByUser());
                if (post.isLiked_ByUser()) {
                    post.setPost_Like(post.getPost_Like() + 1);
                    holder.like_icon.setColorFilter(Color.BLUE);
                    holder.like_num.setTextColor(Color.BLUE);
                } else {
                    post.setPost_Like(post.getPost_Like() - 1);
                    holder.like_icon.setColorFilter(Color.BLACK);
                    holder.like_num.setTextColor(Color.BLACK);
                }
                holder.like_num.setText(post.getPost_Like() + "");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Context context = holder.like_num.getContext();
                        SharedPreferences sharedPreferences = context.getSharedPreferences("app", Context.MODE_PRIVATE);
                        String userID = sharedPreferences.getString("userID", "");
                        if (post.isLiked_ByUser()) {
                            requestAPI_InsertLike(post.getPost_ID(), userID, context);
                        } else {
                            requestAPI_DeleteLike(post.getPost_ID(), userID, context);
                        }
                        requestAPI_UpdatePostLikeNum(post.getPost_ID(), post.getPost_Like(), context);
                    }
                }).start();
            }
        });

        holder.chat_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = holder.like_num.getContext();
                SharedPreferences sharedPreferences = context.getSharedPreferences("app", Context.MODE_PRIVATE);
                String userID = sharedPreferences.getString("userID", "");

                Intent intent = new Intent(holder.itemView.getContext(), CommentArea.class);
                intent.putExtra("post_ID", post.getPost_ID());
                intent.putExtra("user_ID", userID);
                intent.putExtra("user_DisplayName", post.getUser_Name());
                holder.itemView.getContext().startActivity(intent);
            }
        });

        holder.share_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String body = post.getPost_Caption();
                String sub = String.valueOf(post.getPost_ID());
                intent.putExtra(Intent.EXTRA_SUBJECT, sub);
                intent.putExtra(Intent.EXTRA_TEXT, body);
                holder.itemView.getContext().startActivity((Intent.createChooser(intent, "Share using")));
            }
        });
    }

    private void requestAPI_InsertLike(int postID, String userID, Context context) {
        try {
            JSONObject like = new JSONObject();
            like.put("user_ID", userID);
            like.put("post_ID", postID);
            URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/LIKED_POSTS");
            HttpURLConnection hc = (HttpURLConnection) url.openConnection();
            hc.setRequestMethod("POST");
            hc.setRequestProperty("apikey", context.getString(R.string.SUPABASE_KEY));
            hc.setRequestProperty("Authorization", "Bearer " + context.getString(R.string.SUPABASE_KEY));
            hc.setRequestProperty("Content-Type", "application/json");
            hc.setDoOutput(true);
            OutputStream os = hc.getOutputStream();
            os.write(like.toString().getBytes());
            os.flush();

            Log.d("API", "requestAPI_InsertLike: " + hc.getResponseMessage());
            Log.d("API", "requestAPI_InsertLike: " + hc.getResponseCode());
            Log.d("API", "requestAPI_InsertLike: " + like.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void requestAPI_DeleteLike(int postID, String userID, Context context) {
        try {
            URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/LIKED_POSTS?user_ID=eq." + userID + "&post_ID=eq." + postID);
            HttpURLConnection hc = (HttpURLConnection) url.openConnection();
            hc.setRequestMethod("DELETE");
            hc.setRequestProperty("apikey", context.getString(R.string.SUPABASE_KEY));
            hc.setRequestProperty("Authorization", "Bearer " + context.getString(R.string.SUPABASE_KEY));
            Log.d("API", "requestAPI_DeleteLike: " + hc.getResponseMessage());
            Log.d("API", "requestAPI_DeleteLike: " + hc.getResponseCode());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void requestAPI_UpdatePostLikeNum(int postID, int postLikeNum, Context context) {
        try {
            JSONObject like = new JSONObject();
            like.put("post_Like", postLikeNum);
            URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/POSTS?post_ID=eq." + postID);
            HttpURLConnection hc = (HttpURLConnection) url.openConnection();
            hc.setRequestMethod("PATCH");
            hc.setRequestProperty("apikey", context.getString(R.string.SUPABASE_KEY));
            hc.setRequestProperty("Authorization", "Bearer " + context.getString(R.string.SUPABASE_KEY));
            hc.setRequestProperty("Content-Type", "application/json");
            hc.setDoOutput(true);
            OutputStream os = hc.getOutputStream();
            os.write(like.toString().getBytes());
            os.flush();

            Log.d("API", "requestAPI_UpdateLikeNum: " + hc.getResponseMessage());
            Log.d("API", "requestAPI_UpdateLikeNum: " + hc.getResponseCode());
            Log.d("API", "requestAPI_UpdateLikeNum: " + like.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Define view holder for item_video
        ImageView user;
        TextView username;
        TextView time;
        TextView code;
        TextView content;
        ImageButton like_icon;
        TextView like_num;
        ImageButton chat_icon;
        TextView chat_num;
        ImageButton share_icon;
        FlexboxLayout hashtags_layout;

        public ViewHolder(View view) {
            super(view);
            // Initialize views in item_video
            user = view.findViewById(R.id.user);
            username = view.findViewById(R.id.username);
            time = view.findViewById(R.id.time);
            code = view.findViewById(R.id.code);
            content = view.findViewById(R.id.content);
            like_icon = view.findViewById(R.id.like_icon);
            like_num = view.findViewById(R.id.like_num);
            chat_icon = view.findViewById(R.id.chat_icon);
            chat_num = view.findViewById(R.id.chat_num);
            share_icon = view.findViewById(R.id.share_icon);
            hashtags_layout = view.findViewById(R.id.hashtags_layout);
        }
    }
}