package com.example.newsreaderapp.activities;

import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.newsreaderapp.R;
import com.example.newsreaderapp.viewmodel.UserViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    private UserViewModel viewModel;
    private EditText edtEmail, edtPassword;
    private TextView tvTitle;
    private Button btnContinue, btnGoogle;

    public boolean isAccount = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_page_layout);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ view
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnContinue = findViewById(R.id.btnContinue);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvTitle = findViewById(R.id.tvTitle);
        TextInputLayout passwordLayout = findViewById(R.id.passwordLayout);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        viewModel.currentUser.observe(this, user -> {
            if (user != null) {
                Toast.makeText(this, "Đăng nhập thành công: " + user.getUsername(), Toast.LENGTH_SHORT).show();
                // Chuyển sang MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        viewModel.errorMessage.observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý nút "Tiếp tục"
        btnContinue.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();

            if (email.isEmpty()) {
                edtEmail.setError("Please enter your email!");
                return;
            }
            if (!isValidEmail(email)){
                return;
            }

            // Khóa ô email
            edtEmail.setEnabled(false);
            edtEmail.setAlpha(0.6f);
            passwordLayout.setVisibility(VISIBLE);
            edtPassword.requestFocus();

            // Giả sử kiểm tra xem tài khoản có tồn tại
            viewModel.checkUserExists(email);

            viewModel.getCurrentUser().observe(this, user -> {
                if (user != null) {
                    // Đổi tiêu đề
                    tvTitle.setText("You already have an account.\nPlease login");
                    btnContinue.setText("Login");
                    edtPassword.setHint("Enter your password");
                    isAccount = true;
                }else{
                    isAccount = false;
                }
            });
            // nhấn nút lần 2
            btnContinue.setOnClickListener(v2 -> {
                String password2 = edtPassword.getText().toString().trim();

                if (password2.isEmpty()) {
                    Toast.makeText(this, "Please enter your password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isAccount) {
                    viewModel.login(email, password2);
                    return;
                }
                // Chưa có tài khoản
                if (!isAccount && isValidPassword(password2)){
                    viewModel.register(email, password2);
                    Toast.makeText(this, "Account created successfully!!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Xử lý nút "Đăng nhập bằng Google"
        btnGoogle.setOnClickListener(v -> {
            // TODO: gọi Google Sign-In API
            Toast.makeText(this, "Google login chưa được cài đặt", Toast.LENGTH_SHORT).show();
        });
    }
    // Check input
    private boolean isValidEmail(String email) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Invalid email!");
            return false;
        }
        return true;
    }
    private boolean isValidPassword(String password) {
        // Ít nhất 8 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$";
        if (!password.matches(passwordPattern)) {
            edtPassword.setError("Your password must have at least 8 characters, including the numbers, characters and special characters.");
            return false;
        }
        return true;
    }


}
