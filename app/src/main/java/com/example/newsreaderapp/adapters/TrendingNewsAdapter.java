package com.example.newsreaderapp.adapters;

import android.content.Context;
import android.content.Intent;
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

public class TrendingNewsAdapter extends RecyclerView.Adapter<TrendingNewsAdapter.VH> {

    private final List<Article> items = new ArrayList<>();

    public void setItems(List<Article> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    public void appendItems(List<Article> more) {
        if (more == null || more.isEmpty()) return;
        int start = items.size();
        items.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trending_news_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Article a = items.get(position);

        h.title.setText(a.getTitle() != null ? a.getTitle() : "");
        h.desc.setText(a.getDescription() != null ? a.getDescription() : "");
        h.time.setText(a.getPublishedAt() != null ? a.getPublishedAt() : "");

        String imgUrl = a.getUrlToImage();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            h.image.setVisibility(View.VISIBLE);
            Glide.with(h.itemView.getContext()).load(imgUrl).into(h.image);
        } else {
            h.image.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> {
            Context ctx = v.getContext();
            Intent it = new Intent(ctx, com.example.newsreaderapp.activities.NewsDetailActivity.class);
            it.putExtra("title", a.getTitle());
            it.putExtra("description", a.getDescription());
            it.putExtra("content", a.getContent());
            it.putExtra("imageUrl", a.getUrlToImage());
            it.putExtra("publishedAt", a.getPublishedAt());
            ctx.startActivity(it);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, desc, time;

        VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imgTrendingNews);
            title = itemView.findViewById(R.id.txtTrendingNewsTitle);
            desc  = itemView.findViewById(R.id.txtTrendingNewsDescription);
            time  = itemView.findViewById(R.id.txtTrendingNewsTime);
        }
    }
}