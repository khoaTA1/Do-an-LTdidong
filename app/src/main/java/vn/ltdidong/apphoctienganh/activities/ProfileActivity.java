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
    private BottomNavigationView bottomNav;
    @Override
    protected void onCreate(Bundle saveIns) {
        super.onCreate(saveIns);
        setContentView(R.layout.activity_profile);

        btnLogout = findViewById(R.id.logout_id);
        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_profile);

        if (!SharedPreferencesManager.getInstance(this).isLoggedIn()) {
            btnLogout.setAlpha(0.3f);
            btnLogout.setEnabled(false);
        } else {
            btnLogout.setAlpha(1f);
            btnLogout.setEnabled(true);

            btnLogout.setOnClickListener(v -> {
                SharedPreferencesManager.getInstance(this).clearUserData();
                recreate();
            });
        }

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
