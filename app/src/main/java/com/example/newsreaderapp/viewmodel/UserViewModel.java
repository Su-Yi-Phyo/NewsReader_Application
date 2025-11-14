package com.example.newsreaderapp.viewmodel;

import android.app.Application;
import android.app.ComponentCaller;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.newsreaderapp.database.AppDatabase;
import com.example.newsreaderapp.database.UserEntity;
import com.example.newsreaderapp.models.Article;
import com.example.newsreaderapp.repository.UserRepository;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class UserViewModel extends AndroidViewModel {
    public final UserRepository repo;
    private MutableLiveData<UserEntity> currentUser = new MutableLiveData<>();

    public MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private LiveData<UserEntity> userLiveData;

    public LiveData<UserEntity> getUser() {
        return userLiveData;
    }
    public UserViewModel(@NonNull Application app) {
        super(app);
        repo = new UserRepository(
                AppDatabase.getInstance(app).userDao(),
                FirebaseFirestore.getInstance()
        );
    }
    public UserViewModel(@NonNull Application app, UserRepository repo, String userId) {
        super(app);
        this.repo = repo;
        this.userLiveData = repo.getUser(userId);
    }
    public LiveData<UserEntity> getUserById(String id) {
        return repo.getUserById(id);
    }

    public LiveData<UserEntity> getCurrentUser() {
        return currentUser;  // Trả về LiveData để UI observe
    }

    public void login(String email, String password) {
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        UserEntity user = query.getDocuments().get(0).toObject(UserEntity.class);
                        if (user != null) repo.saveUser(user);
                    } else errorMessage.postValue("Wrong credentials");
                });
    }

  // check email tồn tại bằng Firebase (đúng chuẩn)
//    public void checkFirebaseEmailExists(String email, OnResult callback) {
//        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
//                .addOnSuccessListener(result ->
//                        callback.onResult(result.getSignInMethods().size() > 0)
//                );
//    }
  public void checkFirebaseEmailExists(String email, Consumer<Boolean> callback) {
      if (email == null || email.trim().isEmpty()) {
          callback.accept(false);
          return;
      }

      String normalizedEmail = email.trim().toLowerCase();

      FirebaseAuth.getInstance().fetchSignInMethodsForEmail(normalizedEmail)
              .addOnCompleteListener(task -> {
                  if (task.isSuccessful()) {
                      SignInMethodQueryResult result = task.getResult();
                      boolean exists = result != null
                              && result.getSignInMethods() != null
                              && !result.getSignInMethods().isEmpty();

                      Log.d("FIREBASE_CHECK", "Email: " + normalizedEmail + " → exists: " + exists);
                      callback.accept(exists);
                  } else {
                      Exception e = task.getException();
                      Log.e("FIREBASE_CHECK", "Lỗi kiểm tra email", e);

                      // Xử lý lỗi mạng
                      if (e instanceof FirebaseNetworkException) {
                          new Handler(Looper.getMainLooper()).post(() ->
                                  Toast.makeText(getApplication(), "Không có mạng", Toast.LENGTH_SHORT).show()
                          );
                      }
                      callback.accept(false);
                  }
              });

  }

    // khi user login vào Firebase → load vào Room hoặc tạo mới
    public void loadOrCreateUser(FirebaseUser fUser) {
        Executors.newSingleThreadExecutor().execute(() -> {
            UserEntity user = repo.getUserByEmail(fUser.getEmail());

            if (user == null) {
                user = new UserEntity(
                        fUser.getUid(),
                        fUser.getEmail(),
                        null,
                        fUser.getDisplayName(),
                        fUser.getProviderId().equals("google.com") ? "google" : "email"
                );
                repo.saveUser(user);
            }

            currentUser.postValue(user);
        });
    }

    // lúc REGISTER email → tạo UserEntity đầy đủ (có password)
    public void createUserIfNotExists(FirebaseUser fUser, String password) {
        Executors.newSingleThreadExecutor().execute(() -> {
            UserEntity user = new UserEntity(
                    fUser.getUid(),
                    fUser.getEmail(),
                    password,
                    null,
                    "email"
            );

            repo.saveUser(user);
            currentUser.postValue(user);
        });
    }

    public interface OnResult {
        void onResult(boolean exists);
    }
    public void deleteUserById(String id) {
        repo.deleteUserById(id);
    }

    public void likeArticle(String userId, Article article) { repo.likeArticle(userId, article); }
    public void unlikeArticle(String userId, Article article) { repo.unlikeArticle(userId, article); }
    public void saveArticle(String userId, Article article) { repo.saveArticle(userId, article); }
    public void unsaveArticle(String userId, Article article) { repo.unsaveArticle(userId, article); }

    public static class Factory implements ViewModelProvider.Factory {
        private final UserRepository repository;
        private final String userId;
        private final Application app;

        public Factory(Application app, UserRepository repository, String userId) {
            this.app = app;
            this.repository = repository;
            this.userId = userId;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(UserViewModel.class)) {
                return (T) new UserViewModel(app, repository, userId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

}
