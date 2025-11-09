package com.example.newsreaderapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertUser(UserEntity user);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    UserEntity login(String username, String password);
    // Kiểm tra user đã tồn tại theo email/username
    @Query("SELECT * FROM users WHERE username = :email LIMIT 1")
    UserEntity getUserByEmail(String email);
}
