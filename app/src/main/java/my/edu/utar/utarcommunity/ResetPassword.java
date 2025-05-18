package my.edu.utar.utarcommunity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ResetPassword extends AppCompatActivity implements View.OnClickListener{

    int responseCode;
    EditText passwordResetEt, confirmedEt, emailResetEt;
    Button resetBt;
    String emailReset;
    TextView logInTv;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password);

        emailResetEt=findViewById(R.id.emailReset);
        passwordResetEt=findViewById(R.id.passwordReset);
        confirmedEt=findViewById(R.id.confirmed);
        resetBt=findViewById(R.id.reset);
        logInTv=findViewById(R.id.logIn);

        resetBt.setOnClickListener(this);
        logInTv.setOnClickListener(this);

        setupTextChangeListeners();

        PasswordVisibilityTouchListener passwordTouchListener1 = new PasswordVisibilityTouchListener(passwordResetEt);
        passwordResetEt.setOnTouchListener(passwordTouchListener1);

        PasswordVisibilityTouchListener passwordTouchListener2 = new PasswordVisibilityTouchListener(confirmedEt);
        confirmedEt.setOnTouchListener(passwordTouchListener2);
    }

    private void setupTextChangeListeners() {
        emailResetEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailResetEt.setError(null); // Clear error message when the user starts typing
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        passwordResetEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordResetEt.setError(null); // Clear error message when the user starts typing
                confirmedEt.setError(null); // Clear error message when the user starts typing
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        confirmedEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmedEt.setError(null); // Clear error message when the user starts typing
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.reset){

            emailReset = emailResetEt.getText().toString().trim();
            String passwordReset = passwordResetEt.getText().toString().trim();
            String confirmed = confirmedEt.getText().toString().trim();

            if (TextUtils.isEmpty(emailReset)) {
                passwordResetEt.setError("Required");
                return;
            }
            if (TextUtils.isEmpty(passwordReset)) {
                passwordResetEt.setError("Required");
                return;
            }
            if (TextUtils.isEmpty(confirmed)) {
                confirmedEt.setError("Required");
                return;
            }
            if(passwordReset.length() < 6){
                passwordResetEt.setError("Min 6 characters");
                confirmedEt.setError("Min 6 characters");
                return;
            }
            if(!passwordReset.equals(confirmed)){
                passwordResetEt.setError("Password does not match");
                confirmedEt.setError("Password does not match");
                return;
            }
            try{
                JSONObject reset = new JSONObject();
                reset.put("user_Password", passwordReset);
                new Write(reset).start();

            }catch (Exception ex){
                throw new RuntimeException(ex);
            }
        }
        else{
            finish();
        }
    }

    private class Write extends Thread{
        private JSONObject reset;
        public Write(JSONObject reset) {
            this.reset = reset;
        }
        public void run() {
            try {
                URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/USERS?user_Email=eq." + emailReset);
                HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                hc.setRequestMethod("PATCH");
                hc.setRequestProperty("apikey", getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Authorization", "Bearer " + getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Content-Type", "application/json");
                hc.setRequestProperty("Prefer", "return=minimal");
                hc.setDoOutput(true);

                OutputStream os = hc.getOutputStream();
                os.write(reset.toString().getBytes());
                os.flush();

                responseCode = hc.getResponseCode();
                Log.i("Update", "Update" + responseCode);

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
            Log.i("Insert", "Insert " + responseCode);
            Toast.makeText(ResetPassword.this, "Password Reset", Toast.LENGTH_SHORT).show();
            Log.i("Sign Up", "Password Reset!" + responseCode);
            Intent intent = new Intent(ResetPassword.this, Login.class);
            startActivity(intent);
        } else if (responseCode == 0) {
            Toast.makeText(ResetPassword.this, "Connected to the server", Toast.LENGTH_SHORT).show();
        } else {
            emailResetEt.setError("Account Not Found");
            Log.i("Sign Up", "Sign Up Failed" + responseCode);
            Toast.makeText(ResetPassword.this, "Password Changed Failed. Please try again later", Toast.LENGTH_SHORT).show();
        }
    }
}