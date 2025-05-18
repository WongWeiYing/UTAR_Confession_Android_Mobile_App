package my.edu.utar.utarcommunity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout drawerLayout;
    FloatingActionButton fab;
    BottomNavigationView bottomNavigationView;
    NavigationView navigationView;
    SwitchCompat switchMode;
    boolean nightMode;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    UUID user_ID;
    TextView userName;
    TextView userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getIntent().hasExtra("user_ID")) {

            Intent login = getIntent();
            String ID = login.getStringExtra("user_ID");
            user_ID = UUID.fromString(ID);

        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fab = findViewById(R.id.fab);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new Fragment()).commit();
            navigationView.setCheckedItem(R.id.home);
        }

        replaceFragment(new HomeFragment());

        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.search) {
                replaceFragment(new SearchFragment());
                return true;
            } else if (itemId == R.id.notifications) {
                replaceFragment(new NotiFragment());
                return true;
            } else if (itemId == R.id.profile) {
                replaceFragment(new ProfileFragment(user_ID.toString()));
                return true;
            }
            return false;
        });

        fab.setOnClickListener(view -> showBottomDialog());

        View headerView = navigationView.getHeaderView(0);
        switchMode = headerView.findViewById(R.id.switchMode);
        userName = headerView.findViewById(R.id.userName);
        userEmail = headerView.findViewById(R.id.userEmail);

        new ReadUser().start();

        sharedPreferences = getSharedPreferences("app", Context.MODE_PRIVATE);
        nightMode = sharedPreferences.getBoolean("nightMode", false);
        editor = sharedPreferences.edit();

        if (!nightMode){
            switchMode.setChecked(false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        switchMode.setOnClickListener(view -> {

            if (nightMode){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putBoolean("nightMode", false);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putBoolean("nightMode", true);
            }
            editor.apply();
        });

    }

    private class ReadUser extends Thread {

        @Override
        public void run() {

            try {

                URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/USERS?id=eq." + user_ID + "&select=*");
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

                String email = returnUser.getString("user_Email");
                String name = returnUser.getString("user_DisplayName");

                // TODO: Fetch user's profile picture

                runOnUiThread(() -> {

                   userName.setText(name);
                   userEmail.setText(email);

                });

                is.close();
                hc.disconnect();

            } catch (IOException | JSONException e) {

                throw new RuntimeException(e);

            }

        }

    }

    //Outside onCreate
    private void replaceFragment(Fragment fragment) {

        Bundle bundle = new Bundle();
        bundle.putString("user_ID", user_ID.toString());
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    private void showBottomDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheetlayout);

        LinearLayout post_normal = dialog.findViewById(R.id.create_post_normally);
        LinearLayout post_ano = dialog.findViewById(R.id.create_post_anonymously);
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);

        post_normal.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(MainActivity.this, CreateNewPost.class);
            intent.putExtra("isAnonymous", false);
            intent.putExtra("user_ID", user_ID.toString());
            startActivity(intent);
        });

        post_ano.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(MainActivity.this, CreateNewPost.class);
            intent.putExtra("isAnonymous", true);
            intent.putExtra("user_ID", user_ID.toString());
            startActivity(intent);
        });

        cancelButton.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_logout) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
            builder.setTitle("Log Out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Quit", (dialog, i) -> {
                        Intent intent = new Intent(MainActivity.this, Login.class);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", (dialog, i) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();

        } else if (id == R.id.nav_help) {
            replaceFragment(new HelpFragment());
        } else if (id == R.id.nav_review) {
            replaceFragment(new ReviewFragment());
        } else if (id == R.id.nav_about) {
            replaceFragment(new AboutFragment());
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


}
