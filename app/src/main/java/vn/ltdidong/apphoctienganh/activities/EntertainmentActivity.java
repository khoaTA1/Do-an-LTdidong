package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import vn.ltdidong.apphoctienganh.R;

public class EntertainmentActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private MaterialCardView cardStoryAdventure;
    private MaterialCardView cardCrossword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entertainment);

        initializeViews();
        setupBottomNavigation();
        setupClickListeners();
    }

    private void initializeViews() {
        bottomNav = findViewById(R.id.bottomNavigation);
        cardStoryAdventure = findViewById(R.id.cardStoryAdventure);
        cardCrossword = findViewById(R.id.cardCrossword);
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_entertainment);
        
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            
            if (id == R.id.nav_home) {
                Intent intent = new Intent(EntertainmentActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_skills) {
                Intent intent = new Intent(EntertainmentActivity.this, SkillHomeActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_camera) {
                Intent intent = new Intent(EntertainmentActivity.this, CameraActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_entertainment) {
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(EntertainmentActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            
            return false;
        });
    }

    private void setupClickListeners() {
        cardStoryAdventure.setOnClickListener(v -> {
            Toast.makeText(this, "Story Adventure coming soon!", Toast.LENGTH_SHORT).show();
        });

        cardCrossword.setOnClickListener(v -> {
            Intent intent = new Intent(EntertainmentActivity.this, CrosswordGameActivity.class);
            startActivity(intent);
        });
    }
}
