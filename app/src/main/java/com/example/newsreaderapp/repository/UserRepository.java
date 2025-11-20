package com.example.newsreaderapp.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.newsreaderapp.database.AppDatabase;
import com.example.newsreaderapp.database.UserDao;
import com.example.newsreaderapp.database.UserEntity;
import com.example.newsreaderapp.models.Article;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class UserRepository {
    private final UserDao userDao; // Room DAO
    private final ExecutorService executor;
    private final FirebaseFirestore firestore;
    private final MutableLiveData<UserEntity> currentUser = new MutableLiveData<>();

    public UserRepository(UserDao userDao, FirebaseFirestore firestore) {
        this.userDao = userDao;
        this.firestore = firestore;
        this.executor = AppDatabase.databaseWriteExecutor;
    }
    public LiveData<UserEntity> getUserById(String id) {
        return userDao.getUserLiveData(id);
    }
    public UserEntity getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }
    public void deleteUserById(String id) {
        executor.execute(() -> userDao.deleteUserById(id));
    }

    public LiveData<UserEntity> getCurrentUser() {
        return currentUser;  // return LiveData to UI observe
    }
    // LiveData user to Activity/ViewModel observe
    public LiveData<UserEntity> getUser(String userId) {
        // Fetch data from Room
        LiveData<UserEntity> liveData = userDao.getUserLiveData(userId);

        // synchronize Firestore when online
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    UserEntity user = doc.toObject(UserEntity.class);
                    if (user != null) {
                        new Thread(() -> userDao.insertOrUpdate(user)).start();
                    }
                });

        return liveData;
    }
    // save user (register or update profile)
    public void saveUser(UserEntity user) {
        new Thread(() -> userDao.insertOrUpdate(user)).start();
        firestore.collection("users").document(user.getId()).set(user);
    }


    public void fetchUserFromFirestore(String userId) {
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    UserEntity user = doc.toObject(UserEntity.class);
                    if (user != null) {
                        new Thread(() -> userDao.insertOrUpdate(user)).start();
                        currentUser.postValue(user);
                    }
                });
    }

    // Save news offline + Firestore for multi-device
    public void saveArticle(String userId, Article article) {
        // Room
        new Thread(() -> {
            UserEntity user = userDao.getUserLiveData(userId).getValue();
            if (user != null) {
                List<Article> saved = new ArrayList<>(user.getSavedArticles());
                if (!saved.contains(article)) {
                    saved.add(article);
                    user.setSavedArticles(saved);
                    userDao.insertOrUpdate(user);
                }
            }
        }).start();

        // Firestore
        firestore.collection("users").document(userId)
                .update("savedArticles", FieldValue.arrayUnion(article))
                .addOnFailureListener(e -> Log.e("Firestore", "Save failed", e));
    }

    public void unsaveArticle(String userId, Article article) {
        new Thread(() -> {
            UserEntity user = userDao.getUserLiveData(userId).getValue();
            if (user != null) {
                List<Article> saved = new ArrayList<>(user.getSavedArticles());
                saved.remove(article);
                user.setSavedArticles(saved);
                userDao.insertOrUpdate(user);
            }
        }).start();

        firestore.collection("users").document(userId)
                .update("savedArticles", FieldValue.arrayRemove(article))
                .addOnFailureListener(e -> Log.e("Firestore", "Unsave failed", e));
    }
    public void clearAllSavedArticles(String userId) {
        // Room
        new Thread(() -> {
            UserEntity user = userDao.getUserById(userId);
            if (user != null) {
                user.setSavedArticles(new ArrayList<>());
                userDao.insertOrUpdate(user);
            }
        }).start();

        // Firestore
        firestore.collection("users")
                .document(userId)
                .update("savedArticles", new ArrayList<>());
    }


    // Like bài (online only)
    public void likeArticle(String userId, Article article) {
        firestore.collection("users").document(userId)
                .update("likedArticles", FieldValue.arrayUnion(article));
    }

    public void unlikeArticle(String userId, Article article) {
        firestore.collection("users").document(userId)
                .update("likedArticles", FieldValue.arrayRemove(article));
    }

    //get list liked news (online)
    public void fetchLikedArticles(String userId, OnCompleteListener<List<Article>> listener) {
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    List<Article> liked = (List<Article>) doc.get("likedArticles");
                    listener.onComplete(liked != null ? liked : new ArrayList<>());
                });
    }

    public interface OnCompleteListener<T> {
        void onComplete(T data);
    }
}
