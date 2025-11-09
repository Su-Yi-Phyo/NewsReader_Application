package com.example.newsreaderapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.newsreaderapp.database.AppDatabase;
import com.example.newsreaderapp.database.UserEntity;
import com.example.newsreaderapp.repository.UserRepository;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository repository;
    public MutableLiveData<UserEntity> currentUser = new MutableLiveData<>();
    public MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(AppDatabase.getInstance(application));
    }
    public LiveData<UserEntity> getCurrentUser() {
        return currentUser;  // Trả về LiveData để UI observe
    }
    public void register(String username, String password) {
        // Gọi Repository để insert user mới
        repository.register(new UserEntity(username, password), new UserRepository.Callback() {
            @Override
            public void onSuccess() {
                // Khi đăng ký thành công, tự động login
                login(username, password); // Gọi method login trong ViewModel
            }
            @Override
            public void onError(String message) {
                // Nếu có lỗi khi insert DB (ví dụ username/email đã tồn tại)
                errorMessage.postValue(message);
            }
        });
    }


    public void login(String username, String password) {
        repository.login(username, password, new UserRepository.LoginCallback() {
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
                currentUser.postValue(user);  // User đã tồn tại → chuyển sang login
            }

            @Override
            public void onNotExist() {
                currentUser.postValue(null);  // User chưa tồn tại → hiển thị register
            }
        });
    }
    public void logout() {
        // Xóa user hiện tại khỏi LiveData
        currentUser.postValue(null);
    }

}
