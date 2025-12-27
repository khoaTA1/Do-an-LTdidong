package vn.ltdidong.apphoctienganh.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.RecommendationAdapter;
import vn.ltdidong.apphoctienganh.database.AppDatabase;
import vn.ltdidong.apphoctienganh.managers.DataSyncManager;
import vn.ltdidong.apphoctienganh.managers.LearningAnalyzer;
import vn.ltdidong.apphoctienganh.managers.PersonalizedRecommendationEngine;
import vn.ltdidong.apphoctienganh.managers.SkillManager;
import vn.ltdidong.apphoctienganh.managers.StudyScheduleGenerator;
import vn.ltdidong.apphoctienganh.models.LearningSession;
import vn.ltdidong.apphoctienganh.models.SkillProgress;
import vn.ltdidong.apphoctienganh.models.SkillType;
import vn.ltdidong.apphoctienganh.models.StudyHabit;

/**
 * Activity ví dụ minh họa cách sử dụng hệ thống học tập thông minh
 * Màn hình Dashboard hiển thị:
 * - Streak và thống kê tổng quan
 * - Kỹ năng yếu cần cải thiện
 * - Gợi ý học tập hôm nay
 * - Lịch học
 */
public class LearningDashboardActivity extends AppCompatActivity {
    
    private static final String TAG = "LearningDashboard";
    
    // UI Components
    private TextView tvSummary;
    private TextView tvStreak;
    private TextView tvTotalTime;
    private TextView tvWeakSkills;
    private ProgressBar progressBar;
    
    // Skill Scores UI
    private TextView tvReadingScore, tvWritingScore, tvListeningScore, tvSpeakingScore;
    private ProgressBar pbReading, pbWriting, pbListening, pbSpeaking;
    private TextView tvLastPracticeReading, tvLastPracticeWriting, tvLastPracticeListening, tvLastPracticeSpeaking;
    
