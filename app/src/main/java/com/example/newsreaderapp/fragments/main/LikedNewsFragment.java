package com.example.newsreaderapp.fragments.main;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsreaderapp.R;
import com.example.newsreaderapp.adapters.ArticlesAdapter;
import com.example.newsreaderapp.adapters.NewsAdapter;
import com.example.newsreaderapp.database.AppDatabase;
import com.example.newsreaderapp.helper.Converters;
import com.example.newsreaderapp.models.Article;
import com.example.newsreaderapp.repository.UserRepository;
import com.example.newsreaderapp.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class LikedNewsFragment extends Fragment {

    private ArticlesAdapter adapter;
    private UserViewModel userViewModel;
    private String userId;
    private TextView tvEmpty;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recently_liked_layout, container, false);

        tvEmpty = view.findViewById(R.id.tvEmpty);
        // Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Liked Articles");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.remove_all_menu);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_remove_all) {
                showConfirmDeleteDialog();
                return true;
            }
            return false;
        });

        // RecyclerView setup
        RecyclerView recyclerView = view.findViewById(R.id.recyclerLiked);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khoi tao adapter voi che do liked
        adapter = new ArticlesAdapter(ArticlesAdapter.Mode.LIKED);
        recyclerView.setAdapter(adapter);

        // Repository + ViewModel
        UserRepository repo = new UserRepository(
                AppDatabase.getInstance(requireContext()).userDao(),
                FirebaseFirestore.getInstance()
        );

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        UserViewModel.Factory factory = new UserViewModel.Factory(requireActivity().getApplication(), repo, userId);
        userViewModel = new ViewModelProvider(this, factory).get(UserViewModel.class);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Error fetching data", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Object rawLiked = snapshot.get("likedArticles");
                        List<Article> likedArticles = Converters.toArticleList(new Gson().toJson(rawLiked));

                        if (likedArticles.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                        }

                        adapter.updateList(likedArticles);
                    } else {
                        tvEmpty.setVisibility(View.VISIBLE);
                        adapter.updateList(new ArrayList<>());
                    }
                });


        // 🔹 Xử lý click
        adapter.setOnArticleActionListener(new ArticlesAdapter.OnArticleActionListener() {
            @Override
            public void onArticleClick(Article a) {
                Intent it = new Intent(getContext(), com.example.newsreaderapp.activities.NewsDetailActivity.class);
                it.putExtra("title", a.getTitle());
                it.putExtra("description", a.getDescription());
                it.putExtra("content", a.getContent());
                it.putExtra("imageUrl", a.getUrlToImage());
                it.putExtra("publishedAt", a.getPublishedAt());
                it.putExtra("url", a.getUrl());
                startActivity(it);
            }

            @Override
            public void onSaveClick(Article a) {}  // Không dùng
            @Override
            public void onLikeClick(Article a) {}  // Không dùng
            @Override
            public void onRemoveClick(Article a) {
                userViewModel.unlikeArticle(userId, a);
                Toast.makeText(getContext(), "Unliked", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    private void showConfirmDeleteDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm")
                .setMessage("Are you sure you want to remove all liked articles?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    clearAllLikes();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void clearAllLikes() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update("likedArticles", new ArrayList<>())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "All liked articles have been removed", Toast.LENGTH_SHORT).show();
                    adapter.updateList(new ArrayList<>());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
