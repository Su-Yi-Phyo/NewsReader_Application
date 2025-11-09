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

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private final List<Article> items = new ArrayList<>();

    public void setItems(List<Article> data) {
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
            Glide.with(h.itemView.getContext())
                    .load(imgUrl)
                    .into(h.img);
        } else {
            h.img.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDesc, txtTime;
        ImageView img;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtNewsTitle);
            txtDesc  = itemView.findViewById(R.id.txtNewsDescription);
            txtTime  = itemView.findViewById(R.id.txtNewsTime);
            img      = itemView.findViewById(R.id.imgNews);
        }
    }
}