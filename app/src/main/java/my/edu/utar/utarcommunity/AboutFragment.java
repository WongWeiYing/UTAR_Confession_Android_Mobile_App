package my.edu.utar.utarcommunity;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.UUID;

public class AboutFragment extends Fragment {

    UUID user_ID;

    public AboutFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args != null)
            user_ID = UUID.fromString(args.getString("user_ID"));

        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        ImageButton bt_BackHome = rootView.findViewById(R.id.bt_BackHome);

        bt_BackHome.setOnClickListener(e -> {
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.putExtra("user_ID", user_ID.toString());
            startActivity(intent);
        });

        return rootView;
    }
}
