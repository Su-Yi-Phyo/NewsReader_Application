package com.example.newsreaderapp.fragments.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.example.newsreaderapp.activities.LoginActivity;
import com.example.newsreaderapp.activities.NewsDetailActivity;
import com.example.newsreaderapp.adapters.ArticlesAdapter;
import com.example.newsreaderapp.database.AppDatabase;
import com.example.newsreaderapp.models.Article;
import com.example.newsreaderapp.repository.UserRepository;
import com.example.newsreaderapp.viewmodel.UserViewModel;
import com.google.firebase.firestore.FirebaseFirestore;

public class BookmarksFragment extends Fragment {

    private UserViewModel userViewModel;
    private ArticlesAdapter adapter;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private String userId ;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bookmarks_layout, container, false);
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEmpty = view.findViewById(R.id.tvEmpty);

        recyclerView = view.findViewById(R.id.recyclerBookmarks);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Khoi tao adapter voi che do bookmark
        adapter = new ArticlesAdapter(ArticlesAdapter.Mode.BOOKMARKS);
        recyclerView.setAdapter(adapter);

        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        userId = prefs.getString("user_id", null);

        // Toolbar riêng cho fragment
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Bookmarks");
        toolbar.setTitleTextColor(Color.LTGRAY);
        toolbar.inflateMenu(R.menu.remove_all_menu);

        // Bắt sự kiện nhấn menu
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_remove_all) {
                showConfirmDeleteDialog();
                return true;
            }
            return false;
        });

        // Khởi tạo ViewModel
        AppDatabase db = AppDatabase.getInstance(requireContext());
        UserRepository repo= new UserRepository(db.userDao(), FirebaseFirestore.getInstance());
        UserViewModel.Factory factory = new UserViewModel.Factory(requireActivity().getApplication(), repo, userId);
        userViewModel = new ViewModelProvider(this, factory).get(UserViewModel.class);


        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getSavedArticles() != null && !user.getSavedArticles().isEmpty()) {
                adapter.updateList(user.getSavedArticles());
                tvEmpty.setVisibility(View.GONE);
            } else {
                adapter.updateList(null);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });

        // Xử lý click
        adapter.setOnArticleActionListener(new ArticlesAdapter.OnArticleActionListener() {
            @Override
            public void onArticleClick(Article a) {
                Intent it = new Intent(getContext(), NewsDetailActivity.class);
                it.putExtra("title", a.getTitle());
                it.putExtra("description", a.getDescription());
                it.putExtra("content", a.getContent());
                it.putExtra("imageUrl", a.getUrlToImage());
                it.putExtra("publishedAt", a.getPublishedAt());
                it.putExtra("url", a.getUrl());
                startActivity(it);
            }

//            @Override
//            public void onSaveClick(Article a) {
//            }  // Không dùng

            @Override
            public void onLikeClick(Article a) {
            }  // Không dùng

            @Override
            public void onRemoveClick(Article a) {
                userViewModel.unsaveArticle(userId, a);
                Toast.makeText(getContext(), "Unsaved", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showConfirmDeleteDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm")
                .setMessage("Are you sure you want to delete all saved articles?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    deleteAllBookmarks();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteAllBookmarks() {
        userViewModel.repo.clearAllSavedArticles(userId);
        Toast.makeText(getContext(), "All saved articles have been deleted", Toast.LENGTH_SHORT).show();
    }
}
