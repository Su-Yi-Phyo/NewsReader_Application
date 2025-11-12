package com.example.newsreaderapp.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.newsreaderapp.helper.Converters;
import com.example.newsreaderapp.models.Article;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "users")
public class UserEntity {

    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String email;

    private String password; // Có thể null nếu là Google

    private String displayName;

    private String authType; // "email" hoặc "google"

    @TypeConverters(Converters.class)
    private List<Article> savedArticles = new ArrayList<>();

    public UserEntity(@NonNull String id, @NonNull String email, String password,
                      String displayName, String authType) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.displayName = displayName;
        this.authType = authType;
    }

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String username) { this.email = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<Article> getSavedArticles() { return savedArticles; }
    public void setSavedArticles(List<Article> savedArticles) { this.savedArticles = savedArticles; }

    public String getAuthType() {return authType;}
    public String getDisplayName() {return displayName;}

    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setAuthType(String authType) { this.authType = authType; }

}