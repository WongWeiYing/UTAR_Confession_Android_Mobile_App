package my.edu.utar.utarcommunity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import de.hdodenhof.circleimageview.CircleImageView;

public class CreateNewPost extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri selectedImageUri;
    User user;
    Post newPost;
    StringBuilder tags = new StringBuilder();
    ImageButton bt_BackHome;
    Button bt_Submit;
    ImageButton bt_ClearTags;
    TextView tv_UserDisplayName;
    CircleImageView iv_UserProfile;
    EditText et_Caption;
    EditText et_Tags;
    SwitchCompat sw_isAnonymous;
    CheckBox cb_Disclaimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_post);

        bt_BackHome = findViewById(R.id.bt_BackHome);
        bt_Submit = findViewById(R.id.bt_Submit);
        bt_ClearTags = findViewById(R.id.bt_ClearTags);
        tv_UserDisplayName = findViewById(R.id.tv_UserDisplayName);
        iv_UserProfile = findViewById(R.id.iv_UserProfile);
        et_Caption = findViewById(R.id.et_Caption);
        et_Tags = findViewById(R.id.et_Tags);
        sw_isAnonymous = findViewById(R.id.isAnonymous);
        cb_Disclaimer = findViewById(R.id.cb_Disclaimer);

        Intent home = getIntent();

        String ID = home.getStringExtra("user_ID");
        UUID user_UUID = UUID.fromString(ID);
        user = new User(user_UUID);
        new ReadUser().start();
        newPost = new Post();
        boolean isAnonymous = home.getBooleanExtra("isAnonymous", false);
        newPost.setIsAnonymous(isAnonymous);
        sw_isAnonymous.setChecked(isAnonymous);

        if (isAnonymous)
            tv_UserDisplayName.setText("Anonymous User");

        else
            tv_UserDisplayName.setText(user.getDisplayName());

        sw_isAnonymous.setOnClickListener(e -> {

            sw_isAnonymous.setChecked(!newPost.getIsAnonymous());
            newPost.setIsAnonymous(sw_isAnonymous.isChecked());

            if (newPost.getIsAnonymous())
                tv_UserDisplayName.setText("Anonymous User");

            else
                tv_UserDisplayName.setText(user.getDisplayName());

        });

        bt_BackHome.setOnClickListener(e -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(this,  R.style.CustomDialogTheme);
            builder.setTitle("Exit to Home Page")
                    .setMessage("Are you sure you want to cancel editing post?")
                    .setPositiveButton("Quit", (dialog, id) -> {
                        Intent intent = new Intent(CreateNewPost.this, MainActivity.class);
                        intent.putExtra("user_ID", user.getID().toString());
                        startActivity(intent);
                    })
                    .setNegativeButton("Continue Editing", (dialog, id) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();

        });

        bt_ClearTags.setOnClickListener(e -> {

            et_Tags.setText("");
            tags.setLength(0);

        });

        bt_Submit.setOnClickListener(e -> {

            newPost.setCaption(et_Caption.getText().toString());
            newPost.setTags(et_Tags.getText().toString());

            if (newPost.getCaption().equals(""))
                Toast.makeText(this, "Some fields are not filled", Toast.LENGTH_SHORT).show();

            else if (!cb_Disclaimer.isChecked())
                Toast.makeText(this, "Please tick the checkbox", Toast.LENGTH_SHORT).show();

            else{

                new NewPost().start();
                Toast.makeText(this, "Successfully created post", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CreateNewPost.this, MainActivity.class);
                intent.putExtra("user_ID", user.getID().toString());
                startActivity(intent);

            }

        });

    }

    private class NewPost extends Thread {

        @Override
        public void run() {

            try {

                JSONObject post = new JSONObject();
                post.put("user_ID", user.getID());
                post.put("post_Caption", newPost.getCaption());
                post.put("post_Tags", newPost.getTags());
                post.put("post_isAnonymous", newPost.getIsAnonymous());

                URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/POSTS");
                HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                hc.setRequestMethod("POST");
                hc.setRequestProperty("apikey", getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Authorization", "Bearer " + getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Content-Type", "application/json");
                hc.setRequestProperty("Prefer", "return=minimal");
                hc.setDoOutput(true);
                OutputStream os = hc.getOutputStream();
                os.write(post.toString().getBytes());
                os.flush();

                Log.i("Supabase Response", hc.getResponseMessage());

            } catch (IOException | JSONException ex) {

                throw new RuntimeException(ex);

            }

        }

    }
    private class ReadUser extends Thread {

        @Override
        public void run() {

            try {

                URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/USERS?id=eq." + user.getID() + "&select=*");
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

                user.setEmail(returnUser.getString("user_Email"));
                user.setDisplayName(returnUser.getString("user_DisplayName"));
                user.setCaption(returnUser.getString("user_Caption"));
                user.setName(returnUser.getString("user_Name"));
                user.setGender(returnUser.getString("user_Gender"));


                runOnUiThread(() -> {

                    if (sw_isAnonymous.isChecked()) {

                        tv_UserDisplayName.setText("Anonymous User");

                    } else {

                        tv_UserDisplayName.setText(user.getDisplayName());

                    }


                });

                is.close();
                hc.disconnect();

            } catch (IOException | JSONException e) {

                throw new RuntimeException(e);

            }

        }

    }

}