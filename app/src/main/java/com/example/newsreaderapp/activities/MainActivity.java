package com.example.newsreaderapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.newsreaderapp.R;
import com.example.newsreaderapp.fragments.main.HistoryPageFragment;
import com.example.newsreaderapp.fragments.main.HomePageFragment;
import com.example.newsreaderapp.fragments.main.TrendingPageFragment;
import com.example.newsreaderapp.fragments.main.UserProfilePageFragment;
import com.example.newsreaderapp.viewmodel.UserViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    private UserViewModel viewModel;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
//        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .build();
//
//        googleSignInClient = GoogleSignIn.getClient(this, gso);
//
//        GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(this);

        bottomNavigationView = findViewById(R.id.bottom_nav);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);
        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        if (userId == null){
            // Không có user -> quay về login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        // Mặc định mở tab "Tin tức"
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_container, new HomePageFragment())
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
                        .replace(R.id.frame_container, selected)
                        .commit();
            }

            return true;
        });
    }
}