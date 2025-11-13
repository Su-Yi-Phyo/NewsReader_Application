package com.example.newsreaderapp.fragments.main;

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
import com.example.newsreaderapp.adapters.NewsAdapter;
import com.example.newsreaderapp.viewmodel.NewsViewModel;
import com.google.android.material.tabs.TabLayout;

public class HomePageFragment extends Fragment {

    private RecyclerView recyclerViewNewsList;
    private NewsAdapter adapter;

    private int currentPage = 1;
    private final int pageSize = 20;
    @Nullable private String currentCategory = null; // null = Home/all

    private NewsViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_page_layout, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewNewsList = view.findViewById(R.id.recyclerViewNewsList);
        recyclerViewNewsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NewsAdapter();
        recyclerViewNewsList.setAdapter(adapter);

        TabLayout tabs = view.findViewById(R.id.HomeTabLayout);

        vm = new ViewModelProvider(this).get(NewsViewModel.class);

        // observe
        vm.articles.observe(getViewLifecycleOwner(), articles -> {
            if (articles != null) adapter.setItems(articles);
        });

        vm.loading.observe(getViewLifecycleOwner(), isLoading -> {
            // no UI spinner here; just used by pagination guard
        });

        vm.error.observe(getViewLifecycleOwner(), err -> {
            if (err != null && isAdded()) {
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
            }
        });

        // initial load
        currentCategory = null; // Home
        currentPage = 1;
        vm.clear();
        vm.fetchTopHeadlines(currentCategory, currentPage, pageSize, false);

        // tabs
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String label = tab.getText() != null ? tab.getText().toString() : "";
                currentCategory = resolveCategory(label);
                currentPage = 1;
                vm.clear();
                vm.fetchTopHeadlines(currentCategory, currentPage, pageSize, false);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // pagination
        recyclerViewNewsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                if (dy <= 0) return;

                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm == null) return;

                int visible = lm.getChildCount();
                int total   = lm.getItemCount();
                int first   = lm.findFirstVisibleItemPosition();

                boolean nearEnd = (visible + first) >= (total - 4);
                Boolean loading = vm.loading.getValue();
                if (nearEnd && (loading == null || !loading)) {
                    currentPage++;
                    vm.fetchTopHeadlines(currentCategory, currentPage, pageSize, true);
                }
            }
        });
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
}