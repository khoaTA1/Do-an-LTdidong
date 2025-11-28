package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.functions.FirestoreCallBack;
import vn.ltdidong.apphoctienganh.functions.PasswordHasher;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;
import vn.ltdidong.apphoctienganh.models.User;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout textInputEmail;
    private TextInputLayout textInputPassword;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private Button buttonLogin;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firestore = FirebaseFirestore.getInstance();

        textInputEmail = findViewById(R.id.text_input_email);
        textInputPassword = findViewById(R.id.text_input_password);
        buttonLogin = findViewById(R.id.button_login);

        if (textInputEmail.getEditText() != null) {
            editTextEmail = (TextInputEditText) textInputEmail.getEditText();
        }
        if (textInputPassword.getEditText() != null) {
            editTextPassword = (TextInputEditText) textInputPassword.getEditText();
        }

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            textInputEmail.setError(null);
            textInputPassword.setError(null);

            validateLogin(email, password, user -> {
                if (user != null) {
                    // đăng nhập thành công
                    Log.d(">> Login", "success");

                    // Lưu thông tin user vào SharedPreferencesManager
                    SharedPreferencesManager.getInstance(LoginActivity.this).saveUserData((User) user);

                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                    // Tạo Intent để mở HomeActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                    // Bắt đầu HomeActivity
                    startActivity(intent);
                    // Đóng login activity để người dùng không thể nhấn back quay lại
                    finish();
                } else {
                    // đăng nhập thất bại
                    // Toast.makeText(this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                    textInputPassword.setError("Tên đăng nhập hoặc mật khẩu không chính xác");
                }
            });
        });
    }
    private void validateLogin(String email, String password, FirestoreCallBack callback) {
        // 1. Kiểm tra các trường không được để trống
        if (email.isEmpty()) {
            textInputEmail.setError("Email không được để trống");
            callback.returnResult(null);
            return;
        }
        if (password.isEmpty()) {
            textInputPassword.setError("Mật khẩu không được để trống");
            callback.returnResult(null);
            return;
        }

        firestore.collection("users").whereEqualTo("email", email).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("Firebase", "Email không tồn tại: " + email);
                        callback.returnResult(null);
                        return;
                    }

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String userId = document.getId();
                        Log.d("Firebase", "Tìm thấy user ID: " + userId);

                        // lấy mật khẩu băm đã lưu trong firestore
                        String savedPassword = document.getString("password");
                        // lấy salt để băm mật khẩu
                        String salt = document.getString("salt");

                        if (savedPassword == null || salt == null) {
                            Log.e("Firebase", "Dữ liệu user không đầy đủ");
                            callback.returnResult(null);
                            return;
                        }

                        // băm lại mật khẩu mà người dùng đã nhập với salt
                        String hashedInputPassword = PasswordHasher.hashPassword(password, salt);

                        // kiểm tra 2 mật khẩu đã băm
                        if (savedPassword.equals(hashedInputPassword)) {
                            Log.d("Firebase", "Đăng nhập thành công!");
                            User user = new User();

                            // LƯƯU ID TRỰC TIẾP TỪ FIREBASE DOCUMENT ID (String)
                            // Không cần convert sang Integer nữa
                            try {
                                user.setId(Integer.parseInt(userId));
                            } catch (NumberFormatException e) {
                                // Nếu không parse được, dùng hashCode
                                user.setId(userId.hashCode());
                            }

                            user.setEmail(email);
                            user.setPassword(password);

                            // Lấy thêm fullname nếu có
                            String fullname = document.getString("fullname");
                            if (fullname != null) {
                                user.setFullName(fullname);
                            }

                            // Lấy phone và address
                            String phone = document.getString("phone");
                            if (phone != null) {
                                user.setPhone(phone);
                            }

                            String address = document.getString("address");
                            if (address != null) {
                                user.setAddress(address);
                            }

                            // Lấy role từ Firestore, mặc định là "user"
                            String role = document.getString("role");
                            if (role != null) {
                                user.setRole(role);
                            } else {
                                user.setRole("user"); // Default role
                            }

                            callback.returnResult(user);
                            return;
                        } else {
                            Log.d("Firebase", "Mật khẩu không khớp");
                        }
                    }
                    callback.returnResult(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Lỗi kết nối Firestore: " + e.getMessage(), e);
                    Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    callback.returnResult(null);
                });
    }
}
