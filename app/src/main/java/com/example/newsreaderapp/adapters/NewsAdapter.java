package com.example.newsreaderapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
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

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    //variable declaration

    private final List<Article> items = new ArrayList<>();

    public void setItems(List<Article> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_card_layout, parent, false);
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
            Glide.with(h.itemView.getContext())
                    .load(imgUrl)
                    .into(h.img);
        } else {
            h.img.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> {
            Context ctx = v.getContext();
            Intent it = new Intent(ctx, com.example.newsreaderapp.activities.NewsDetailActivity.class);
            it.putExtra("title", a.getTitle());
            it.putExtra("description", a.getDescription());
            it.putExtra("content", a.getContent());
            it.putExtra("imageUrl", a.getUrlToImage());
            it.putExtra("publishedAt", a.getPublishedAt());
            it.putExtra("url", a.getUrl());
            ctx.startActivity(it);
        });

        //xu ly btn like click
        h.btnLike.setOnClickListener(v -> {

        });

        h.btnSave.setOnClickListener(v -> {

        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDesc, txtTime;
        ImageView img;
        ImageView btnSave, btnLike;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtNewsTitle);
            txtDesc  = itemView.findViewById(R.id.txtNewsDescription);
            txtTime  = itemView.findViewById(R.id.txtNewsTime);
            img      = itemView.findViewById(R.id.imgNews);
            btnLike = itemView.findViewById(R.id.btnDetailLike);
            btnSave = itemView.findViewById(R.id.btnDetailBookmark);
        }
    }

    public void appendItems(List<Article> more) {
        if (more == null || more.isEmpty()) return;
        int start = items.size();
        items.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

}