package com.example.newsreaderapp.activities;

import static android.view.View.VISIBLE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
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
import com.example.newsreaderapp.database.UserEntity;
import com.example.newsreaderapp.repository.UserRepository;
import com.example.newsreaderapp.viewmodel.UserViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 101;
    private EditText edtEmail, edtPassword;
    private TextView tvTitle;
    private Button btnContinue, btnGoogle;
    private TextInputLayout passwordLayout;

    private boolean isAccount = false;
    private FirebaseAuth mAuth;

    private UserViewModel viewModel;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page_layout);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        passwordLayout = findViewById(R.id.passwordLayout);
        tvTitle = findViewById(R.id.tvTitle);
        btnContinue = findViewById(R.id.btnContinue);
        btnGoogle = findViewById(R.id.btnGoogle);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        mAuth = FirebaseAuth.getInstance();
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Khi user được tải từ Room → chuyển trang
        viewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                prefs.edit().putString("user_id", user.getId()).apply();

                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        // 1) User nhập email trước
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

            // Kiểm tra user có trong Firebase Auth chưa
            viewModel.checkFirebaseEmailExists(email, exists -> {
                if (exists) {
                    // Tài khoản đã tồn tại → login
                    isAccount = true;
                    tvTitle.setText("You already have an account.\nPlease login");
                    btnContinue.setText("Login");
                    edtPassword.setHint("Enter your password");
                } else {
                    // Tài khoản chưa có → register
                    isAccount = false;
                    tvTitle.setText("Create your account");
                    btnContinue.setText("Register");
                    edtPassword.setHint("Create your password");
                }
            });

            // 2) Nhấn nút lần thứ hai
            btnContinue.setOnClickListener(v2 -> {
                String password = edtPassword.getText().toString().trim();
                if (password.isEmpty()) {
                    Toast.makeText(this, "Please enter your password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isAccount) {
                    loginFirebase(email, password);
                } else if (isValidPassword(password)){
                    registerFirebase(email, password);
                }
            });
        });

        // Google Login
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogle.setOnClickListener(v -> startActivityForResult(
                googleSignInClient.getSignInIntent(), RC_SIGN_IN
        ));
    }

    // ========== LOGIN EMAIL/PASSWORD ==========
    private void loginFirebase(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(auth -> {
                    FirebaseUser fUser = auth.getUser();

                    // tải user từ ROOM (hoặc tạo mới nếu lần đầu login)
                    viewModel.loadOrCreateUser(fUser);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ========== REGISTER EMAIL/PASSWORD ==========
    private void registerFirebase(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(auth -> {
                    FirebaseUser fUser = auth.getUser();

                    // tạo user trong Room
                    viewModel.createUserIfNotExists(fUser, password);

                    Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Register failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ========== GOOGLE LOGIN ==========
    @Override
    public void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);

        if (req == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                firebaseGoogleLogin(account.getIdToken());
            } catch (Exception e) {
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseGoogleLogin(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(auth -> {
                    FirebaseUser fUser = auth.getUser();
                    viewModel.loadOrCreateUser(fUser); // lưu vào room
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Google Login failed", Toast.LENGTH_SHORT).show()
                );
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