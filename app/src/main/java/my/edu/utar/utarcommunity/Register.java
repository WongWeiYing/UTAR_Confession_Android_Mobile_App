package my.edu.utar.utarcommunity;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
import java.util.HashMap;

public class Register extends AppCompatActivity implements View.OnClickListener{
    String name, displayName;
    EditText email, password, confirmedPass;
    Button register;
    TextView alrHasAcc;
    int responseCode;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        confirmedPass=findViewById(R.id.confirmedPass);
        register=findViewById(R.id.register);
        alrHasAcc=findViewById(R.id.alrHasAcc);

        register.setOnClickListener(this);
        alrHasAcc.setOnClickListener(this);

        setupTextChangeListeners();

        PasswordVisibilityTouchListener passwordTouchListener1 = new PasswordVisibilityTouchListener(password);
        password.setOnTouchListener(passwordTouchListener1);

        PasswordVisibilityTouchListener passwordTouchListener2 = new PasswordVisibilityTouchListener(confirmedPass);
        confirmedPass.setOnTouchListener(passwordTouchListener2);

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
                confirmedPass.setError(null); // Clear error message when the user starts typing
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        confirmedPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmedPass.setError(null); // Clear error message when the user starts typing
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.register){

            if(TextUtils.isEmpty(email.getText().toString().trim())){
                email.setError("Required");
                return;
            }
            else{
                if (!email.getText().toString().trim().endsWith("@1utar.my")) {
                    email.setError("@1utar.my");
                    return;
                }
                else{

                    int atIndex = email.getText().toString().trim().indexOf('@');
                    name = email.getText().toString().trim().substring(0, atIndex);
                    displayName = email.getText().toString().trim().substring(0, atIndex) + "_user";

                }
            }
            if(TextUtils.isEmpty(password.getText().toString().trim())){
                password.setError("Required");
                return;
            }
            else{
                if(password.length() < 6){
                    password.setError("Min 6 characters");
                    confirmedPass.setError("Min 6 characters");
                    return;
                }
            }
            if(TextUtils.isEmpty(confirmedPass.getText().toString().trim())){
                confirmedPass.setError("Required");
                return;
            }
            else{
                if(!password.getText().toString().trim().equals(confirmedPass.getText().toString().trim())) {
                    password.setError("Password does not match");
                    confirmedPass.setError("Password does not match");
                    return;
                }
            }

            try{
                JSONObject request = new JSONObject();
                request.put("user_Name", name);
                request.put("user_DisplayName", displayName);
                request.put("user_Email", email.getText().toString().trim());
                request.put("user_Password", password.getText().toString().trim());
                Log.d("Request Payload", request.toString());
                new Write(request).start();

            }catch (Exception ex){
                throw new RuntimeException(ex);
            }
        }
        else{
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
        }
    }
    private class Write extends Thread {
        private JSONObject request;
        public Write(JSONObject request) {
            this.request = request;
        }
        @Override
        public void run() {
            try {
                URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/USERS");
                HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                hc.setRequestMethod("POST");
                hc.setRequestProperty("apikey", getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Authorization", "Bearer " + getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Content-Type", "application/json");
                hc.setRequestProperty("Prefer", "return=minimal");
                hc.setDoOutput(true);

                OutputStream os = hc.getOutputStream();
                os.write(request.toString().getBytes());
                os.flush();

                responseCode = hc.getResponseCode();

                os.close();
                hc.disconnect();

                runOnUiThread(() -> handleLoginResult(responseCode));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleLoginResult(int responseCode) {
        if (responseCode == 200 || responseCode == 201 || responseCode == 204) {

            Toast.makeText(Register.this, "Account Created", Toast.LENGTH_SHORT).show();
            Log.i("Sign Up", "Account Created!" + responseCode);

            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
        } else if (responseCode == 0) {
            Toast.makeText(Register.this, "Connected to the server", Toast.LENGTH_SHORT).show();
        } else {
            Log.i("Sign Up", "Sign Up Failed" + responseCode);
            email.setError("Account exist.");
        }
    }

}