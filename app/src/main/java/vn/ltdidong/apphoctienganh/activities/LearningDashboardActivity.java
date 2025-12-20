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

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.RecommendationAdapter;
import vn.ltdidong.apphoctienganh.database.AppDatabase;
import vn.ltdidong.apphoctienganh.managers.DataSyncManager;
import vn.ltdidong.apphoctienganh.managers.LearningAnalyzer;
import vn.ltdidong.apphoctienganh.managers.PersonalizedRecommendationEngine;
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
    private RecyclerView rvRecommendations;
    private Button btnSyncData;
    private Button btnGenerateSchedule;
    private Button btnTestLesson;
    private ProgressBar progressBar;
    
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
        
        // Only proceed if critical views are not null
        if (btnSyncData != null && btnGenerateSchedule != null && btnTestLesson != null) {
            initListeners();
            
            // Auto-sync data from Firestore when opening Dashboard
            syncDataFromFirestore();
            
            // Load local data first (will update after sync)
            loadDashboardData();
        } else {
            Toast.makeText(this, "Error loading dashboard", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Critical views are null");
            finish();
        }
    }
    
    /**
     * Auto-sync dữ liệu của user từ Firestore
     */
    private void syncDataFromFirestore() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        // Use DataSyncManager for comprehensive sync
        DataSyncManager syncManager = new DataSyncManager(this);
        syncManager.downloadAllData(userId, new DataSyncManager.SyncCallback() {
            @Override
            public void onSyncSuccess(String message) {
                runOnUiThread(() -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Log.d(TAG, "Firestore sync success: " + message);
                    Toast.makeText(LearningDashboardActivity.this, 
                        "Đã đồng bộ dữ liệu!", Toast.LENGTH_SHORT).show();
                    // Reload dashboard data after sync
                    loadDashboardData();
                });
            }
            
            @Override
            public void onSyncError(String error) {
                runOnUiThread(() -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Log.e(TAG, "Firestore sync error: " + error);
                    // Load local data anyway
                    loadDashboardData();
                });
            }
            
            @Override
            public void onSyncProgress(int current, int total) {
                Log.d(TAG, "Sync progress: " + current + "/" + total);
            }
        });
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
        rvRecommendations = findViewById(R.id.rv_recommendations);
        btnSyncData = findViewById(R.id.btn_sync_data);
        btnGenerateSchedule = findViewById(R.id.btn_generate_schedule);
        btnTestLesson = findViewById(R.id.btn_test_lesson);
        progressBar = findViewById(R.id.progress_bar);
        
        // Setup RecyclerView only if not null
        if (rvRecommendations != null) {
            rvRecommendations.setLayoutManager(new LinearLayoutManager(this));
        }
    }
    
    private void initListeners() {
        // Null check để tránh crash
        if (btnSyncData == null || btnGenerateSchedule == null || btnTestLesson == null) {
            Log.e(TAG, "One or more buttons are null!");
            Toast.makeText(this, "Layout error. Please check resources.", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Sync button
        btnSyncData.setOnClickListener(v -> syncData());
        
        // Generate schedule button
        btnGenerateSchedule.setOnClickListener(v -> generateSchedule());
        
        // Test lesson button (để test tạo dữ liệu)
        btnTestLesson.setOnClickListener(v -> createMultipleTestSessions());
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
        
        // Load total study time từ StudyHabit
        AppDatabase.databaseWriteExecutor.execute(() -> {
            StudyHabit habit = database.studyHabitDao().getByUser(userId);
            
            runOnUiThread(() -> {
                if (habit != null) {
                    long totalMinutes = habit.getTotalStudyDays() * habit.getAverageSessionMinutes();
                    int hours = (int) (totalMinutes / 60);
                    int minutes = (int) (totalMinutes % 60);
                    tvTotalTime.setText(String.format("%dh %dm", hours, minutes));
                } else {
                    tvTotalTime.setText("Chưa có dữ liệu");
                }
            });
        });
        
        // Load weak skills - dùng LearningAnalyzer trực tiếp
        AppDatabase.databaseWriteExecutor.execute(() -> {
            LearningAnalyzer analyzer = new LearningAnalyzer(this);
            List<SkillProgress> weakSkills = analyzer.getWeakSkills(userId);
            
            runOnUiThread(() -> {
                if (weakSkills.isEmpty()) {
                    tvWeakSkills.setText("✅ Tất cả kỹ năng đều tốt!");
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (SkillProgress skill : weakSkills) {
                        sb.append(String.format("• %s: %.1f%%\n", 
                            getSkillDisplayName(skill.getSkillType()),
                            skill.getAverageScore()));
                    }
                    tvWeakSkills.setText(sb.toString());
                }
            });
        });
        
        // Load recommendations
        loadRecommendations();
    }
    
    /**
     * Load danh sách gợi ý
     */
    private void loadRecommendations() {
        new Thread(() -> {
            PersonalizedRecommendationEngine recommendationEngine = 
                new PersonalizedRecommendationEngine(this);
            List<PersonalizedRecommendationEngine.Recommendation> recommendations = 
                recommendationEngine.getPersonalizedRecommendations(userId);
            
            runOnUiThread(() -> {
                if (!recommendations.isEmpty()) {
                    // Setup RecyclerView with adapter
                    RecommendationAdapter adapter = new RecommendationAdapter();
                    adapter.setRecommendations(recommendations);
                    adapter.setOnRecommendationClickListener(recommendation -> {
                        Toast.makeText(this, 
                            "Bắt đầu: " + recommendation.title, 
                            Toast.LENGTH_SHORT).show();
                    });
                    rvRecommendations.setAdapter(adapter);
                    
                    Log.d(TAG, "Loaded " + recommendations.size() + " recommendations");
                } else {
                    Log.d(TAG, "No recommendations available");
                }
            });
        }).start();
    }
    
    /**
     * Đồng bộ dữ liệu
     */
    private void syncData() {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(ProgressBar.VISIBLE);
        btnSyncData.setEnabled(false);
        
        // Gọi trực tiếp DataSyncManager với userId
        vn.ltdidong.apphoctienganh.managers.DataSyncManager syncManager = 
            new vn.ltdidong.apphoctienganh.managers.DataSyncManager(this);
        
        syncManager.downloadAllData(userId, new vn.ltdidong.apphoctienganh.managers.DataSyncManager.SyncCallback() {
            @Override
            public void onSyncSuccess(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    btnSyncData.setEnabled(true);
                    Toast.makeText(LearningDashboardActivity.this, 
                        "Đồng bộ thành công!", Toast.LENGTH_SHORT).show();
                    
                    // Reload dashboard
                    loadDashboardData();
                });
            }
            
            @Override
            public void onSyncError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    btnSyncData.setEnabled(true);
                    Toast.makeText(LearningDashboardActivity.this, 
                        "Lỗi đồng bộ: " + error, Toast.LENGTH_LONG).show();
                });
            }
            
            @Override
            public void onSyncProgress(int current, int total) {
                runOnUiThread(() -> {
                    // Update progress
                    Log.d(TAG, "Sync progress: " + current + "/" + total);
                });
            }
        });
    }
    
    /**
     * Tạo lịch học
     */
    private void generateSchedule() {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(ProgressBar.VISIBLE);
        btnGenerateSchedule.setEnabled(false);
        
        // Gọi trực tiếp StudyScheduleGenerator với userId
        vn.ltdidong.apphoctienganh.managers.StudyScheduleGenerator scheduleGenerator = 
            new vn.ltdidong.apphoctienganh.managers.StudyScheduleGenerator(this);
        
        scheduleGenerator.generateSchedule(userId, 7, new vn.ltdidong.apphoctienganh.managers.StudyScheduleGenerator.ScheduleCallback() {
            @Override
            public void onScheduleGenerated(List<vn.ltdidong.apphoctienganh.models.StudySchedule> schedules) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    btnGenerateSchedule.setEnabled(true);
                    Toast.makeText(LearningDashboardActivity.this, 
                        "Đã tạo " + schedules.size() + " lịch học!", 
                        Toast.LENGTH_SHORT).show();
                    
                    // Log schedules
                    for (vn.ltdidong.apphoctienganh.models.StudySchedule schedule : schedules) {
                        Log.d(TAG, "Schedule: " + schedule.getSkillType() + 
                                   " - " + schedule.getReason());
                    }
                });
            }
            
            @Override
            public void onScheduleError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    btnGenerateSchedule.setEnabled(true);
                    Toast.makeText(LearningDashboardActivity.this, 
                        "Lỗi tạo lịch: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    /**
     * Tạo nhiều dữ liệu test (để demo)
     */
    private void createMultipleTestSessions() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (btnTestLesson != null) {
            btnTestLesson.setEnabled(false);
        }
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            String[] skills = {"LISTENING", "SPEAKING", "READING", "WRITING", "VOCABULARY", "GRAMMAR"};
            String[] difficulties = {"EASY", "MEDIUM", "HARD"};
            
            for (int i = 0; i < 15; i++) {
                LearningSession session = new LearningSession();
                session.setUserId(userId);
                session.setSkillType(skills[i % skills.length]);
                session.setLessonId(i + 1);
                session.setLessonName("Test Lesson " + (i + 1));
                session.setScore(60 + (float)(Math.random() * 40)); // 60-100
                session.setCorrectAnswers(12 + (int)(Math.random() * 13)); // 12-24
                session.setTotalQuestions(25);
                
                // Tạo sessions trong 15 ngày qua
                long daysAgo = (15 - i) * 24 * 60 * 60 * 1000L;
                session.setStartTime(System.currentTimeMillis() - daysAgo);
                session.setEndTime(session.getStartTime() + ((20 + (int)(Math.random() * 40)) * 60 * 1000)); // 20-60 phút
                
                session.setDifficulty(difficulties[i % 3]);
                session.setCompleted(true);
                
                session.calculateAccuracy();
                session.calculateDuration();
                
                database.learningSessionDao().insert(session);
            }
            
            // Đợi 1 giây rồi phân tích
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                LearningAnalyzer analyzer = new LearningAnalyzer(this);
                analyzer.analyzeAllData(userId, new LearningAnalyzer.AnalysisCallback() {
                    @Override
                    public void onAnalysisComplete(String message) {
                        runOnUiThread(() -> {
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            if (btnTestLesson != null) {
                                btnTestLesson.setEnabled(true);
                            }
                            Toast.makeText(LearningDashboardActivity.this, 
                                "✅ Đã tạo 15 phiên học test!", Toast.LENGTH_SHORT).show();
                            loadDashboardData();
                        });
                    }
                    
                    @Override
                    public void onAnalysisError(String error) {
                        runOnUiThread(() -> {
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            if (btnTestLesson != null) {
                                btnTestLesson.setEnabled(true);
                            }
                            Toast.makeText(LearningDashboardActivity.this, 
                                "Lỗi phân tích: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }, 1000);
        });
    }
    

    
    /**
     * Lấy tên hiển thị của kỹ năng
     */
    private String getSkillDisplayName(String skillType) {
        try {
            SkillType skill = SkillType.valueOf(skillType);
            return skill.getDisplayName();
        } catch (Exception e) {
            return skillType;
        }
    }
}
