package com.example.newsreaderapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.bumptech.glide.Glide;
import com.example.newsreaderapp.R;
import com.example.newsreaderapp.repository.ReaderModeRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NewsDetailActivity extends AppCompatActivity {

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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

        // If truncated and we have a URL, fetch full readable HTML in background and render it inside the app.
        if (truncated && !isEmpty(url)) {
            loadReaderMode(url);
        }

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadReaderMode(String url) {
        // hide current scroll content; we'll show a WebView with cleaned article HTML
        scrollableContent.setVisibility(View.GONE);

        View rootView = (View) topActionBar.getParent();
        if (!(rootView instanceof ConstraintLayout)) return;
        ConstraintLayout root = (ConstraintLayout) rootView;

        WebView webView = new WebView(this);
        webView.setId(View.generateViewId());
        webView.getSettings().setJavaScriptEnabled(false); // not needed for static HTML
        webView.setWebViewClient(new WebViewClient());     // keep navigation inside

        ConstraintLayout.LayoutParams lp =
                new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                        ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
        lp.topToBottom = R.id.topActionBar;
        lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        root.addView(webView, lp);

        // Background fetch of cleaned HTML
        runningTask = executor.submit(() -> {
            try {
                String readable = readerRepo.getReadableHtml(url);
                runOnUiThread(() ->
                        webView.loadDataWithBaseURL(
                                url, readable, "text/html", "UTF-8", null
                        )
                );
            } catch (Exception e) {
                // fallback: if parsing fails, just load original URL in WebView
                runOnUiThread(() -> webView.loadUrl(url));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (runningTask != null) runningTask.cancel(true);
        executor.shutdownNow();
    }

    // --- helpers ---
    private boolean isEmpty(String s) { return s == null || s.trim().isEmpty(); }
    private String nz(String s) { return s == null ? "" : s; }
    private boolean containsTruncation(String s) {
        if (s == null) return false;
        return s.matches("(?s).*\\[\\+\\d+\\s*chars\\]\\s*$") || s.matches("(?s).*\\[\\s*\\.\\.\\.\\s*\\]\\s*$");
    }
    private String stripTruncation(String s) {
        if (s == null) return "";
        s = s.replaceAll("\\s*\\[\\+\\d+\\s*chars\\]\\s*$", "");
        s = s.replaceAll("\\s*\\[\\s*\\.\\.\\.\\s*\\]\\s*$", "");
        return s;
    }
}