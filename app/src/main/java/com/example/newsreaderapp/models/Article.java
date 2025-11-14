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

    // ---- REQUIRED FOR FIRESTORE / ROOM ----
    public Article() {}

    // ---- MANUAL CONSTRUCTOR FOR DETAIL ACTIVITY ----
    public Article(String title, String description, String content,
                   String urlToImage, String url, String publishedAt) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.urlToImage = urlToImage;
        this.url = url;
        this.publishedAt = publishedAt;
        this.source = null;
        this.author = null;
    }

    // ---- GETTERS & SETTERS ----
    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getUrlToImage() { return urlToImage; }
    public void setUrlToImage(String urlToImage) { this.urlToImage = urlToImage; }

    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    // ---- COMPARE BY URL (UNIQUE) ----
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Article)) return false;
        Article a = (Article) o;
        return url != null && url.equals(a.getUrl());
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }
}
