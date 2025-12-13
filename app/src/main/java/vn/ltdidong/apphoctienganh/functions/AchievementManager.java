package vn.ltdidong.apphoctienganh.functions;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AchievementManager {
    
    private static AchievementManager instance;
    private FirebaseFirestore firestore;
    private Context context;
    
    private AchievementManager(Context context) {
        this.context = context.getApplicationContext();
        this.firestore = FirebaseFirestore.getInstance();
    }
    
    public static AchievementManager getInstance(Context context) {
        if (instance == null) {
            instance = new AchievementManager(context);
        }
        return instance;
    }
    
    /**
     * Kiểm tra và unlock thành tích dựa trên level
     */
    public void checkLevelAchievements(String userId, int level) {
        List<String> achievementsToUnlock = new ArrayList<>();
        
        if (level >= 1) achievementsToUnlock.add("Người mới bắt đầu");
        if (level >= 5) achievementsToUnlock.add("Học viên tích cực");
        if (level >= 10) achievementsToUnlock.add("Chuyên gia");
        if (level >= 20) achievementsToUnlock.add("Bậc thầy");
        
        unlockAchievements(userId, achievementsToUnlock);
    }
    
    /**
     * Kiểm tra và unlock thành tích dựa trên XP
     */
    public void checkXPAchievements(String userId, long totalXP) {
        List<String> achievementsToUnlock = new ArrayList<>();
        
        if (totalXP >= 1000) achievementsToUnlock.add("Thu thập XP");
        if (totalXP >= 5000) achievementsToUnlock.add("Chuyên gia XP");
        if (totalXP >= 10000) achievementsToUnlock.add("Bậc thầy XP");
        
        unlockAchievements(userId, achievementsToUnlock);
    }
    
    /**
     * Kiểm tra và unlock thành tích dựa trên streak
     */
    public void checkStreakAchievements(String userId, int streak) {
        List<String> achievementsToUnlock = new ArrayList<>();
        
        if (streak >= 7) achievementsToUnlock.add("Nhất quán");
        if (streak >= 30) achievementsToUnlock.add("Kiên trì");
        if (streak >= 100) achievementsToUnlock.add("Huyền thoại");
        
        unlockAchievements(userId, achievementsToUnlock);
    }
    
    /**
     * Kiểm tra và unlock thành tích dựa trên số bài học hoàn thành
     */
    public void checkSkillAchievements(String userId, String skillType, int completedLessons) {
        List<String> achievementsToUnlock = new ArrayList<>();
        
        if (completedLessons >= 10) {
            switch (skillType) {
                case "reading":
                    achievementsToUnlock.add("Người đọc giỏi");
                    break;
                case "listening":
                    achievementsToUnlock.add("Tai thính");
                    break;
                case "writing":
                    achievementsToUnlock.add("Nhà văn");
                    break;
                case "speaking":
                    achievementsToUnlock.add("Diễn giả");
                    break;
            }
        }
        
        unlockAchievements(userId, achievementsToUnlock);
    }
    
    /**
     * Unlock achievements và lưu vào Firebase
     */
    private void unlockAchievements(String userId, List<String> achievementTitles) {
        if (achievementTitles.isEmpty()) return;
        
        firestore.collection("user_achievements")
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                List<String> currentUnlocked = new ArrayList<>();
                if (documentSnapshot.exists()) {
                    List<String> existing = (List<String>) documentSnapshot.get("unlocked_achievements");
                    if (existing != null) {
                        currentUnlocked.addAll(existing);
                    }
                }
                
                // Thêm thành tích mới
                List<String> newlyUnlocked = new ArrayList<>();
                for (String title : achievementTitles) {
                    if (!currentUnlocked.contains(title)) {
                        currentUnlocked.add(title);
                        newlyUnlocked.add(title);
                    }
                }
                
                // Lưu vào Firebase nếu có thành tích mới
                if (!newlyUnlocked.isEmpty()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("user_id", userId);
                    data.put("unlocked_achievements", currentUnlocked);
                    data.put("last_updated", System.currentTimeMillis());
                    
                    firestore.collection("user_achievements")
                        .document(userId)
                        .set(data)
                        .addOnSuccessListener(aVoid -> {
                            // Hiển thị thông báo cho thành tích mới
                            for (String title : newlyUnlocked) {
                                Toast.makeText(context, 
                                    "🎉 Thành tích mới: " + title + "!", 
                                    Toast.LENGTH_SHORT).show();
                            }
                        });
                }
            });
    }
    
    /**
     * Kiểm tra tất cả thành tích của user
     */
    public void checkAllAchievements(String userId) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Long level = documentSnapshot.getLong("current_level");
                    Long totalXp = documentSnapshot.getLong("total_xp");
                    
                    if (level != null) {
                        checkLevelAchievements(userId, level.intValue());
                    }
                    
                    if (totalXp != null) {
                        checkXPAchievements(userId, totalXp);
                    }
                }
            });
    }
}
