package com.example.newsreaderapp.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.newsreaderapp.R;
import com.example.newsreaderapp.database.AppDatabase;
import com.example.newsreaderapp.models.Article;
import com.example.newsreaderapp.repository.ReaderModeRepository;
import com.example.newsreaderapp.repository.UserRepository;
import com.example.newsreaderapp.viewmodel.UserViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NewsDetailActivity extends AppCompatActivity {

    private static final String TAG = "NewsDetailActivity";

    private ImageView btnBack, imgDetailNews, btnDetailLike, btnDetailBookmark;
    private TextView txtDetailTitle, txtDetailContent, txtUploadTime;
    private View topActionBar, scrollableContent;

    private UserViewModel userViewModel;
    private String userId;
    private Article article;

    private boolean isLiked = false;
    private boolean isSaved = false;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> runningTask;
    private final ReaderModeRepository readerRepo = new ReaderModeRepository();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_detail_layout);

        // Padding for system bars
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // SharedPreferences for userId
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        userId = prefs.getString("user_id", null);

        // Initialize ViewModel
        AppDatabase dbRoom = AppDatabase.getInstance(this);
        UserRepository repo = new UserRepository(dbRoom.userDao(), FirebaseFirestore.getInstance());
        UserViewModel.Factory factory = new UserViewModel.Factory(getApplication(), repo, userId);
        userViewModel = new ViewModelProvider(this, factory).get(UserViewModel.class);

        // Bind views
        btnBack = findViewById(R.id.btnBack);
        imgDetailNews = findViewById(R.id.imgDetailNews);
        txtDetailTitle = findViewById(R.id.txtDetailTitle);
        txtDetailContent = findViewById(R.id.txtDetailContent);
        txtUploadTime = findViewById(R.id.txtUploadTime);
        topActionBar = findViewById(R.id.topActionBar);
        scrollableContent = findViewById(R.id.scrollableContent);
        btnDetailLike = findViewById(R.id.btnDetailLike);
        btnDetailBookmark = findViewById(R.id.btnDetailBookmark);

        // Get article from Intent
        article = (Article) getIntent().getSerializableExtra("article");
        if (article == null) {
            // fallback: create article from extras
            article = new Article(
                    getIntent().getStringExtra("title"),
                    getIntent().getStringExtra("description"),
                    getIntent().getStringExtra("content"),
                    getIntent().getStringExtra("imageUrl"),
                    getIntent().getStringExtra("url"),
                    getIntent().getStringExtra("publishedAt")
            );
        }

        displayArticle(article);

        // Load initial states
        loadOnlineLikeStatus();
        loadSavedStatus();

        // Like button
        btnDetailLike.setOnClickListener(v -> {
            if (isLiked) {
                isLiked = false;
                btnDetailLike.setImageResource(R.drawable.like);
                userViewModel.unlikeArticle(userId, article); // Firestore only
            } else {
                isLiked = true;
                btnDetailLike.setImageResource(R.drawable.liked);
                userViewModel.likeArticle(userId, article); // Firestore only
            }
        });

        // Bookmark button
        btnDetailBookmark.setOnClickListener(v -> {
            if (isSaved) {
                isSaved = false;
                btnDetailBookmark.setImageResource(R.drawable.save);

                // Xóa bài khỏi Room + Firestore
                userViewModel.unsaveArticle(userId, article);
            } else {
                isSaved = true;
                btnDetailBookmark.setImageResource(R.drawable.saved);

                // Lưu toàn bộ nội dung hiện tại của article
                String contentToSave = article.getContent();
                if (!isEmpty(txtDetailContent.getText().toString())) {
                    contentToSave = txtDetailContent.getText().toString();
                    article.setContent(contentToSave); // cập nhật nội dung đầy đủ
                }

                // Lưu bài vào Room + Firestore
                userViewModel.saveArticle(userId, article);
            }
        });


        btnBack.setOnClickListener(v -> finish());
    }

    private void displayArticle(Article article) {
        txtDetailTitle.setText(nz(article.getTitle()));
        txtDetailContent.setText(HtmlCompat.fromHtml(nz(stripTruncation(article.getContent())), HtmlCompat.FROM_HTML_MODE_LEGACY));
        txtUploadTime.setText(nz(article.getPublishedAt()));

        String imageUrl = article.getUrlToImage();
        if (!isEmpty(imageUrl)) {
            imgDetailNews.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUrl).into(imgDetailNews);
        } else {
            imgDetailNews.setVisibility(View.GONE);
        }

        if (containsTruncation(article.getContent()) && !isEmpty(article.getUrl())) {
            loadReaderMode(article.getUrl(), article);
        }
    }


    // --- cập nhật article với full content khi load xong ---
    private void loadReaderMode(String url, Article article) {
        scrollableContent.setVisibility(View.VISIBLE);
        txtDetailContent.setText("Loading full article…");

        runningTask = executor.submit(() -> {
            try {
                final String fullText = readerRepo.getReadableText(url);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    String displayText = isEmpty(fullText) ? "Couldn't load full article." : fullText;
                    txtDetailContent.setText(displayText);

                    // Cập nhật full content cho article để lưu
                    if (!isEmpty(fullText)) {
                        article.setContent(fullText);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    txtDetailContent.setText("Couldn't load full article.");
                });
            }
        });
    }

    // --- LOAD ONLINE LIKE STATUS ONLY ---
    private void loadOnlineLikeStatus() {
        if (userId == null) return;

        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<?> likedList = (List<?>) doc.get("likedArticles");
                        if (likedList != null) {
                            for (Object obj : likedList) {
                                if (obj instanceof Map) {
                                    Article a = gson.fromJson(gson.toJson(obj), Article.class);
                                    if (a.equals(article)) {
                                        isLiked = true;
                                        btnDetailLike.setImageResource(R.drawable.liked);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });
    }

    // --- LOAD SAVED STATUS (Room + Firestore) ---
    private void loadSavedStatus() {
        userViewModel.getUser().observe(this, user -> {
            if (user != null && user.getSavedArticles() != null && user.getSavedArticles().contains(article)) {
                isSaved = true;
                btnDetailBookmark.setImageResource(R.drawable.saved);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (runningTask != null) runningTask.cancel(true);
        executor.shutdownNow();
    }

    // --- HELPERS ---
    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    private boolean containsTruncation(String s) {
        if (s == null) return false;
        return s.matches("(?s).*\\[\\+\\d+\\s*chars\\]\\s*$") || s.matches("(?s).*\\[\\s*\\.\\.\\.\\s*\\]\\s*$");
    }

    private String stripTruncation(String s) {
        if (s == null) return "";
        return s.replaceAll("\\s*\\[\\+\\d+\\s*chars\\]\\s*$", "").replaceAll("\\s*\\[\\s*\\.\\.\\.\\s*\\]\\s*$", "");
    }
}
