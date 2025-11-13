package com.example.newsreaderapp.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.newsreaderapp.models.Article;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertUser(UserEntity user);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(UserEntity user);
    @Query("UPDATE users SET savedArticles = :saved WHERE id = :id")
    void updateSavedArticles(String id, List<Article> saved);
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    UserEntity login(String email, String password);
    // Kiểm tra user đã tồn tại theo email/username
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    UserEntity getUserById(String userId);
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    LiveData<UserEntity> getUserLiveData(String id);
    @Query("DELETE FROM users WHERE id = :id")
    void deleteUserById(String id);
}
