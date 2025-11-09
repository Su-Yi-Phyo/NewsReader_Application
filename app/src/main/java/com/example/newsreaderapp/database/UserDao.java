package com.example.newsreaderapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertUser(UserEntity user);

    @Query("SELECT * FROM users WHERE username = :email AND password = :password LIMIT 1")
    UserEntity login(String email, String password);
    // Kiểm tra user đã tồn tại theo email/username
    @Query("SELECT * FROM users WHERE username = :email LIMIT 1")
    UserEntity getUserByEmail(String email);
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    UserEntity getUserById(int userId);

    @Delete
    void deleteUser(UserEntity user);
}
