package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;

public class ProfileActivity extends AppCompatActivity {

    private MaterialButton btnLogout;
    private MaterialButton btnWishlist; // 1. Khai báo thêm nút Wishlist
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle saveIns) {
        super.onCreate(saveIns);
        setContentView(R.layout.activity_profile);

        btnLogout = findViewById(R.id.logout_id);
        btnWishlist = findViewById(R.id.btn_wishlist); // 2. Ánh xạ nút Wishlist
        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_profile);

        // Kiểm tra trạng thái đăng nhập
        if (!SharedPreferencesManager.getInstance(this).isLoggedIn()) {
            // Logic cũ: Ẩn nút đăng xuất
            btnLogout.setAlpha(0.3f);
            btnLogout.setEnabled(false);

            // Logic mới: Cũng ẩn luôn nút Wishlist (vì chưa đăng nhập ko xem được)
            btnWishlist.setAlpha(0.3f);
            btnWishlist.setEnabled(false);
        } else {
            // Logic cũ: Hiện nút đăng xuất
            btnLogout.setAlpha(1f);
            btnLogout.setEnabled(true);
            btnLogout.setOnClickListener(v -> {
                SharedPreferencesManager.getInstance(this).clearUserData();
                recreate(); // Giữ nguyên cách reload của code cũ
            });

            // Logic mới: Hiện nút Wishlist và bắt sự kiện click
            btnWishlist.setAlpha(1f);
            btnWishlist.setEnabled(true);
            btnWishlist.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, WishlistActivity.class);
                startActivity(intent);
            });
        }

        // Logic điều hướng giữ nguyên
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_skills) {
                Intent intent = new Intent(ProfileActivity.this, SkillHomeActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }

            return false;
        });
    }
}