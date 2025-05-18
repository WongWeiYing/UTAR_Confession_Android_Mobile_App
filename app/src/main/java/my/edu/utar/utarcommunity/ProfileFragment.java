package my.edu.utar.utarcommunity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class ProfileFragment extends Fragment {

    private String userID, gender;
    private TextView name, username, bio, myPost, likedPost;
    private ImageView editProfile;

    public ProfileFragment(String userID) {
        this.userID = userID;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);

        name=view.findViewById(R.id.nameTv);
        username=view.findViewById(R.id.usernameTv);
        bio=view.findViewById(R.id.bioTv);
        editProfile=(ImageView)view.findViewById(R.id.editProfile);
        myPost=(TextView)view.findViewById(R.id.myPost);
        likedPost=(TextView)view.findViewById(R.id.likedPost);

        myPost.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        myPost.setTypeface(null, Typeface.BOLD);

        displayMyPostFragment();

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Clicked", "EditProfile onClick");
                Intent intent = new Intent(getContext(), EditProfile.class);
                intent.putExtra("userID", userID);
                intent.putExtra("name",name.getText().toString().trim());
                intent.putExtra("username",username.getText().toString().trim());
                intent.putExtra("bio",bio.getText().toString().trim());
                intent.putExtra("gender",gender);
                startActivityForResult(intent,1);

            }
        });

        myPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Clicked", "mypost onClick");
                myPost.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                myPost.setTypeface(null, Typeface.BOLD);
                likedPost.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                likedPost.setTypeface(null, Typeface.NORMAL);

                displayMyPostFragment();

            }
        });

        likedPost.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d("Clicked", "likedpost onClick");
                likedPost.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                likedPost.setTypeface(null, Typeface.BOLD);
                myPost.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                myPost.setTypeface(null, Typeface.NORMAL);

                displayLikedPostFragment();
            }
        });

        new Read().start();

        return view;
    }
    private void displayMyPostFragment() {

        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment, new MyPost(userID));
        fragmentTransaction.addToBackStack("MyPosts");
        fragmentTransaction.commit();

    }
    private void displayLikedPostFragment() {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment, new LikedPost(userID));
        fragmentTransaction.addToBackStack("LikedPost");
        fragmentTransaction.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            name.setText(data.getStringExtra("changeName"));
            username.setText(data.getStringExtra("changeUsername"));
            bio.setText(data.getStringExtra("changeBio"));
        }
    }
    private class Read extends Thread {

        String storedName ="";
        String storedUsername="";
        String storedGender="";
        String storedBio="";

        @Override
        public void run() {

            try {
                URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/USERS?id=eq."+userID+"&select=*");
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
                JSONArray returnJA = new JSONArray(jsonString);

                for (int i = 0; i < returnJA.length(); i++) {

                    JSONObject jo = (JSONObject) returnJA.get(i);

                    storedName = jo.optString("user_Name");
                    storedUsername = jo.optString("user_DisplayName");
                    storedGender = jo.optString("user_Gender");
                    storedBio = jo.optString("user_Caption");
                    gender = storedGender;

                    requireActivity().runOnUiThread(() -> handleLoginResult(storedName, storedUsername, storedBio));

                }
                is.close();
                hc.disconnect();

            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void handleLoginResult(String storedName, String storedUsername, String storedBio) {
        name.setText(storedName);
        username.setText(storedUsername);
        bio.setText(storedBio);
    }
}
