package com.example.newsreaderapp.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.newsreaderapp.api.RetrofitClient;
import com.example.newsreaderapp.models.Article;
import com.example.newsreaderapp.models.NewsResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsViewModel extends ViewModel {

    private static final String API_KEY = "bbf35dae0783409e93ac4460209b2259";

    private final MutableLiveData<List<Article>> _articles = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<Article>> articles = _articles;

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    public LiveData<Boolean> loading = _loading;

    private final MutableLiveData<String> _error = new MutableLiveData<>(null);
    public LiveData<String> error = _error;

    /** Top-headlines by category (Home tabs). Pass null for "Home"/all. */
    public void fetchTopHeadlines(String category, int page, int pageSize, boolean append) {
        _loading.setValue(true);
        RetrofitClient.getService()
                .getTopHeadlines("us", category, page, pageSize, API_KEY)
                .enqueue(new Callback<NewsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                        _loading.postValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<Article> incoming = response.body().getArticles();
                            if (append) {
                                List<Article> current = new ArrayList<>(_articles.getValue() != null ? _articles.getValue() : new ArrayList<>());
                                if (incoming != null) current.addAll(incoming);
                                _articles.postValue(current);
                            } else {
                                _articles.postValue(incoming != null ? incoming : new ArrayList<>());
                            }
                            _error.postValue(null);
                        } else {
                            _error.postValue("Failed to load");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                        _loading.postValue(false);
                        _error.postValue(t.getMessage());
                    }
                });
    }
    public void fetchMostRead(int page, int pageSize, boolean append) {
        // For now we reuse top-headlines like the fragment did (country = "us", no category)
        _loading.setValue(true);
        RetrofitClient.getService()
                .getTopHeadlines("us", null, page, pageSize, API_KEY)
                .enqueue(new Callback<NewsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                        _loading.postValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<Article> incoming = response.body().getArticles();
                            if (append) {
                                List<Article> current = new ArrayList<>(_articles.getValue() != null ? _articles.getValue() : new ArrayList<>());
                                if (incoming != null) current.addAll(incoming);
                                _articles.postValue(current);
                            } else {
                                _articles.postValue(incoming != null ? incoming : new ArrayList<>());
                            }
                            _error.postValue(null);
                        } else {
                            _error.postValue("Failed to load");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                        _loading.postValue(false);
                        _error.postValue(t.getMessage());
                    }
                });
    }

    public void clear() {
        _articles.setValue(new ArrayList<>());
        _error.setValue(null);
    }
}