    // Managers
    private AppDatabase database;
    private String userId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "=== onCreate START ===");
        
        // Use main layout (not test layout)
        setContentView(R.layout.activity_learning_dashboard);
        
        initComponents();
        
        // Load local data first (will update after sync)
        loadDashboardData();
    }
    
    private void initComponents() {
        // Initialize database
        database = AppDatabase.getDatabase(this);
        
        // Get current user from SharedPreferences (app doesn't use Firebase Auth)
        userId = vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager
                .getInstance(this).getUserId();
        
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No user in SharedPreferences");
            finish();
            return;
        }
        
        // Initialize UI components
        tvSummary = findViewById(R.id.tv_summary);
        tvStreak = findViewById(R.id.tv_streak);
        tvTotalTime = findViewById(R.id.tv_total_time);
        tvWeakSkills = findViewById(R.id.tv_weak_skills);
        progressBar = findViewById(R.id.progress_bar);
        
        // Initialize Skill Scores UI
        tvReadingScore = findViewById(R.id.tv_reading_score);
        tvWritingScore = findViewById(R.id.tv_writing_score);
        tvListeningScore = findViewById(R.id.tv_listening_score);
        tvSpeakingScore = findViewById(R.id.tv_speaking_score);
        
        pbReading = findViewById(R.id.pb_reading);
        pbWriting = findViewById(R.id.pb_writing);
        pbListening = findViewById(R.id.pb_listening);
        pbSpeaking = findViewById(R.id.pb_speaking);
        
        tvLastPracticeReading = findViewById(R.id.tv_last_practice_reading);
        tvLastPracticeWriting = findViewById(R.id.tv_last_practice_writing);
        tvLastPracticeListening = findViewById(R.id.tv_last_practice_listening);
        tvLastPracticeSpeaking = findViewById(R.id.tv_last_practice_speaking);
    }
    
    /**
     * Load dữ liệu cho dashboard
     */
    private void loadDashboardData() {
        // Null check
        if (tvSummary == null || tvStreak == null || tvTotalTime == null || tvWeakSkills == null) {
            Log.e(TAG, "TextViews are null in loadDashboardData!");
            return;
        }
        
        // Hiển thị tóm tắt - dùng PersonalizedRecommendationEngine trực tiếp
        PersonalizedRecommendationEngine recommendationEngine = 
            new PersonalizedRecommendationEngine(this);
        String summary = recommendationEngine.getDailySummary(userId);
        tvSummary.setText(summary);
        
        // Load streak từ UserStreak table (giống như Home)
        database.userStreakDao().getStreakByUser(userId).observe(this, userStreak -> {
            if (userStreak != null) {
                tvStreak.setText(String.format("%d ngày 🔥", userStreak.getCurrentStreak()));
            } else {
                tvStreak.setText("0 ngày");
            }
        });
        
        // Load total time và weak skills từ Firebase
        loadTimeAndWeakSkillsFromFirebase();
        
        // Load skill scores from Firebase
        loadSkillScoresFromFirebase();
    }
    
    /**
     * Load điểm kỹ năng từ Firebase
     */
    private void loadSkillScoresFromFirebase() {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID is null");
            return;
        }
        
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data != null) {
                            // Get skill_scores
                            Map<String, Double> skillScores = (Map<String, Double>) data.get("skill_scores");
                            Map<String, Long> lastPracticeTime = (Map<String, Long>) data.get("last_practice_time");
                            
                            if (skillScores != null) {
                                updateSkillScoreUI(SkillManager.SKILL_READING, skillScores, lastPracticeTime, 
                                    tvReadingScore, pbReading, tvLastPracticeReading);
                                updateSkillScoreUI(SkillManager.SKILL_WRITING, skillScores, lastPracticeTime, 
                                    tvWritingScore, pbWriting, tvLastPracticeWriting);
                                updateSkillScoreUI(SkillManager.SKILL_LISTENING, skillScores, lastPracticeTime, 
                                    tvListeningScore, pbListening, tvLastPracticeListening);
                                updateSkillScoreUI(SkillManager.SKILL_SPEAKING, skillScores, lastPracticeTime, 
                                    tvSpeakingScore, pbSpeaking, tvLastPracticeSpeaking);
                            } else {
                                Log.d(TAG, "No skill_scores found in Firebase");
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load skill scores from Firebase", e);
                });
    }
    
    /**
     * Cập nhật UI cho một kỹ năng
     */
    private void updateSkillScoreUI(String skillKey, Map<String, Double> skillScores, 
                                     Map<String, Long> lastPracticeTime,
                                     TextView tvScore, ProgressBar progressBar, TextView tvLastPractice) {
        if (tvScore == null || progressBar == null || tvLastPractice == null) {
            return;
        }
        
        // Get score (default 5.0)
        Double scoreObj = skillScores.get(skillKey);
        double score = (scoreObj != null) ? scoreObj : 5.0;
        
        // Update score text
        tvScore.setText(String.format(Locale.getDefault(), "%.1f / 10", score));
        
        // Update progress bar (0-10 scale to 0-100 percentage)
        int progress = (int) (score * 10);
        progressBar.setProgress(progress);
        
        // Update last practice time or advice based on score
        if (lastPracticeTime != null && lastPracticeTime.containsKey(skillKey)) {
            Long timestamp = lastPracticeTime.get(skillKey);
            if (timestamp != null && timestamp > 0) {
                String timeAgo = getTimeAgo(timestamp);
                tvLastPractice.setText("Lần cuối: " + timeAgo);
                return;
            }
        }
        
        // Nếu chưa có lịch sử luyện tập, đưa ra lời khuyên dựa trên điểm
        String advice = getAdviceForScore(score);
        tvLastPractice.setText(advice);
    }
    
    /**
     * Đưa ra lời khuyên dựa trên điểm số
     */
    private String getAdviceForScore(double score) {
        if (score < 3.0) {
            return "⚠️ Cần cải thiện khẩn cấp!";
        } else if (score < 5.0) {
            return "💪 Cần luyện tập nhiều hơn";
        } else if (score < 7.0) {
            return "😊 Ổn, có thể tốt hơn";
        } else if (score < 9.0) {
            return "👍 Tốt! Tiếp tục duy trì";
        } else {
            return "🌟 Xuất sắc! Giữ vững phong độ";
        }
    }
    
    /**
     * Chuyển đổi timestamp thành text "x phút/giờ/ngày trước"
     */
    private String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + " ngày trước";
        } else if (hours > 0) {
            return hours + " giờ trước";
        } else if (minutes > 0) {
            return minutes + " phút trước";
        } else {
            return "Vừa xong";
        }
    }
    
    /**
     * Load tổng thời gian và kỹ năng yếu nhất từ Firebase
     */
    private void loadTimeAndWeakSkillsFromFirebase() {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID is null");
            return;
        }
        
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data != null) {
                            // Get skill_scores và last_practice_time
                            Map<String, Double> skillScores = (Map<String, Double>) data.get("skill_scores");
                            
                            // 1. Lấy tổng thời gian sử dụng app từ Firebase
                            Long totalSessionTime = documentSnapshot.getLong("total_session_time");
                            if (totalSessionTime != null && totalSessionTime > 0) {
                                // totalSessionTime lưu bằng milliseconds, chuyển sang phút
                                long totalMinutes = totalSessionTime / (60 * 1000);
                                int hours = (int) (totalMinutes / 60);
                                int minutes = (int) (totalMinutes % 60);
                                tvTotalTime.setText(String.format("%dh %dm", hours, minutes));
                            } else {
                                tvTotalTime.setText("0h 0m");
                            }
                            
                            // 2. Tìm kỹ năng yếu nhất
                            if (skillScores != null && !skillScores.isEmpty()) {
                                String weakestSkill = findWeakestSkill(skillScores);
                                Double weakestScore = skillScores.get(weakestSkill);
                                if (weakestSkill != null && weakestScore != null) {
                                    String skillName = getSkillDisplayName(weakestSkill);
                                    tvWeakSkills.setText(String.format("%s: %.1f/10\nCần cải thiện kỹ năng này!", 
                                        skillName, weakestScore));
                                } else {
                                    tvWeakSkills.setText("✅ Tất cả kỹ năng đều tốt!");
                                }
                            } else {
                                tvWeakSkills.setText("Chưa có dữ liệu");
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load time and weak skills from Firebase", e);
                    tvTotalTime.setText("Lỗi tải dữ liệu");
                    tvWeakSkills.setText("Lỗi tải dữ liệu");
                });
    }
    
    /**
     * Tìm kỹ năng có điểm thấp nhất
     */
    private String findWeakestSkill(Map<String, Double> skillScores) {
        String weakestSkill = null;
        double lowestScore = 11.0; // Max là 10, nên bắt đầu từ 11
        
        for (Map.Entry<String, Double> entry : skillScores.entrySet()) {
            if (entry.getValue() != null && entry.getValue() < lowestScore) {
                lowestScore = entry.getValue();
                weakestSkill = entry.getKey();
            }
        }
        
        return weakestSkill;
    }
    
    /**
     * Lấy tên hiển thị của kỹ năng
     */
    private String getSkillDisplayName(String skillKey) {
        switch (skillKey) {
            case SkillManager.SKILL_READING:
                return "📖 Reading";
            case SkillManager.SKILL_WRITING:
                return "✍️ Writing";
            case SkillManager.SKILL_LISTENING:
                return "🎧 Listening";
            case SkillManager.SKILL_SPEAKING:
                return "🗣️ Speaking";
            default:
                return skillKey;
        }
    }
}
