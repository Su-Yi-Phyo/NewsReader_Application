package com.example.newsreaderapp.activities;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.newsreaderapp.R;
import com.example.newsreaderapp.fragments.main.HistoryPageFragment;
import com.example.newsreaderapp.fragments.main.HomePageFragment;
import com.example.newsreaderapp.fragments.main.TrendingPageFragment;
import com.example.newsreaderapp.fragments.main.UserProfilePageFragment;

public class MainActivity extends AppCompatActivity {

    private ImageView btnHome, btnTrending, btnHistory, btnProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find buttons first
        btnHome = findViewById(R.id.btnHome);
        btnTrending = findViewById(R.id.btnTrending);
        btnHistory = findViewById(R.id.btnHistory);
        btnProfile = findViewById(R.id.btnProfile);

        //Load Homepage when the app starts
        loadFragment(new HomePageFragment());

        //set up click listeners
        btnHome.setOnClickListener(v -> loadFragment(new HomePageFragment()));
        btnTrending.setOnClickListener(v -> loadFragment(new TrendingPageFragment()));
        btnHistory.setOnClickListener(v -> loadFragment(new HistoryPageFragment()));
        btnProfile.setOnClickListener(v -> loadFragment(new UserProfilePageFragment()));
    }

    private void loadFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
    }
}