//package com.example.newsreaderapp.fragments.main;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProvider;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.newsreaderapp.R;
//import com.example.newsreaderapp.adapters.NewsAdapter;
//import com.example.newsreaderapp.viewmodel.NewsViewModel;
//import com.google.android.material.tabs.TabLayout;
//
//public class HomePageFragment extends Fragment {
//
//    private RecyclerView recyclerViewNewsList;
//    private NewsAdapter adapter;
//
//    private int currentPage = 1;
//    private final int pageSize = 20;
//    @Nullable private String currentCategory = null; // null = Home/all
//
//    private NewsViewModel vm;
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.home_page_layout, container, false);
//        return view;
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        recyclerViewNewsList = view.findViewById(R.id.recyclerViewNewsList);
//        recyclerViewNewsList.setLayoutManager(new LinearLayoutManager(requireContext()));
//        adapter = new NewsAdapter();
//        recyclerViewNewsList.setAdapter(adapter);
//
//        TabLayout tabs = view.findViewById(R.id.HomeTabLayout);
//
//        vm = new ViewModelProvider(this).get(NewsViewModel.class);
//
//        // observe
//        vm.articles.observe(getViewLifecycleOwner(), articles -> {
//            if (articles != null) adapter.setItems(articles);
//        });
//
//        vm.loading.observe(getViewLifecycleOwner(), isLoading -> {
//            // no UI spinner here; just used by pagination guard
//        });
//
//        vm.error.observe(getViewLifecycleOwner(), err -> {
//            if (err != null && isAdded()) {
//                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // initial load
//        currentCategory = null; // Home
//        currentPage = 1;
//        vm.clear();
//        vm.fetchTopHeadlines(currentCategory, currentPage, pageSize, false);
//
//        // tabs
//        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                String label = tab.getText() != null ? tab.getText().toString() : "";
//                currentCategory = resolveCategory(label);
//                currentPage = 1;
//                vm.clear();
//                vm.fetchTopHeadlines(currentCategory, currentPage, pageSize, false);
//            }
//            @Override public void onTabUnselected(TabLayout.Tab tab) {}
//            @Override public void onTabReselected(TabLayout.Tab tab) {}
//        });
//
//        // pagination
//        recyclerViewNewsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
//                super.onScrolled(rv, dx, dy);
//                if (dy <= 0) return;
//
//                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
//                if (lm == null) return;
//
//                int visible = lm.getChildCount();
//                int total   = lm.getItemCount();
//                int first   = lm.findFirstVisibleItemPosition();
//
//                boolean nearEnd = (visible + first) >= (total - 4);
//                Boolean loading = vm.loading.getValue();
//                if (nearEnd && (loading == null || !loading)) {
//                    currentPage++;
//                    vm.fetchTopHeadlines(currentCategory, currentPage, pageSize, true);
//                }
//            }
//        });
//    }
//
//    private String resolveCategory(String label) {
//        switch (label) {
//            case "Home":          return null;
//            case "Business":      return "business";
//            case "Technology":    return "technology";
//            case "Sports":        return "sports";
//            case "Entertainment": return "entertainment";
//            case "Health":        return "health";
//            case "Science":       return "science";
//            default:              return null;
//        }
//    }
//}
package com.example.newsreaderapp.fragments.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsreaderapp.R;
import com.example.newsreaderapp.adapters.ArticlesAdapter;
import com.example.newsreaderapp.database.AppDatabase;
import com.example.newsreaderapp.database.UserEntity;
import com.example.newsreaderapp.models.Article;
import com.example.newsreaderapp.repository.UserRepository;
import com.example.newsreaderapp.viewmodel.NewsViewModel;
import com.example.newsreaderapp.viewmodel.UserViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomePageFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArticlesAdapter adapter;
    private NewsViewModel vm;
    private UserViewModel userViewModel;
    private int currentPage = 1;
    private final int pageSize = 20;
    @Nullable private String currentCategory = null;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId = FirebaseAuth.getInstance().getUid();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_page_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewNewsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new ArticlesAdapter(ArticlesAdapter.Mode.HOME);
        recyclerView.setAdapter(adapter);
        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);

        vm = new ViewModelProvider(this).get(NewsViewModel.class);
        // Khởi tạo ViewModel
        AppDatabase db = AppDatabase.getInstance(requireContext());
        UserRepository repo= new UserRepository(db.userDao(), FirebaseFirestore.getInstance());
        UserViewModel.Factory factory = new UserViewModel.Factory(requireActivity().getApplication(), repo, userId);
        userViewModel = new ViewModelProvider(this, factory).get(UserViewModel.class);

//        loadSavedArticles(userId);
        loadLikedArticles();

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

//            @Override
//            public void onSaveClick(Article article) {
//                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show();
//                userViewModel.saveArticle(userId, article);
//            }

            @Override
            public void onLikeClick(Article article) {
                if (userId == null) return;
                userViewModel.likeArticle(userId, article);
            }

            @Override
            public void onRemoveClick(Article article) {
                // không dùng trong HOME mode
            }
        });

        TabLayout tabs = view.findViewById(R.id.HomeTabLayout);
        setupTabs(tabs);

        observeViewModel();

        loadPage(1, false);

        setupPagination();
    }

    private void setupTabs(TabLayout tabs) {
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String label = tab.getText() != null ? tab.getText().toString() : "";
                currentCategory = resolveCategory(label);
                currentPage = 1;
                vm.clear();
                loadPage(currentPage, false);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void observeViewModel() {
        vm.articles.observe(getViewLifecycleOwner(), articles -> {
            if (articles != null) adapter.updateList(articles);
        });

        vm.error.observe(getViewLifecycleOwner(), err -> {
            if (err != null && isAdded()) {
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupPagination() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                if (dy <= 0) return;

                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm == null) return;

                int visible = lm.getChildCount();
                int total   = lm.getItemCount();
                int first   = lm.findFirstVisibleItemPosition();

                Boolean loading = vm.loading.getValue();
                if ((visible + first) >= (total - 4) && (loading == null || !loading)) {
                    currentPage++;
                    loadPage(currentPage, true);
                }
            }
        });
    }

    private void loadPage(int page, boolean append) {
        vm.fetchTopHeadlines(currentCategory, page, pageSize, append);
    }

    private String resolveCategory(String label) {
        switch (label) {
            case "Home":          return null;
            case "Business":      return "business";
            case "Technology":    return "technology";
            case "Sports":        return "sports";
            case "Entertainment": return "entertainment";
            case "Health":        return "health";
            case "Science":       return "science";
            default:              return null;
        }
    }
//    //  Load savedArticles từ Room và set trạng thái nút
//    private void loadSavedArticles(String userId) {
//        new Thread(() -> {
//            UserEntity user = AppDatabase.getInstance(requireContext())
//                    .userDao()
//                    .getUserById(userId); // phương thức lấy user hiện tại
//            if (user != null) {
//                List<Article> saved = user.getSavedArticles();
//                requireActivity().runOnUiThread(() -> adapter.setSavedArticles(saved));
//            }
//        }).start();
//    }

    //  Load likedArticles từ Firestore và set trạng thái nút
    private void loadLikedArticles() {
        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<Article> likedArticles = new ArrayList<>();
                        List<?> list = (List<?>) doc.get("likedArticles");
                        if (list != null) {
                            Gson gson = new Gson();
                            for (Object obj : list) {
                                if (obj instanceof Map) {
                                    Article a = gson.fromJson(gson.toJson(obj), Article.class);
                                    likedArticles.add(a);
                                }
                            }
                        }
                        adapter.setLikedArticles(likedArticles);
                    }
                });
    }
}
