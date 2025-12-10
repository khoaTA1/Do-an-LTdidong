package vn.ltdidong.apphoctienganh.activities;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import vn.ltdidong.apphoctienganh.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout textInputEmail;
    private TextInputEditText editTextEmail;
    private Button buttonResetPassword;
    private ProgressBar progressBar;
    private TextView textBackToLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        textInputEmail = findViewById(R.id.text_input_email);
        buttonResetPassword = findViewById(R.id.button_reset_password);
        progressBar = findViewById(R.id.progress_bar);
        textBackToLogin = findViewById(R.id.text_back_to_login);

        if (textInputEmail.getEditText() != null) {
            editTextEmail = (TextInputEditText) textInputEmail.getEditText();
        }

        // Reset Password button click
        buttonResetPassword.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            
            textInputEmail.setError(null);
            
            if (validateEmail(email)) {
                sendPasswordResetEmail(email);
            }
        });

        // Back to Login click
        textBackToLogin.setOnClickListener(v -> finish());
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            textInputEmail.setError("Email không được để trống");
            return false;
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputEmail.setError("Email không hợp lệ");
            return false;
        }
        
        return true;
    }

    private void sendPasswordResetEmail(String email) {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        buttonResetPassword.setEnabled(false);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    buttonResetPassword.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Email đặt lại mật khẩu đã được gửi! Vui lòng kiểm tra hộp thư của bạn.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String errorMessage = "Không thể gửi email. ";
                        
                        if (task.getException() != null) {
                            String exceptionMessage = task.getException().getMessage();
                            if (exceptionMessage != null) {
                                if (exceptionMessage.contains("no user record")) {
                                    errorMessage = "Email này chưa được đăng ký trong hệ thống.";
                                } else if (exceptionMessage.contains("network")) {
                                    errorMessage = "Lỗi kết nối mạng. Vui lòng thử lại.";
                                } else {
                                    errorMessage += exceptionMessage;
                                }
                            }
                        }
                        
                        Toast.makeText(ForgotPasswordActivity.this, 
                                errorMessage, 
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
