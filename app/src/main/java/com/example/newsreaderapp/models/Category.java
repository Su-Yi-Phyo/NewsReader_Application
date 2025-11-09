package com.example.newsreaderapp.models;

public class Category {

    private String categoryname;
    private int imageResource;

    public Category(String categoryname, int imageResource) {
        this.categoryname = categoryname;
        this.imageResource = imageResource;
    }

    public String getcategoryname() {
        return categoryname;
    }

    public int getImageResource() {
        return imageResource;
    }
}
