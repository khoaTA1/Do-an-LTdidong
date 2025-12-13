package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import vn.ltdidong.apphoctienganh.R;

public class AdvanceModeActivity extends AppCompatActivity {
    BottomNavigationView bottomNav;
    MaterialCardView firstMode;
    MaterialCardView secondMode;

    @Override
    protected void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        setContentView(R.layout.activity_advance_mode_page);

        bottomNav = findViewById(R.id.bottomNavigation);
        firstMode = findViewById(R.id.first_mode);
        secondMode = findViewById(R.id.second_mode);

        bottomNav.setSelectedItemId(R.id.nav_advance_mode);


        // mode storytelling road map
        firstMode.setOnClickListener(v -> {
            Intent intent = new Intent(AdvanceModeActivity.this, MemoryMatchActivity.class);
            startActivity(intent);

            // không finish để có gì bấm nút back còn quay lại được trang này
        });


        // dành cho mode nâng cao thứ 2
        secondMode.setOnClickListener(v -> {
            //Intent intent = new Intent(AdvanceModeActivity.this, StorytellingRoadMapActivity.class);
            //startActivity(intent);

            // không finish để có gì bấm nút back còn quay lại được trang này
        });


        // thanh điều hướng dưới
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Intent intent = new Intent(AdvanceModeActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_skills) {
                Intent intent = new Intent(AdvanceModeActivity.this, SkillHomeActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_advance_mode) {
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(AdvanceModeActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }
}
