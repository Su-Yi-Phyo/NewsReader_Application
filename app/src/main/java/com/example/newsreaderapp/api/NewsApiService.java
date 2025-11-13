package com.example.newsreaderapp.api;

import com.example.newsreaderapp.models.NewsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {

    String BASE_URL = "https://newsapi.org/v2/";

    // Top headlines with optional category
    @GET("top-headlines")
    Call<NewsResponse> getTopHeadlines(
            @Query("country") String country,
            @Query("category") String category,
            @Query("page") Integer page,
            @Query("pageSize") Integer pageSize,
            @Query("apiKey") String apiKey
    );

    // Everything endpoint
    @GET("everything")
    Call<NewsResponse> searchEverything(
            @Query("q") String query,
            @Query("sortBy") String sortBy,
            @Query("page") Integer page,
            @Query("pageSize") Integer pageSize,
            @Query("apiKey") String apiKey
    );
}