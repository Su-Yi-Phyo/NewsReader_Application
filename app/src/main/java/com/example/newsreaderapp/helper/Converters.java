package com.example.newsreaderapp.helper;

import androidx.room.TypeConverter;

import com.example.newsreaderapp.models.Article;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Converters {
    @TypeConverter
    public static String fromArticleList(List<Article> articles) {
        return new Gson().toJson(articles);
    }

    @TypeConverter
    public static List<Article> toArticleList(String data) {
        if (data == null || data.isEmpty()) return new ArrayList<>();
        Type listType = new TypeToken<List<Article>>() {}.getType();
        return new Gson().fromJson(data, listType);
    }
}
