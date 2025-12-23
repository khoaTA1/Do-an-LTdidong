package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;

public class ReadingSkillResultActivity extends AppCompatActivity {
    private TextView congratTitle, score, total;
    private MaterialButton btnBack;
    @Override
    protected void onCreate(Bundle saveIns) {
        super.onCreate(saveIns);
        setContentView(R.layout.reading_skill_all_mode_result);

        // ánh xạ các thành phần view
        congratTitle = findViewById(R.id.congrat_title);
        score = findViewById(R.id.score);
        total = findViewById(R.id.total);
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> {
            finish();
        });

        Intent intent = getIntent();
        int intentScore = intent.getIntExtra("score", -1);
        int intentTotal = intent.getIntExtra("total", -1);

        if (intentScore == -1 || intentTotal == -1) {
            congratTitle.setText("Error, please try again");
        } else if ((float) intentScore / intentTotal < 0.5) {
            congratTitle.setText("Keep going!\nEvery mistake is a step toward improvement");
            score.setText(String.valueOf(intentScore));
            total.setText("/" + intentTotal);
        } else {
            congratTitle.setText("Congratulation!\nThat's a good result");
            score.setText(String.valueOf(intentScore));
            total.setText("/" + intentTotal);
        }
        
        // Update user XP for reading
        String userId = SharedPreferencesManager.getInstance(this).getUserId();
        if (userId != null && intentScore != -1 && intentTotal != -1) {
            updateUserXP(userId, intentScore, intentTotal);
        }
    }
    
    /**
     * Cập nhật kinh nghiệm (XP) cho user và đồng bộ lên Firebase
     */
    private void updateUserXP(String userId, int correctAnswers, int totalQuestions) {
        // Tính XP dựa trên số câu đúng
        // 10 XP cho mỗi câu đúng
        final int earnedXP = correctAnswers * 10 + (correctAnswers == totalQuestions ? 20 : 0);
        
        // Lấy thông tin user từ Firebase và cập nhật XP
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Lấy XP hiện tại
                        Long currentTotalXP = documentSnapshot.getLong("total_xp");
                        Long currentLevel = documentSnapshot.getLong("current_level");
                        Long currentLevelXP = documentSnapshot.getLong("current_level_xp");
                        Long xpToNextLevel = documentSnapshot.getLong("xp_to_next_level");
                        
                        // Khởi tạo giá trị mặc định nếu null
                        int newTotalXP = (currentTotalXP != null) ? currentTotalXP.intValue() : 0;
                        int newLevel = (currentLevel != null) ? currentLevel.intValue() : 1;
                        int newLevelXP = (currentLevelXP != null) ? currentLevelXP.intValue() : 0;
                        int newNextLevelXP = (xpToNextLevel != null) ? xpToNextLevel.intValue() : 100;
                        
                        // Cộng XP mới
                        newTotalXP += earnedXP;
                        newLevelXP += earnedXP;
                        
                        // Kiểm tra level up
                        boolean leveledUp = false;
                        while (newLevelXP >= newNextLevelXP) {
                            leveledUp = true;
                            newLevelXP -= newNextLevelXP;
                            newLevel++;
                            newNextLevelXP = 100 + (newLevel - 1) * 50;
                        }
                        
                        // Cập nhật lên Firebase
                        final int finalTotalXP = newTotalXP;
                        final int finalLevel = newLevel;
                        final int finalLevelXP = newLevelXP;
                        final int finalNextLevelXP = newNextLevelXP;
                        final boolean finalLeveledUp = leveledUp;
                        
                        java.util.Map<String, Object> updates = new java.util.HashMap<>();
                        updates.put("total_xp", finalTotalXP);
                        updates.put("current_level", finalLevel);
                        updates.put("current_level_xp", finalLevelXP);
                        updates.put("xp_to_next_level", finalNextLevelXP);
                        
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    String xpMessage = "+" + earnedXP + " XP earned!";
                                    if (finalLeveledUp) {
                                        xpMessage += "\n🎉 Level Up! You're now Level " + finalLevel + "!";
                                    }
                                    Toast.makeText(ReadingSkillResultActivity.this, xpMessage, Toast.LENGTH_LONG).show();
                                });
                    }
                });
    }
}
