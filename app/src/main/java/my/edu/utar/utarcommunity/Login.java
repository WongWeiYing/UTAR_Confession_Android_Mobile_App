package my.edu.utar.utarcommunity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class Login extends AppCompatActivity implements View.OnClickListener {
    EditText email, password;
    TextView forgotPass, signup;
    Button login;
    boolean passwordVisible;
    boolean emailExist = false, passwordValid = false;
    String userID = "";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.resetPass);
        password = findViewById(R.id.password);
        forgotPass = findViewById(R.id.forgotPass);
        signup = findViewById(R.id.signup);
        login = findViewById(R.id.login);

        forgotPass.setOnClickListener(this);
        signup.setOnClickListener(this);
        login.setOnClickListener(this);

        Log.d("Login", "Initial state of passwordVisible: " + passwordVisible);

        setupTextChangeListeners();

        PasswordVisibilityTouchListener passwordTouchListener = new PasswordVisibilityTouchListener(password);
        password.setOnTouchListener(passwordTouchListener);
    }
    private void setupTextChangeListeners() {

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                email.setError(null); // Clear error message when the user starts typing
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                password.setError(null); // Clear error message when the user starts typing
                // Clear error message when the user starts typing
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.login) {

            if (TextUtils.isEmpty(email.getText().toString().trim())) {
                email.setError("Required");
                return;
            } else {
                if (!email.getText().toString().trim().endsWith("@1utar.my")) {
                    email.setError("@1utar.my");
                    return;
                }
            }
            if (TextUtils.isEmpty(password.getText().toString().trim())) {
                password.setError("Required");
                return;
            }

            new Read().start();

        } else if (v.getId() == R.id.signup) {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(Login.this, ResetPassword.class);
            startActivity(intent);
        }
    }
    @SuppressLint("ClickableViewAccessibility")

    private class Read extends Thread {
        String storedPassword = "";
        String storedEmail = "";
        String storeduserID="";

        public void run() {

            try {
                URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/USERS?user_Email=eq."+email.getText().toString().trim()+"&select=*");
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
                    storeduserID = jo.optString("id");
                    storedEmail = jo.optString("user_Email");
                    storedPassword = jo.optString("user_Password");


                    if (storedEmail.equals(email.getText().toString().trim())) {
                        emailExist = true;
                        if (storedPassword.equals(password.getText().toString().trim())) {
                            passwordValid = true;
                            userID = storeduserID;
                        }
                        else{
                            passwordValid = false;
                        }
                    }
                }
                is.close();
                hc.disconnect();

                runOnUiThread(() -> handleLoginResult(emailExist, passwordValid));

            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void handleLoginResult(boolean emailExist, boolean passwordValid) {
        if (emailExist) {
            if (passwordValid) {

                Log.d("Login", "Login successful. User ID: " + userID);

                // save login user id in shared preferences
                SharedPreferences sharedPreferences = getSharedPreferences("app", MODE_PRIVATE);
                sharedPreferences.edit().putString("userID", userID).apply();

                Intent intent = new Intent(Login.this, MainActivity.class);
                intent.putExtra("user_ID", userID);
                startActivity(intent);

            } else {
                password.setError("Password does not match");
            }
        } else {
            email.setError("Account does not exist");
            password.setError("Account does not exist");
        }
    }
}