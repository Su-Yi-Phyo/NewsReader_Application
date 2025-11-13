package com.example.newsreaderapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newsreaderapp.R;
import com.example.newsreaderapp.models.Article;

import java.util.ArrayList;
import java.util.List;

public class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.NewsViewHolder> {

    private final List<Article> items = new ArrayList<>();
    private OnArticleActionListener listener;

    // 🔹 Loại adapter (HOME, BOOKMARKS, LIKED)
    public enum Mode { HOME, BOOKMARKS, LIKED }
    private final Mode mode;

    public ArticlesAdapter(Mode mode) {
        this.mode = mode;
    }

    public interface OnArticleActionListener {
        void onArticleClick(Article article);
        void onSaveClick(Article article);
        void onLikeClick(Article article);
        void onRemoveClick(Article article);
    }

    public void setOnArticleActionListener(OnArticleActionListener listener) {
        this.listener = listener;
    }

    public void updateList(List<Article> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_card_layout, parent, false);
        return new NewsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder h, int position) {
        Article a = items.get(position);

        h.txtTitle.setText(a.getTitle() != null ? a.getTitle() : "");
        h.txtDesc.setText(a.getDescription() != null ? a.getDescription() : "");
        h.txtTime.setText(a.getPublishedAt() != null ? a.getPublishedAt() : "");

        String imgUrl = a.getUrlToImage();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            h.img.setVisibility(View.VISIBLE);
            Glide.with(h.itemView.getContext()).load(imgUrl).into(h.img);
        } else {
            h.img.setVisibility(View.GONE);
        }

        // Xử lý click vào toàn bộ bài viết
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onArticleClick(a);
        });

        // 🔹 Cấu hình nút theo chế độ
        switch (mode) {
            case HOME:
                h.btnSave.setVisibility(View.VISIBLE);
                h.btnLike.setVisibility(View.VISIBLE);
                h.btnUnsave.setVisibility(View.GONE);
                break;
            case BOOKMARKS:
                h.btnSave.setVisibility(View.GONE);
                h.btnLike.setVisibility(View.GONE);
                h.btnUnsave.setVisibility(View.VISIBLE);
                h.btnUnsave.setOnClickListener(v -> {
                    if (listener != null) listener.onRemoveClick(a);
                });
                break;
            case LIKED:
                h.btnSave.setVisibility(View.GONE);
                h.btnLike.setVisibility(View.GONE);
                h.btnUnsave.setVisibility(View.VISIBLE);
                h.btnUnsave.setOnClickListener(v -> {
                    if (listener != null) listener.onRemoveClick(a);
                });
                break;
        }

        // HOME mode — thêm hành động like/save
        if (mode == Mode.HOME) {
            h.btnSave.setOnClickListener(v -> {
                if (listener != null) listener.onSaveClick(a);
            });
            h.btnLike.setOnClickListener(v -> {
                if (listener != null) listener.onLikeClick(a);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDesc, txtTime;
        ImageView img, btnUnsave, btnSave, btnLike;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtNewsTitle);
            txtDesc  = itemView.findViewById(R.id.txtNewsDescription);
            txtTime  = itemView.findViewById(R.id.txtNewsTime);
            img      = itemView.findViewById(R.id.imgNews);
            btnUnsave  = itemView.findViewById(R.id.btnDelete);
            btnLike  = itemView.findViewById(R.id.btnLike);
            btnSave  = itemView.findViewById(R.id.btnBookmark);
        }
    }
}
