package com.example.newsreaderapp.fragments.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.newsreaderapp.R;
import com.example.newsreaderapp.activities.LoginActivity;
import com.example.newsreaderapp.viewmodel.UserViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserProfilePageFragment extends Fragment {
    private UserViewModel viewModel;
    private ImageView imgAvatar;
    private TextView txtUsername, txtEmail;
    private TextView btnLogout, btnDeleteAccount;

    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    private String currentUserId;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_page_layout, container, false);

        imgAvatar = view.findViewById(R.id.imgAvatar);
        txtUsername = view.findViewById(R.id.tvName);
        txtEmail = view.findViewById(R.id.tvEmail);

        btnLogout = view.findViewById(R.id.btnLogout);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAcc);

        mAuth = FirebaseAuth.getInstance();

        // Khởi tạo GoogleSignInClient giống LoginActivity
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        // Xử lý sự kiện
        loadUserInfo();
        setupListeners();

        // Lấy ViewModel scoped với MainActivity
        viewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);

        if (userId != null) {
            viewModel.getUserById(userId);
        }

        // Observe currentUser để hiển thị username
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                String email = user.getEmail();
                String username = user.getDisplayName();
                txtUsername.setText("Hello, " + username);
                txtEmail.setText(email);
            }
        });


        return view;
    }
    private void loadUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            txtUsername.setText(user.getDisplayName() != null ? user.getDisplayName() : "Unknown User");
            txtEmail.setText(user.getEmail());

            Uri photoUrl = user.getPhotoUrl();
            if (photoUrl != null) {
                // Dùng Glide để load avatar
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.user)
                        .circleCrop()
                        .into(imgAvatar);
            } else {
                imgAvatar.setImageResource(R.drawable.user);
            }
        }
    }
    private void setupListeners() {
        // Đăng xuất
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Xóa SharedPreferences
                        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                        prefs.edit().clear().apply();

                        // Firebase logout
                        mAuth.signOut();

                        // Google logout
                        googleSignInClient.signOut();

                        // Điều hướng về LoginActivity
                        startActivity(new Intent(requireActivity(), LoginActivity.class));
                        requireActivity().finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Xóa tài khoản
        btnDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Account")
                    .setMessage("This action cannot be undone. Are you sure?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        FirebaseUser fUser = mAuth.getCurrentUser();

                        // Lấy userId từ SharedPreferences
                        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                        String userId = prefs.getString("user_id", null);

                        if (fUser != null) {
                            // Xóa trên Firebase
                            fUser.delete().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {

                                    // Xóa trên Room
                                    if (userId != null) {
                                        viewModel.deleteUserById(userId);
                                    }

                                    // Xóa SharedPreferences
                                    prefs.edit().clear().apply();

                                    // Google logout
                                    googleSignInClient.signOut();

                                    Toast.makeText(getContext(), "Account deleted", Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(requireActivity(), LoginActivity.class));
                                    requireActivity().finish();

                                } else {
                                    Toast.makeText(getContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}

