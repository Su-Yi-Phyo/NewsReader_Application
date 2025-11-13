package com.example.newsreaderapp.activities;

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

import com.bumptech.glide.Glide;
import com.example.newsreaderapp.R;
import com.example.newsreaderapp.repository.ReaderModeRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NewsDetailActivity extends AppCompatActivity {

    private static final String TAG = "NewsDetailActivity";

    private ImageView btnBack, imgDetailNews;
    private TextView txtDetailTitle, txtDetailContent, txtUploadTime, txtDetailLikeCount;
    private View topActionBar, scrollableContent;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> runningTask;
    private final ReaderModeRepository readerRepo = new ReaderModeRepository();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_detail_layout);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        btnBack = findViewById(R.id.btnBack);
        imgDetailNews = findViewById(R.id.imgDetailNews);
        txtDetailTitle = findViewById(R.id.txtDetailTitle);
        txtDetailContent = findViewById(R.id.txtDetailContent);
        txtUploadTime = findViewById(R.id.txtUploadTime);
        txtDetailLikeCount = findViewById(R.id.txtDetailLikeCount);
        topActionBar = findViewById(R.id.topActionBar);
        scrollableContent = findViewById(R.id.scrollableContent);

        String title       = getIntent().getStringExtra("title");
        String content     = getIntent().getStringExtra("content");
        String description = getIntent().getStringExtra("description");
        String imageUrl    = getIntent().getStringExtra("imageUrl");
        String publishedAt = getIntent().getStringExtra("publishedAt");
        String url         = getIntent().getStringExtra("url");

        Log.d(TAG, "Article URL: " + url);

        String preferred = !isEmpty(content) ? content : (!isEmpty(description) ? description : "");
        boolean truncated = containsTruncation(preferred);
        String body = stripTruncation(preferred);

        txtDetailTitle.setText(nz(title));
        txtDetailContent.setText(HtmlCompat.fromHtml(nz(body), HtmlCompat.FROM_HTML_MODE_LEGACY));
        txtUploadTime.setText(nz(publishedAt));
        txtDetailLikeCount.setText("");

        if (!isEmpty(imageUrl)) {
            imgDetailNews.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUrl).into(imgDetailNews);
        } else {
            imgDetailNews.setVisibility(View.GONE);
        }

        // If truncated and we have a URL, fetch full readable content
        if (truncated && !isEmpty(url)) {
            Log.d(TAG, "Content is truncated, loading full article...");
            loadReaderMode(url);
        } else {
            Log.d(TAG, "Content not truncated or no URL, showing preview");
        }

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadReaderMode(String url) {
        scrollableContent.setVisibility(View.VISIBLE);
        txtDetailContent.setText("Loading full article…");

        runningTask = executor.submit(() -> {
            try {
                final String fullText = readerRepo.getReadableText(url);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    txtDetailContent.setText(fullText == null || fullText.trim().isEmpty()
                            ? "Couldn't load full article."
                            : fullText);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    txtDetailContent.setText("Couldn't load full article.");
                });
            }
        });

    }

    private Element findArticleContent(Document document) {
        // Priority list of selectors
        String[] selectors = {
                "article[role=main]",
                "main article",
                "article",
                "[role=main]",
                "main",
                ".article-content",
                ".article-body",
                ".post-content",
                ".entry-content",
                ".story-body",
                ".article-text",
                "div[itemprop=articleBody]",
                ".content-body",
                "#article-body",
                "#main-content",
                "[class*=article-content]",
                "[class*=post-content]"
        };

        for (String selector : selectors) {
            Elements elements = document.select(selector);
            if (!elements.isEmpty()) {
                Element candidate = elements.first();
                // Check if it has substantial content
                if (candidate.text().length() > 200) {
                    Log.d(TAG, "Found content with selector: " + selector);
                    return candidate;
                }
            }
        }

        Log.w(TAG, "No suitable article container found");
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (runningTask != null) {
            runningTask.cancel(true);
        }
        executor.shutdownNow();
    }

    // --- helpers ---
    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    private boolean containsTruncation(String s) {
        if (s == null) return false;
        return s.matches("(?s).*\\[\\+\\d+\\s*chars\\]\\s*$") ||
                s.matches("(?s).*\\[\\s*\\.\\.\\.\\s*\\]\\s*$");
    }

    private String stripTruncation(String s) {
        if (s == null) return "";
        s = s.replaceAll("\\s*\\[\\+\\d+\\s*chars\\]\\s*$", "");
        s = s.replaceAll("\\s*\\[\\s*\\.\\.\\.\\s*\\]\\s*$", "");
        return s;
    }
}