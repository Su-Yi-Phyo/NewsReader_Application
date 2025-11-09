package com.example.newsreaderapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.newsreaderapp.database.AppDatabase;
import com.example.newsreaderapp.database.UserEntity;
import com.example.newsreaderapp.repository.UserRepository;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository repository;
    public MutableLiveData<UserEntity> currentUser = new MutableLiveData<>();
    public MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUserExists = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(AppDatabase.getInstance(application));
    }
    public LiveData<Boolean> getIsUserExists() {
        return isUserExists;
    }
    public LiveData<UserEntity> getCurrentUser() {
        return currentUser;  // Trả về LiveData để UI observe
    }
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public void register(String email, String password) {
        String username = email.split("@")[0];
        // Gọi Repository để insert user mới
        repository.register(new UserEntity(email, password), new UserRepository.Callback() {
            @Override
            public void onSuccess() {
                // Khi đăng ký thành công, tự động login
                login(email, password); // Gọi method login trong ViewModel
            }
            @Override
            public void onError(String message) {
                // Nếu có lỗi khi insert DB (ví dụ username/email đã tồn tại)
                errorMessage.postValue(message);
            }
        });
    }


    public void login(String email, String password) {
        repository.login(email, password, new UserRepository.LoginCallback() {
            @Override
            public void onSuccess(UserEntity user) {
                currentUser.postValue(user);
            }

            @Override
            public void onError(String message) {
                errorMessage.postValue(message);
            }
        });
    }
    public void checkUserExists(String email) {
        repository.checkUserExists(email, new UserRepository.UserExistCallback() {
            @Override
            public void onExist(UserEntity user) {
                isUserExists.postValue(true);  // User đã tồn tại → chuyển sang login
            }

            @Override
            public void onNotExist() {
                isUserExists.postValue(false);  // User chưa tồn tại → hiển thị register
            }
        });
    }
    public void logout() {
        // Xóa user hiện tại khỏi LiveData
        currentUser.postValue(null);
    }
    public void loadUserById(int userId) {
        repository.getUserById(userId, new UserRepository.LoadUserCallback() {
            @Override
            public void onSuccess(UserEntity user) {
                currentUser.postValue(user);
            }

            @Override
            public void onError(String message) {
                errorMessage.postValue(message);
            }
        });
    }
    public void deleteAccount() {
        UserEntity user = currentUser.getValue();
        if (user != null) {
            repository.deleteAccount(user, new UserRepository.Callback() {
                @Override
                public void onSuccess() {
                    // Xóa user khỏi LiveData và SharedPreferences
                    currentUser.postValue(null);
                }

                @Override
                public void onError(String message) {
                    errorMessage.postValue(message);
                }
            });
        }
    }
    public void handleGoogleSignIn(GoogleSignInAccount account) {
        String email = account.getEmail();
        String username = account.getDisplayName();

        // Kiểm tra user đã có trong DB chưa
        repository.checkUserExists(email, new UserRepository.UserExistCallback() {
            @Override
            public void onExist(UserEntity user) {
                // User đã có → login
                currentUser.postValue(user);
            }

            @Override
            public void onNotExist() {
                // User chưa có → tạo mới
                repository.registerGoogleUser(new UserEntity(username, email), new UserRepository.Callback() {
                    @Override
                    public void onSuccess() {
                        repository.checkUserExists(email, new UserRepository.UserExistCallback() {
                            @Override
                            public void onExist(UserEntity user) {
                                currentUser.postValue(user);
                            }
                            @Override
                            public void onNotExist() {
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        errorMessage.postValue(message);
                    }
                });
            }
        });
    }

}
