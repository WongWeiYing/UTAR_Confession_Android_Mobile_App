package my.edu.utar.utarcommunity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.provider.MediaStore;
import android.widget.Toast;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity implements View.OnClickListener {

    String userID, name, username, bio, gender;
    TextView nameTv, usernameTv, bioTv, genderTv;
    LinearLayout genderNav;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        nameTv=findViewById(R.id.nameTv);
        usernameTv=findViewById(R.id.usernameTv);
        bioTv=findViewById(R.id.bioTv);
        genderNav=findViewById(R.id.genderNav);
        genderTv=findViewById(R.id.genderTv);
        back=findViewById(R.id.back);

        nameTv.setOnClickListener(this);
        usernameTv.setOnClickListener(this);
        bioTv.setOnClickListener(this);
        genderNav.setOnClickListener(this);
        back.setOnClickListener(this);

        Intent intent = getIntent();
        userID=intent.getStringExtra("userID");
        name=intent.getStringExtra("name");
        username=intent.getStringExtra("username");
        bio=intent.getStringExtra("bio");
        gender= intent.getStringExtra("gender");

        nameTv.setText(name);
        usernameTv.setText(username);
        bioTv.setText(bio);
        genderTv.setText(gender);

        Log.d("Profile", "User ID: " + userID);

       // new Read().start();
    }
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.genderNav){
            Log.d("TextView Click", "Name TextView clicked");
            Intent intent = new Intent(EditProfile.this, Gender.class);
            intent.putExtra("userID", userID);
            intent.putExtra("gender",gender);
            startActivityForResult(intent, 2);
        }
        else if(v.getId() == R.id.nameTv){
            Log.d("TextView Click", "Name TextView clicked");
            Intent intent = new Intent(EditProfile.this, EditName.class);
            intent.putExtra("userID", userID);
            intent.putExtra("name",name);
            startActivityForResult(intent, 3);
        }
        else if(v.getId() == R.id.usernameTv){
            Intent intent = new Intent(EditProfile.this, EditUsername.class);
            intent.putExtra("userID", userID);
            intent.putExtra("username",username);
            startActivityForResult(intent, 4);
        }
        else if(v.getId() == R.id.bioTv){
            Intent intent = new Intent(EditProfile.this, EditBio.class);
            intent.putExtra("userID", userID);
            intent.putExtra("bio",bio);
            startActivityForResult(intent, 5);
        }
        else if(v.getId()==R.id.back){
            Intent intent = new Intent(EditProfile.this, ProfileFragment.class);
            intent.putExtra("changeName",nameTv.getText().toString().trim());
            intent.putExtra("changeUsername",usernameTv.getText().toString().trim());
            intent.putExtra("changeBio",bioTv.getText().toString().trim());
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {
            String option = data.getStringExtra("change");
            genderTv.setText(option);
            Toast.makeText(EditProfile.this, "Gender Changed", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 3 && resultCode == RESULT_OK) {
            String option = data.getStringExtra("change");
            nameTv.setText(option);Toast.makeText(EditProfile.this, "Name Changed", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 4 && resultCode == RESULT_OK) {
            String option = data.getStringExtra("change");
            usernameTv.setText(option);
            Toast.makeText(EditProfile.this, "Username Changed", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 5 && resultCode == RESULT_OK) {
            String option = data.getStringExtra("change");
            bioTv.setText(option);
            Toast.makeText(EditProfile.this, "Bio Changed", Toast.LENGTH_SHORT).show();
        }

    }
}