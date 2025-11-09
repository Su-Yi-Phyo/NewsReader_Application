package com.example.newsreaderapp.repository;

import com.example.newsreaderapp.database.AppDatabase;
import com.example.newsreaderapp.database.UserDao;
import com.example.newsreaderapp.database.UserEntity;

import java.util.concurrent.ExecutorService;

public class UserRepository {

    private final UserDao userDao;
    private final ExecutorService executorService;

    public UserRepository(AppDatabase db) {
        this.userDao = db.userDao();
        this.executorService = AppDatabase.databaseWriteExecutor;
    }

    public void register(UserEntity user, Callback callback) {
        executorService.execute(() -> {
            try {
                userDao.insertUser(user);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void login(String email, String password, LoginCallback callback) {
        executorService.execute(() -> {
            UserEntity user = userDao.login(email, password);
            if (user != null) {
                callback.onSuccess(user);
            } else {
                callback.onError("Wrong password");
            }
        });
    }

    // Callback interfaces
    public interface Callback {
        void onSuccess();
        void onError(String message);
    }
    public void registerGoogleUser(UserEntity user, Callback callback) {
        executorService.execute(() -> {
            try {
                userDao.insertUser(user);  // lưu user mới vào Room
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }


    public interface LoginCallback {
        void onSuccess(UserEntity user);
        void onError(String message);
    }
    public void checkUserExists(String email, UserExistCallback callback) {
        executorService.execute(() -> {
            UserEntity user = userDao.getUserByEmail(email);
            if (user != null) {
                callback.onExist(user);   // User đã tồn tại
            } else {
                callback.onNotExist();    // User chưa tồn tại
            }
        });
    }

    // Callback interface
    public interface UserExistCallback {
        void onExist(UserEntity user);
        void onNotExist();
    }
    public void getUserById(int userId, LoadUserCallback callback) {
        executorService.execute(() -> {
            UserEntity user = userDao.getUserById(userId); // cần tạo query trong DAO
            if (user != null) {
                callback.onSuccess(user);
            } else {
                callback.onError("User không tồn tại");
            }
        });
    }
    public void deleteAccount(UserEntity user, Callback callback) {
        executorService.execute(() -> {
            try {
                userDao.deleteUser(user);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public interface LoadUserCallback {
        void onSuccess(UserEntity user);
        void onError(String message);
    }


}
