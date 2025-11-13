package com.example.newsreaderapp.models;

import com.google.gson.annotations.SerializedName;

public class Article {

    @SerializedName("source")
    private Source source;

    @SerializedName("author")
    private String author;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("url")
    private String url;

    @SerializedName("urlToImage")
    private String urlToImage;

    @SerializedName("publishedAt")
    private String publishedAt;

    @SerializedName("content")
    private String content;

    // Getters
    public Source getSource() { return source; }
    public String getAuthor() { return author; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getUrl() { return url; }
    public String getUrlToImage() { return urlToImage; }
    public String getPublishedAt() { return publishedAt; }
    public String getContent() { return content; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Article)) return false;
        Article a = (Article) o;
        return url != null && url.equals(a.getUrl()); // so sánh bằng URL duy nhất
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }

}