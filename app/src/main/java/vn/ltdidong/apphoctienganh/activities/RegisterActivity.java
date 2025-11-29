package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.functions.PasswordHasher;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout textInputUsername, textInputEmail, textInputPassword, textInputConfirmPassword;
    private TextInputEditText editTextUsername, editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonRegister;
    private TextView textLogin;
    private FirebaseFirestore cloud_db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        cloud_db = FirebaseFirestore.getInstance();

        textInputUsername = findViewById(R.id.text_input_username);
        textInputEmail = findViewById(R.id.text_input_email);
        textInputPassword = findViewById(R.id.text_input_password);
        textInputConfirmPassword = findViewById(R.id.text_input_confirm_password);
        buttonRegister = findViewById(R.id.button_register);
        textLogin = findViewById(R.id.text_login);

        // Lấy EditText từ bên trong TextInputLayout
        // kiểm tra null
        if (textInputUsername.getEditText() != null)
            editTextUsername = (TextInputEditText) textInputUsername.getEditText();
        if (textInputEmail.getEditText() != null)
            editTextEmail = (TextInputEditText) textInputEmail.getEditText();
        if (textInputPassword.getEditText() != null)
            editTextPassword = (TextInputEditText) textInputPassword.getEditText();
        if (textInputConfirmPassword.getEditText() != null)
            editTextConfirmPassword = (TextInputEditText) textInputConfirmPassword.getEditText();

        buttonRegister.setOnClickListener(v -> {
            // Gọi hàm đăng ký khi nhấn nút
            registerUser();
        });

        textLogin.setOnClickListener(v -> {
            // Quay về màn hình đăng nhập
            finish();
        });
    }

    /**
     * Hàm chính để xử lý logic đăng ký
     */
    private void registerUser() {
        // 1. Lấy dữ liệu người dùng nhập
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // 2. Kiểm tra dữ liệu có hợp lệ không
        boolean isUsernameValid = validateUsername(username);
        boolean isEmailValid = validateEmail(email);
        boolean isPasswordValid = validatePassword(password, confirmPassword);

        // 3. Nếu tất cả đều hợp lệ, kiểm tra email đã tồn tại chưa
        if (isUsernameValid && isEmailValid && isPasswordValid) {
            // Hiển thị loading (có thể thêm ProgressDialog hoặc disable button)
            buttonRegister.setEnabled(false);
            buttonRegister.setText("Đang xử lý...");

            // Kiểm tra email đã tồn tại chưa
            cloud_db.collection("users").whereEqualTo("email", email).get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            // Email đã tồn tại
                            Log.d("Firebase", "Email đã được sử dụng: " + email);
                            textInputEmail.setError("Email này đã được đăng ký");
                            buttonRegister.setEnabled(true);
                            buttonRegister.setText("Đăng ký");
                            return;
                        }

                        // Email chưa tồn tại, tiến hành đăng ký
                        Map<String, Object> user = new HashMap<>();
                        user.put("fullname", username);
                        user.put("email", email);

                        String salt = PasswordHasher.generateSalt();
                        String hashedPassword = PasswordHasher.hashPassword(password, salt);
                        user.put("password", hashedPassword);
                        user.put("salt", salt);
                        user.put("createdAt", System.currentTimeMillis());

                        // lưu vào firestore
                        // lấy user id mới nhất và cập nhật lại bằng transaction nhằm tránh race condition
                        DocumentReference docRef = cloud_db.collection("pref").document("trackLastUserId");

                        cloud_db.runTransaction(transaction -> {
                                    DocumentSnapshot docSnap = transaction.get(docRef);

                                    Long lastUserIdObj = docSnap.getLong("lastUserId");
                                    long lastUserId = (lastUserIdObj != null) ? lastUserIdObj : 0;

                                    // cập nhật pref
                                    transaction.update(docRef, "lastUserId", lastUserId + 1);

                                    // lưu user mới vào firestore
                                    transaction.set(cloud_db.collection("users").document(String.valueOf(lastUserId + 1)), user);

                                    return lastUserId + 1;
                                }).addOnSuccessListener(userId -> {
                                    Log.d("Firebase", "Đã đăng ký user ID: " + userId);
                                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                                    // Tạo Intent để trả dữ liệu (email, password) về cho MainActivity
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("email", email);
                                    resultIntent.putExtra("password", password);
                                    setResult(RESULT_OK, resultIntent);

                                    // Đóng màn hình đăng ký và quay về màn hình đăng nhập
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firebase", "Lỗi transaction: " + e.getMessage(), e);
                                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    buttonRegister.setEnabled(true);
                                    buttonRegister.setText("Đăng ký");
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firebase", "Lỗi kiểm tra email: " + e.getMessage(), e);
                        Toast.makeText(this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        buttonRegister.setEnabled(true);
                        buttonRegister.setText("Đăng ký");
                    });
        }
    }


    /**
     * Hàm kiểm tra tên người dùng
     */
    private boolean validateUsername(String username) {
        if (username.isEmpty()) {
            textInputUsername.setError("Tên người dùng không được để trống");
            return false;
        }
        // Nếu hợp lệ, xóa thông báo lỗi
        textInputUsername.setError(null);
        return true;
    }

    /**
     * Hàm kiểm tra Email
     */
    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            textInputEmail.setError("Email không được để trống");
            return false;
        }
        // Sử dụng hàm có sẵn của Android để kiểm tra định dạng email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputEmail.setError("Vui lòng nhập địa chỉ email hợp lệ");
            return false;
        }
        textInputEmail.setError(null);
        return true;
    }

    /**
     * Hàm kiểm tra Mật khẩu và Xác nhận mật khẩu
     */
    private boolean validatePassword(String password, String confirmPassword) {
        boolean isValid = true;

        // Kiểm tra ô mật khẩu
        if (password.isEmpty()) {
            textInputPassword.setError("Mật khẩu không được để trống");
            isValid = false;
        } else if (password.length() < 6) {
            textInputPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            isValid = false;
        } else {
            textInputPassword.setError(null);
        }

        // Kiểm tra ô xác nhận mật khẩu
        if (confirmPassword.isEmpty()) {
            textInputConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            // Chỉ kiểm tra khớp nếu ô mật khẩu đã hợp lệ
            if (isValid) {
                textInputConfirmPassword.setError("Mật khẩu không khớp");
                isValid = false;
            }
        } else {
            textInputConfirmPassword.setError(null);
        }

        return isValid;
    }
}
