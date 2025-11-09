package com.example.newsreaderapp.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsreaderapp.R;
import com.example.newsreaderapp.adapters.NewsAdapter;
import com.example.newsreaderapp.api.RetrofitClient;
import com.example.newsreaderapp.models.NewsResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomePageFragment extends Fragment {

    private static final String API_KEY = "bbf35dae0783409e93ac4460209b2259";

    private RecyclerView recyclerView;
    private NewsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_page_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewNewsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NewsAdapter();
        recyclerView.setAdapter(adapter);

        loadTopHeadlines();
    }

    private void loadTopHeadlines() {
        RetrofitClient.getService()
                .getTopHeadlines(
                        "us",          // country
                        null,          // category (null = all)
                        1,             // page
                        20,            // page size
                        API_KEY        // api key
                )
                .enqueue(new Callback<NewsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.setItems(response.body().getArticles());
                        } else {
                            Toast.makeText(requireContext(), "Failed to load news", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}