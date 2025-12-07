package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.database.AppDatabase;
import vn.ltdidong.apphoctienganh.database.UserStreakDao;
import vn.ltdidong.apphoctienganh.models.UserStreak;
import vn.ltdidong.apphoctienganh.dialogs.DailyGoalDialog;
import vn.ltdidong.apphoctienganh.dialogs.EditNameDialog;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private Executor executor = Executors.newSingleThreadExecutor();
    private UserStreakDao streakDao;
    
    // Basic Info
    private ImageView imgAvatar;
    private TextView tvUsername;
    private TextView tvEmail;
    private TextView btnChangeAvatar;
    private ImageView btnEditName;
    
    // Progress & Stats
    private TextView tvLevel;
    private TextView tvTotalXP;
    private TextView tvLevelProgress;
    private ProgressBar progressLevel;
    private TextView tvStreak;
    private TextView tvLongestStreak;
    
    // Goals & Settings
    private LinearLayout layoutDailyGoal;
    private TextView tvDailyGoal;
    private TextView btnViewAllAchievements;
    private LinearLayout btnWishlist;
    private LinearLayout btnSettings;
    private LinearLayout btnHelp;
    private LinearLayout btnInviteFriends;
    private MaterialButton btnLogout;
    
    private BottomNavigationView bottomNav;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle saveIns) {
        super.onCreate(saveIns);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        streakDao = AppDatabase.getDatabase(this).userStreakDao();
        
        // Initialize views
        initializeViews();
        
        // Setup bottom navigation
        bottomNav.setSelectedItemId(R.id.nav_profile);
        setupBottomNavigation();

        // Check login state and setup accordingly
        if (!SharedPreferencesManager.getInstance(this).isLoggedIn()) {
            disableLoginRequiredFeatures();
        } else {
            enableLoginRequiredFeatures();
            loadUserData();
        }
    }
    
    private void initializeViews() {
        // Basic Info
        imgAvatar = findViewById(R.id.imgAvatar);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        btnEditName = findViewById(R.id.btnEditName);
        
        // Progress & Stats
        tvLevel = findViewById(R.id.tvLevel);
        tvTotalXP = findViewById(R.id.tvTotalXP);
        tvLevelProgress = findViewById(R.id.tvLevelProgress);
        progressLevel = findViewById(R.id.progressLevel);
        tvStreak = findViewById(R.id.tvStreak);
        tvLongestStreak = findViewById(R.id.tvLongestStreak);
        
        // Goals & Settings
        layoutDailyGoal = findViewById(R.id.layoutDailyGoal);
        tvDailyGoal = findViewById(R.id.tvDailyGoal);
        btnViewAllAchievements = findViewById(R.id.btnViewAllAchievements);
        btnWishlist = findViewById(R.id.btn_wishlist);
        btnSettings = findViewById(R.id.btnSettings);
        btnHelp = findViewById(R.id.btnHelp);
        btnInviteFriends = findViewById(R.id.btnInviteFriends);
        btnLogout = findViewById(R.id.logout_id);
        bottomNav = findViewById(R.id.bottomNavigation);
    }
    
    private void disableLoginRequiredFeatures() {
        // Disable all interactive elements
        btnLogout.setAlpha(0.3f);
        btnLogout.setEnabled(false);
        btnWishlist.setAlpha(0.3f);
        btnWishlist.setEnabled(false);
        btnSettings.setAlpha(0.3f);
        btnSettings.setEnabled(false);
        btnHelp.setAlpha(0.3f);
        btnHelp.setEnabled(false);
        btnInviteFriends.setAlpha(0.3f);
        btnInviteFriends.setEnabled(false);
        layoutDailyGoal.setAlpha(0.3f);
        layoutDailyGoal.setEnabled(false);
        btnChangeAvatar.setEnabled(false);
        btnEditName.setEnabled(false);
        
        // Show default values
        tvUsername.setText("Người dùng");
        tvEmail.setText("Vui lòng đăng nhập");
        tvLevel.setText("Level 0");
        tvTotalXP.setText("0 XP");
        tvStreak.setText("0 ngày");
    }
    
    private void enableLoginRequiredFeatures() {
        // Enable all features
        btnLogout.setAlpha(1f);
        btnLogout.setEnabled(true);
        btnWishlist.setAlpha(1f);
        btnWishlist.setEnabled(true);
        btnSettings.setAlpha(1f);
        btnSettings.setEnabled(true);
        btnHelp.setAlpha(1f);
        btnHelp.setEnabled(true);
        btnInviteFriends.setAlpha(1f);
        btnInviteFriends.setEnabled(true);
        layoutDailyGoal.setAlpha(1f);
        layoutDailyGoal.setEnabled(true);
        
        // Setup click listeners
        btnLogout.setOnClickListener(v -> {
            SharedPreferencesManager.getInstance(this).clearUserData();
            recreate();
        });

        btnWishlist.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, WishlistActivity.class);
            startActivity(intent);
        });
        
        btnChangeAvatar.setOnClickListener(v -> 
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
        );
        
        btnEditName.setOnClickListener(v -> {
            String currentName = tvUsername.getText().toString();
            EditNameDialog dialog = new EditNameDialog(this, currentName, newName -> {
                tvUsername.setText(newName);
            });
            dialog.show();
        });
        
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
        
        btnHelp.setOnClickListener(v -> {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
        });
        
        btnInviteFriends.setOnClickListener(v -> 
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
        );
        
        layoutDailyGoal.setOnClickListener(v -> {
            DailyGoalDialog dialog = new DailyGoalDialog(this, newGoal -> {
                tvDailyGoal.setText(newGoal);
            });
            dialog.show();
        });
        
        btnViewAllAchievements.setOnClickListener(v -> 
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
        );
    }
    
    private void loadUserData() {
        String userId = SharedPreferencesManager.getInstance(this).getUserId();
        String username = SharedPreferencesManager.getInstance(this).getUserName();
        String email = SharedPreferencesManager.getInstance(this).getUserEmail();
        
        // Display basic info
        tvUsername.setText(username != null ? username : "Người dùng");
        tvEmail.setText(email != null ? email : "Chưa có email");
        
        // Load daily goal
        String dailyGoal = SharedPreferencesManager.getInstance(this)
            .getDailyGoal();
        tvDailyGoal.setText(dailyGoal);
        
        // Load data from Firebase
        if (userId != null && !userId.isEmpty()) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(this::displayUserProgress)
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Error loading user data", e)
                );
            
            // Load streak from Room Database
            loadStreakData(userId);
        }
    }
    
    private void displayUserProgress(DocumentSnapshot document) {
        if (document.exists()) {
            // Get data from Firebase
            Long totalXP = document.getLong("total_xp");
            Long currentLevel = document.getLong("current_level");
            Long currentLevelXP = document.getLong("current_level_xp");
            Long xpToNextLevel = document.getLong("xp_to_next_level");
            
            // Display Level
            if (currentLevel != null) {
                tvLevel.setText("Level " + currentLevel);
            }
            
            // Display Total XP
            if (totalXP != null) {
                tvTotalXP.setText(totalXP + " XP");
            }
            
            // Display Level Progress
            if (currentLevelXP != null && xpToNextLevel != null) {
                tvLevelProgress.setText(currentLevelXP + " / " + xpToNextLevel + " XP");
                int progress = (int) ((currentLevelXP * 100.0) / xpToNextLevel);
                progressLevel.setProgress(progress);
            }
            
            Log.d(TAG, "User progress loaded successfully");
        }
    }
    
    private void loadStreakData(String userId) {
        executor.execute(() -> {
            UserStreak streak = streakDao.getStreakByUserSync(userId);
            
            runOnUiThread(() -> {
                if (streak != null) {
                    tvStreak.setText(streak.getCurrentStreak() + " ngày");
                    tvLongestStreak.setText("Cao nhất: " + streak.getLongestStreak());
                    Log.d(TAG, "Streak loaded: " + streak.getCurrentStreak());
                } else {
                    tvStreak.setText("0 ngày");
                    tvLongestStreak.setText("Cao nhất: 0");
                    Log.d(TAG, "No streak data found");
                }
            });
        });
    }
    
    private void setupBottomNavigation() {
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