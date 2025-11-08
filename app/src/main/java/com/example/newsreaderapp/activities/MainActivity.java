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
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_nav);

        // Mặc định mở tab "Tin tức"
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new HomePageFragment())
                .commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selected = null;

            int itemId = item.getItemId();

            if (itemId == R.id.nav_news) {
                selected = new HomePageFragment();
            } else if (itemId == R.id.nav_trending) {
                selected = new TrendingPageFragment();
            } else if (itemId == R.id.nav_history) {
                selected = new HistoryPageFragment();
            } else if (itemId == R.id.nav_profile) {
                selected = new UserProfilePageFragment();
            }

            if (selected != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, selected)
                        .commit();
            }

            return true;
        });
    }
}