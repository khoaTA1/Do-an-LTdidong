package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.AchievementAdapter;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;
import vn.ltdidong.apphoctienganh.models.Achievement;

public class AchievementsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewAchievements;
    private AchievementAdapter achievementAdapter;
    private List<Achievement> achievementList;
    private FirebaseFirestore firestore;
    private String userId;
    private TextView tvAchievementCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thành tích");
        }

        firestore = FirebaseFirestore.getInstance();
        userId = SharedPreferencesManager.getInstance(this).getUserId();

        tvAchievementCount = findViewById(R.id.tvAchievementCount);
        recyclerViewAchievements = findViewById(R.id.recyclerViewAchievements);
        recyclerViewAchievements.setLayoutManager(new GridLayoutManager(this, 2));

        achievementList = new ArrayList<>();
        achievementAdapter = new AchievementAdapter(this, achievementList);
        recyclerViewAchievements.setAdapter(achievementAdapter);

        loadAchievements();
        
        // Debug: Log số lượng thành tích
        android.util.Log.d("Achievements", "Total achievements: " + achievementList.size());
    }

    private void loadAchievements() {
        // Tạo danh sách thành tích mẫu
        achievementList.clear();
        
        // Thành tích dựa trên level
        achievementList.add(new Achievement(
            "🌱",
            "Người mới bắt đầu",
            "Đạt Level 1",
            true,
            1
        ));
        
        achievementList.add(new Achievement(
            "🌿",
            "Học viên tích cực",
            "Đạt Level 5",
            false,
            5
        ));
        
        achievementList.add(new Achievement(
            "🌳",
            "Chuyên gia",
            "Đạt Level 10",
            false,
            10
        ));
        
        achievementList.add(new Achievement(
            "🏆",
            "Bậc thầy",
            "Đạt Level 20",
            false,
            20
        ));
        
        // Thành tích dựa trên streak
        achievementList.add(new Achievement(
            "🔥",
            "Nhất quán",
            "Học 7 ngày liên tiếp",
            false,
            7
        ));
        
        achievementList.add(new Achievement(
            "⚡",
            "Kiên trì",
            "Học 30 ngày liên tiếp",
            false,
            30
        ));
        
        achievementList.add(new Achievement(
            "💎",
            "Huyền thoại",
            "Học 100 ngày liên tiếp",
            false,
            100
        ));
        
        // Thành tích dựa trên XP
        achievementList.add(new Achievement(
            "⭐",
            "Thu thập XP",
            "Đạt 1000 XP",
            false,
            1000
        ));
        
        achievementList.add(new Achievement(
            "🌟",
            "Chuyên gia XP",
            "Đạt 5000 XP",
            false,
            5000
        ));
        
        achievementList.add(new Achievement(
            "✨",
            "Bậc thầy XP",
            "Đạt 10000 XP",
            false,
            10000
        ));
        
        // Thành tích dựa trên kỹ năng
        achievementList.add(new Achievement(
            "📖",
            "Người đọc giỏi",
            "Hoàn thành 10 bài Reading",
            false,
            10
        ));
        
        achievementList.add(new Achievement(
            "👂",
            "Tai thính",
            "Hoàn thành 10 bài Listening",
            false,
            10
        ));
        
        achievementList.add(new Achievement(
            "✍️",
            "Nhà văn",
            "Hoàn thành 10 bài Writing",
            false,
            10
        ));
        
        achievementList.add(new Achievement(
            "🗣️",
            "Diễn giả",
            "Hoàn thành 10 bài Speaking",
            false,
            10
        ));
        
        // Kiểm tra user data từ Firebase để unlock achievements
        if (userId != null && !userId.isEmpty()) {
            checkUserAchievements();
        }
        
        achievementAdapter.notifyDataSetChanged();
        android.util.Log.d("Achievements", "After notify, list size: " + achievementList.size() + ", adapter count: " + achievementAdapter.getItemCount());
    }

    private void checkUserAchievements() {
        // Load unlocked achievements từ Firebase
        firestore.collection("user_achievements")
            .document(userId)
            .get()
            .addOnSuccessListener(achievementDoc -> {
                List<String> unlockedAchievements = (List<String>) achievementDoc.get("unlocked_achievements");
                
                // Đánh dấu các thành tích đã unlock
                if (unlockedAchievements != null) {
                    for (Achievement achievement : achievementList) {
                        if (unlockedAchievements.contains(achievement.getTitle())) {
                            achievement.setUnlocked(true);
                        }
                    }
                }
                
                // Kiểm tra điều kiện để unlock thành tích mới
                checkAndUnlockNewAchievements();
            })
            .addOnFailureListener(e -> {
                // Nếu chưa có document, tạo mới và kiểm tra
                checkAndUnlockNewAchievements();
            });
    }
    
    private void checkAndUnlockNewAchievements() {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<Achievement> newlyUnlocked = new ArrayList<>();
                    
                    // Lấy dữ liệu user
                    Long level = documentSnapshot.getLong("current_level");
                    Long totalXp = documentSnapshot.getLong("total_xp");
                    
                    if (level != null) {
                        // Check achievements dựa trên level
                        for (Achievement achievement : achievementList) {
                            if (!achievement.isUnlocked() && 
                                (achievement.getTitle().contains("Level") || 
                                 achievement.getTitle().contains("Người mới") ||
                                 achievement.getTitle().contains("Học viên") ||
                                 achievement.getTitle().contains("Chuyên gia") ||
                                 achievement.getTitle().contains("Bậc thầy"))) {
                                if (level >= achievement.getRequirement()) {
                                    achievement.setUnlocked(true);
                                    newlyUnlocked.add(achievement);
                                }
                            }
                        }
                    }
                    
                    if (totalXp != null) {
                        // Check achievements dựa trên XP
                        for (Achievement achievement : achievementList) {
                            if (!achievement.isUnlocked() && achievement.getTitle().contains("XP")) {
                                if (totalXp >= achievement.getRequirement()) {
                                    achievement.setUnlocked(true);
                                    newlyUnlocked.add(achievement);
                                }
                            }
                        }
                    }
                    
                    // Lưu các thành tích mới vào Firebase
                    if (!newlyUnlocked.isEmpty()) {
                        saveUnlockedAchievements(newlyUnlocked);
                    }
                    
                    updateAchievementCount();
                    achievementAdapter.notifyDataSetChanged();
                }
            });
    }
    
    private void saveUnlockedAchievements(List<Achievement> newAchievements) {
        // Lấy danh sách tất cả thành tích đã unlock
        List<String> unlockedTitles = new ArrayList<>();
        for (Achievement achievement : achievementList) {
            if (achievement.isUnlocked()) {
                unlockedTitles.add(achievement.getTitle());
            }
        }
        
        // Lưu vào Firebase
        firestore.collection("user_achievements")
            .document(userId)
            .set(new java.util.HashMap<String, Object>() {{
                put("user_id", userId);
                put("unlocked_achievements", unlockedTitles);
                put("last_updated", System.currentTimeMillis());
            }})
            .addOnSuccessListener(aVoid -> {
                // Hiển thị thông báo cho thành tích mới
                for (Achievement achievement : newAchievements) {
                    Toast.makeText(this, 
                        "🎉 Thành tích mới: " + achievement.getTitle() + "!", 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void updateAchievementCount() {
        int unlockedCount = 0;
        for (Achievement achievement : achievementList) {
            if (achievement.isUnlocked()) {
                unlockedCount++;
            }
        }
        
        tvAchievementCount.setText("Đã đạt được: " + unlockedCount + "/" + achievementList.size());
        
        // Gửi kết quả về ProfileActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("achievementCount", unlockedCount);
        resultIntent.putExtra("totalAchievements", achievementList.size());
        setResult(RESULT_OK, resultIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
