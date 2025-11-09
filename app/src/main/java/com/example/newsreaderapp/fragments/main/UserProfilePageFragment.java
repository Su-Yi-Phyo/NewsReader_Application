package com.example.newsreaderapp.fragments.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.newsreaderapp.R;
import com.example.newsreaderapp.activities.LoginActivity;
import com.example.newsreaderapp.viewmodel.UserViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class UserProfilePageFragment extends Fragment {
    private UserViewModel viewModel;
    private TextView txtUsername, txtEmail;
    private TextView btnLogout, btnDeleteAccount;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_page_layout, container, false);

        txtUsername = view.findViewById(R.id.tvName);
        txtEmail = view.findViewById(R.id.tvEmail);

        btnLogout = view.findViewById(R.id.btnLogout);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAcc);
        // Lấy ViewModel scoped với MainActivity
        viewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId != -1) {
            viewModel.loadUserById(userId);
        }

        // Observe currentUser để hiển thị username
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                String email = user.getUsername();
                String[] parts = email.split("@");

                String username = parts[0];
                txtUsername.setText("Hello, " + username);
                txtEmail.setText(email);
            }
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            viewModel.logout();
            prefs.edit().remove("user_id").apply();
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        btnDeleteAccount.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        viewModel.deleteAccount();
                        prefs.edit().remove("user_id").apply();

                        Toast.makeText(getContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();

                        // Quay lại màn đăng nhập
                        startActivity(new Intent(requireActivity(), LoginActivity.class));
                        requireActivity().finish();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Observe errorMessage
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

}
