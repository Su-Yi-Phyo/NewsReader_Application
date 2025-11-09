package com.example.newsreaderapp.api;

import com.example.newsreaderapp.models.NewsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {

    String BASE_URL = "https://newsapi.org/v2/";

    // Top headlines with optional category (e.g., "business", "technology", ...)
    @GET("top-headlines")
    Call<NewsResponse> getTopHeadlines(
            @Query("country") String country,       // e.g., "us"
            @Query("category") String category,     // e.g., "business" or null for all
            @Query("page") Integer page,            // page number (optional)
            @Query("pageSize") Integer pageSize,    // items per page (optional)
            @Query("apiKey") String apiKey          // your API key
    );

    // Everything endpoint (optional, for search later if needed)
    @GET("everything")
    Call<NewsResponse> searchEverything(
            @Query("q") String query,
            @Query("sortBy") String sortBy,         // "publishedAt", "relevancy", "popularity"
            @Query("page") Integer page,
            @Query("pageSize") Integer pageSize,
            @Query("apiKey") String apiKey
    );
}