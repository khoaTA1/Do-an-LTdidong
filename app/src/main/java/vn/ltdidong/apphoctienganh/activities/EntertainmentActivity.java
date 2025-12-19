package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import vn.ltdidong.apphoctienganh.R;

public class EntertainmentActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private MaterialCardView cardStoryAdventure;
    private MaterialCardView cardCrossword;
    private MaterialCardView cardMemoryMatch;
    private MaterialCardView funFactCard;
    private TextView tvFunFactContent;

    private String[] funFacts = {
            "Did you know? The word 'alphabet' comes from the first two letters of the Greek alphabet: alpha and beta!",
            "The longest word in English has 189,819 letters! It's the chemical name for the protein titin.",
            "Shakespeare invented over 1,700 words that we still use today, including 'eyeball' and 'bedroom'!",
            "The sentence 'The quick brown fox jumps over the lazy dog' uses every letter of the alphabet!",
            "English is the official language of the sky! All pilots must speak English on international flights.",
            "The word 'set' has more definitions than any other word in English - over 430!",
            "A new word is added to the English dictionary every 2 hours!",
            "The dot over the letter 'i' is called a 'tittle'.",
            "The shortest complete sentence in English is 'I am.' or 'I do.'",
            "More English words begin with the letter 'S' than any other letter!"
    };
    private int currentFactIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entertainment);

        initializeViews();
        setupBottomNavigation();
        setupClickListeners();
        setupFunFacts();
    }

    private void initializeViews() {
        bottomNav = findViewById(R.id.bottomNavigation);
        cardStoryAdventure = findViewById(R.id.cardStoryAdventure);
        cardCrossword = findViewById(R.id.cardCrossword);
        cardMemoryMatch = findViewById(R.id.cardMemoryMatch);
        funFactCard = findViewById(R.id.funFactCard);
        tvFunFactContent = findViewById(R.id.tvFunFactContent);
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_entertainment);
        
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            
            if (id == R.id.nav_home) {
                Intent intent = new Intent(EntertainmentActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_skills) {
                Intent intent = new Intent(EntertainmentActivity.this, SkillHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            }
            
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Đồng bộ selected item khi activity được reuse
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_entertainment);
        }
    }

    private void setupClickListeners() {
        cardStoryAdventure.setOnClickListener(v -> {
            Intent intent = new Intent(EntertainmentActivity.this, StoryAdventureActivity.class);
            startActivity(intent);
        });

        cardCrossword.setOnClickListener(v -> {
            Intent intent = new Intent(EntertainmentActivity.this, CrosswordGameActivity.class);
            startActivity(intent);
        });

        cardMemoryMatch.setOnClickListener(v -> {
            Intent intent = new Intent(EntertainmentActivity.this, MemoryMatchStartPageActivity.class);
            startActivity(intent);
        });
    }

    private void setupFunFacts() {
        // Shuffle and show random fact on start
        currentFactIndex = (int) (Math.random() * funFacts.length);
        tvFunFactContent.setText(funFacts[currentFactIndex]);

        // Click to show next fact
        funFactCard.setOnClickListener(v -> {
            currentFactIndex = (currentFactIndex + 1) % funFacts.length;
            tvFunFactContent.setText(funFacts[currentFactIndex]);
            
            // Add a little animation
            tvFunFactContent.setAlpha(0f);
            tvFunFactContent.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
        });
    }
}
