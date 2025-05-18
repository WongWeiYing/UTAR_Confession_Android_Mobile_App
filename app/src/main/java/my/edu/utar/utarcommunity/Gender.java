package my.edu.utar.utarcommunity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class Gender extends AppCompatActivity implements View.OnClickListener {

    int responseCode;
    String gd, userID, gender;
    RadioButton option1, option2, option3;
    ImageButton close, check;
    RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gender);

        option1=findViewById(R.id.option1);
        option2=findViewById(R.id.option2);
        option3=findViewById(R.id.option3);

        radioGroup=findViewById(R.id.radioGroup);

        close=findViewById(R.id.close);
        check=findViewById(R.id.check);

        close.setOnClickListener(this);
        check.setOnClickListener(this);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("gender")) {
            gd = intent.getStringExtra("gender");
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                View view = radioGroup.getChildAt(i);
                if (view instanceof RadioButton) {
                    RadioButton radioButton = (RadioButton) view;
                    if (radioButton.getText().toString().equals(gd)) {
                        radioButton.setChecked(true);
                        break;
                    }
                }
            }
        }
        if (intent != null && intent.hasExtra("userID")) {
            userID=intent.getStringExtra("userID");
        }
    }

    @Override
    public void onClick(View v) {

        if(option1.isChecked()){
            gender = option1.getText().toString();
        }
        else if(option2.isChecked()){
            gender = option2.getText().toString();
        }
        else if(option3.isChecked()){
            gender = option2.getText().toString();
        }

        if(v.getId() == R.id.check) {
            try{
                JSONObject request = new JSONObject();
                request.put("user_Gender", gender);
                Log.d("Request Payload", request.toString());
                new Write(request).start();

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            /*
            if (responseCode == 200 || responseCode == 201 || responseCode == 204) {

                Intent intent = new Intent(Gender.this, Profile.class);
                intent.putExtra("change", gender);
                setResult(RESULT_OK, intent);
                finish();
            }*/
        }
        else{
           finish();
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
        if (responseCode == 200 || responseCode == 201 || responseCode == 204) {

            Intent intent = new Intent(Gender.this, EditProfile.class);
            intent.putExtra("change", gender);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
