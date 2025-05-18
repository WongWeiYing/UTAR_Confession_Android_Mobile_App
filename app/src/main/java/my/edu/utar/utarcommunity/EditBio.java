package my.edu.utar.utarcommunity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class EditBio extends AppCompatActivity implements View.OnClickListener {
    int responseCode;
    String userID;
    EditText editBio;
    ImageButton close, check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_bio);

        editBio= findViewById(R.id.bioTv);

        close=findViewById(R.id.close);
        check=findViewById(R.id.check);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("bio")) {
            editBio.setText(intent.getStringExtra("bio"));

        }
        if (intent != null && intent.hasExtra("userID")) {
            userID=intent.getStringExtra("userID");
        }

        close.setOnClickListener(this);
        check.setOnClickListener(this);

        setupTextChangeListeners();

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.check) {

            try {
                JSONObject request = new JSONObject();
                request.put("user_Caption", editBio.getText().toString().trim());
                Log.d("Request Payload", request.toString());
                new Write(request).start();

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        else{
            finish();
        }
    }
    private void setupTextChangeListeners() {

        editBio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editBio.setError(null); // Clear error message when the user starts typing
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private class Write extends Thread {
        private JSONObject request;

        public Write(JSONObject request) {
            this.request = request;
        }

        @Override
        public void run() {
            try {
                URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/USERS?id=eq." + userID);
                HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                hc.setRequestMethod("PATCH");
                hc.setRequestProperty("apikey", getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Authorization", "Bearer " + getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Content-Type", "application/json");
                hc.setRequestProperty("Prefer", "return=minimal");
                hc.setDoOutput(true);

                OutputStream os = hc.getOutputStream();
                os.write(request.toString().getBytes());
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
        Log.d("Response Code", "Response code: " + responseCode);
        if (responseCode == 200 || responseCode == 201 || responseCode == 204) {

            Intent intent = new Intent(EditBio.this, EditProfile.class);
            intent.putExtra("change", editBio.getText().toString().trim());
            setResult(RESULT_OK, intent);
            finish();
        }
    }

}