package my.edu.utar.utarcommunity;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.UUID;


public class ReviewFragment extends Fragment {

    UUID user_ID;

    public ReviewFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args != null)
            user_ID = UUID.fromString(args.getString("user_ID"));

        View rootView = inflater.inflate(R.layout.fragment_review, container, false);

        Button rate = rootView.findViewById(R.id.rating_btn);

        ImageButton bt_BackHome = rootView.findViewById(R.id.bt_BackHome);

        bt_BackHome.setOnClickListener(e -> {
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.putExtra("user_ID", user_ID.toString());
            startActivity(intent);
        });

        rate.setOnClickListener(v -> Toast.makeText(getContext(), "Submitted! Thank you for your support", Toast.LENGTH_SHORT).show());

        return rootView;

    }
}