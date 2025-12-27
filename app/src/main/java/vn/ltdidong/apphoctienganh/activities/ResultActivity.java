package vn.ltdidong.apphoctienganh.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.managers.SkillManager;
import vn.ltdidong.apphoctienganh.database.AppDatabase;
import vn.ltdidong.apphoctienganh.database.UserProgressDao;
import vn.ltdidong.apphoctienganh.database.UserStreakDao;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;
import vn.ltdidong.apphoctienganh.models.ListeningLesson;
import vn.ltdidong.apphoctienganh.models.UserProgress;
import vn.ltdidong.apphoctienganh.models.UserStreak;
import vn.ltdidong.apphoctienganh.viewmodel.ListeningViewModel;

/**
 * Activity hiển thị kết quả quiz
 * Hiển thị điểm số, số câu đúng và các options để retry hoặc xem transcript
 */
public class ResultActivity extends AppCompatActivity {
    
    private ListeningViewModel viewModel;
    private int lessonId;
    private int correctAnswers;
    private int totalQuestions;
    private float score;
    private ListeningLesson lesson;
    private String quizType; // "listening" or "fill_blank"
    private String detailedResults; // JSON string for fill_blank results
    
    // UI components
    private TextView tvScorePercentage;
    private TextView tvCorrectAnswers;
    private TextView tvResultMessage;
    private MaterialButton btnViewTranscript;
    private MaterialButton btnTryAgain;
    private MaterialButton btnBackToLessons;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBackToList();
            }
        });
        
        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ListeningViewModel.class);
        
        // Lấy data từ Intent
        lessonId = getIntent().getIntExtra("lesson_id", -1);
        correctAnswers = getIntent().getIntExtra("correct_answers", 0);
        totalQuestions = getIntent().getIntExtra("total_questions", 0);
        score = getIntent().getFloatExtra("score", 0);
        quizType = getIntent().getStringExtra("quiz_type"); // "listening" or "fill_blank"
        detailedResults = getIntent().getStringExtra("detailed_results");
        
        // Debug log
        android.util.Log.d("ResultActivity", "Quiz Type: " + quizType);
        
        // Initialize views
        initViews();
        
        // Load lesson data
        loadLessonData();
        
        // Display results
        displayResults();
        
        // Save progress to database
        saveUserProgress();
        
        // Setup buttons
        setupButtons();
    }
    
    /**
     * Khởi tạo tất cả views
     */
    private void initViews() {
        tvScorePercentage = findViewById(R.id.tvScorePercentage);
        tvCorrectAnswers = findViewById(R.id.tvCorrectAnswers);
        tvResultMessage = findViewById(R.id.tvMessage);
        btnViewTranscript = findViewById(R.id.btnViewTranscript);
        btnTryAgain = findViewById(R.id.btnTryAgain);
        btnBackToLessons = findViewById(R.id.btnBackToLessons);
    }
    
    /**
     * Load lesson data từ database
     */
    private void loadLessonData() {
        viewModel.getLessonById(lessonId).observe(this, loadedLesson -> {
            lesson = loadedLesson;
        });
    }
    
    /**
     * Lưu tiến độ học tập vào database
     */
    private void saveUserProgress() {
        String userId = SharedPreferencesManager.getInstance(this).getUserId();
        android.util.Log.d("ResultActivity", "saveUserProgress - userId: " + userId + ", lessonId: " + lessonId);
        
        if (userId == null || lessonId == -1) {
            android.util.Log.e("ResultActivity", "Cannot save progress - userId is null or lessonId is invalid");
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            UserProgressDao progressDao = db.userProgressDao();
            UserStreakDao streakDao = db.userStreakDao();
            
            // 1. Cập nhật UserProgress
            UserProgress existingProgress = progressDao.getProgressByUserAndLessonSync(userId, lessonId);
            
            if (existingProgress != null) {
                // Cập nhật progress hiện có
                existingProgress.setCorrectAnswers(correctAnswers);
                existingProgress.setTotalQuestions(totalQuestions);
                existingProgress.setScore(score);
                existingProgress.setStatus("COMPLETED");
                existingProgress.setCompletedAt(currentTime);
                existingProgress.setAttempts(existingProgress.getAttempts() + 1);
                existingProgress.updateBestScore();
                
                progressDao.updateProgress(existingProgress);
            } else {
                // Tạo progress mới
                UserProgress newProgress = new UserProgress(
                    userId,
                    lessonId,
                    correctAnswers,
                    totalQuestions,
                    score,
                    "COMPLETED",
                    currentTime
                );
                
                progressDao.insertProgress(newProgress);
            }
            
            // 2. Cập nhật UserStreak
            UserStreak streak = streakDao.getStreakByUserSync(userId);
            
            if (streak == null) {
                // Tạo streak mới
                streak = new UserStreak(userId);
            }
            
            // Cập nhật streak (chỉ cập nhật 1 lần mỗi ngày)
            if (streak.shouldUpdateStreak(currentTime)) {
                streak.updateStreak(currentTime);
                
                if (streak.getId() == 0) {
                    streakDao.insertStreak(streak);
                } else {
                    streakDao.updateStreak(streak);
                }
            }
        });
        
        // 3. Cập nhật XP cho User và đồng bộ lên Firebase (chạy trên main thread)
        updateUserXP(userId, correctAnswers, totalQuestions);
        
        // 4. Cập nhật Listening Skill Score
        updateListeningSkillScore(score / 10.0); // Chuyển từ 0-100 sang 0-10
    }
    
    /**
     * Cập nhật kinh nghiệm (XP) cho user và đồng bộ lên Firebase
     */
    private void updateUserXP(String userId, int correctAnswers, int totalQuestions) {
        // Tính XP dựa trên số câu đúng
        // 10 XP cho mỗi câu đúng
        final int earnedXP = correctAnswers * 10 + (correctAnswers == totalQuestions ? 20 : 0);
        
        android.util.Log.d("ResultActivity", "updateUserXP - userId: " + userId + ", earnedXP: " + earnedXP);
        
        // Lấy thông tin user từ Firebase và cập nhật XP
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    android.util.Log.d("ResultActivity", "Firebase get user - success: " + documentSnapshot.exists());
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
                        
                        android.util.Log.d("ResultActivity", "Updating Firebase - totalXP: " + finalTotalXP + ", level: " + finalLevel + ", levelXP: " + finalLevelXP);
                        
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    android.util.Log.d("ResultActivity", "Firebase update SUCCESS");
                                    String xpMessage = "+" + earnedXP + " XP earned!";
                                    if (finalLeveledUp) {
                                        xpMessage += "\n🎉 Level Up! You're now Level " + finalLevel + "!";
                                    }
                                    android.widget.Toast.makeText(ResultActivity.this, xpMessage, android.widget.Toast.LENGTH_LONG).show();
                                })
                                .addOnFailureListener(e -> {
                                    android.util.Log.e("ResultActivity", "Error updating user XP", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("ResultActivity", "Error fetching user data", e);
                });
    }
    
    /**
     * Cập nhật điểm kỹ năng Listening
     */
    private void updateListeningSkillScore(double lessonScore) {
        String userId = SharedPreferencesManager.getInstance(this).getUserId();
        if (userId == null) return;
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get current scores map
                        java.util.Map<String, Object> data = documentSnapshot.getData();
                        java.util.Map<String, Double> skillScores = (java.util.Map<String, Double>) data.get("skill_scores");
                        java.util.Map<String, Long> lastPracticeTime = (java.util.Map<String, Long>) data.get("last_practice_time");
                        
                        if (skillScores == null) skillScores = new java.util.HashMap<>();
                        if (lastPracticeTime == null) lastPracticeTime = new java.util.HashMap<>();
                        
                        // Initialize if missing
                        if (!skillScores.containsKey(SkillManager.SKILL_LISTENING)) {
                            skillScores.put(SkillManager.SKILL_LISTENING, 5.0);
                        }
                        
                        // Get current score
                        Object currentObj = skillScores.get(SkillManager.SKILL_LISTENING);
                        double currentScore = 5.0;
                        if (currentObj instanceof Number) {
                            currentScore = ((Number) currentObj).doubleValue();
                        }
                        
                        // 1. Apply Decay first
                        long lastTime = 0;
                        if (lastPracticeTime.containsKey(SkillManager.SKILL_LISTENING)) {
                            lastTime = lastPracticeTime.get(SkillManager.SKILL_LISTENING);
                        }
                        double decayedScore = SkillManager.applyTimeDecay(currentScore, lastTime);
                        
                        // 2. Calculate New Score
                        double newScore = SkillManager.calculateNewScore(decayedScore, lessonScore);
                        
                        // 3. Update Map
                        skillScores.put(SkillManager.SKILL_LISTENING, newScore);
                        lastPracticeTime.put(SkillManager.SKILL_LISTENING, System.currentTimeMillis());
                        
                        // 4. Save to Firestore
                        final double finalCurrentScore = currentScore;
                        final double finalNewScore = newScore;
                        
                        db.collection("users").document(userId)
                                .update(
                                        "skill_scores", skillScores,
                                        "last_practice_time", lastPracticeTime)
                                .addOnSuccessListener(aVoid -> {
                                    String msg = String.format("Listening Score: %.1f -> %.1f", finalCurrentScore, finalNewScore);
                                    android.widget.Toast.makeText(ResultActivity.this, msg, android.widget.Toast.LENGTH_LONG).show();
                                })
                                .addOnFailureListener(e -> {
                                    android.util.Log.e("ResultActivity", "Error updating skill score", e);
                                });
                    }
                });
    }
    
    /**
     * Hiển thị kết quả
     */
    private void displayResults() {
        // Score percentage
        tvScorePercentage.setText(String.format("%.0f%%", score));
        
        // Correct answers
        tvCorrectAnswers.setText("Correct Answers: " + correctAnswers + "/" + totalQuestions);
        
        // Result message dựa trên điểm
        String message;
        if (score >= 90) {
            message = "🎉 Excellent! Outstanding work!";
            tvResultMessage.setTextColor(getResources().getColor(R.color.result_excellent));
        } else if (score >= 70) {
            message = "👍 Great job! You did very well!";
            tvResultMessage.setTextColor(getResources().getColor(R.color.result_great));
        } else if (score >= 50) {
            message = "😊 Good effort! Keep practicing!";
            tvResultMessage.setTextColor(getResources().getColor(R.color.result_good));
        } else {
            message = "💪 Don't give up! Try again!";
            tvResultMessage.setTextColor(getResources().getColor(R.color.result_try_again));
        }
        
        tvResultMessage.setText(message);
        
        // Animation
        tvScorePercentage.setScaleX(0f);
        tvScorePercentage.setScaleY(0f);
        tvScorePercentage.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setStartDelay(200)
                .start();
    }
    
    /**
     * Setup các buttons
     */
    private void setupButtons() {
        // Thay đổi label và chức năng nút tùy theo quiz type
        if ("fill_blank".equals(quizType)) {
            btnViewTranscript.setText("Xem lại kết quả");
            btnViewTranscript.setVisibility(android.view.View.VISIBLE);
            btnViewTranscript.setOnClickListener(v -> showDetailedResultsDialog());
        } else {
            btnViewTranscript.setText("Xem lại ghi chú");
            btnViewTranscript.setVisibility(android.view.View.VISIBLE);
            btnViewTranscript.setOnClickListener(v -> {
                if (lesson != null) {
                    showTranscriptDialog();
                }
            });
        }
        
        btnTryAgain.setOnClickListener(v -> {
            // Điều hướng về đúng activity tương ứng để làm lại
            Intent intent;
            if ("fill_blank".equals(quizType)) {
                intent = new Intent(ResultActivity.this, FillBlankActivity.class);
                intent.putExtra("lesson_id", String.valueOf(lessonId));
                intent.putExtra("lesson_title", getIntent().getStringExtra("lesson_title"));
            } else {
                intent = new Intent(ResultActivity.this, QuizActivity.class);
                intent.putExtra("lesson_id", lessonId);
            }
            startActivity(intent);
            finish();
        });
        
        btnBackToLessons.setOnClickListener(v -> {
            navigateBackToList();
        });
    }
    
    /**
     * Điều hướng quay lại danh sách tương ứng với quiz type
     */
    private void navigateBackToList() {
        android.util.Log.d("ResultActivity", "navigateBackToList - Quiz Type: " + quizType);
        Intent intent;
        if ("fill_blank".equals(quizType)) {
            // Quay về FillBlankLessonListActivity
            android.util.Log.d("ResultActivity", "Navigating to FillBlankLessonListActivity");
            intent = new Intent(ResultActivity.this, FillBlankLessonListActivity.class);
        } else {
            // Mặc định quay về ListeningListActivity
            android.util.Log.d("ResultActivity", "Navigating to ListeningListActivity");
            intent = new Intent(ResultActivity.this, ListeningListActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
    
    /**
     * Hiển thị dialog với kết quả chi tiết Fill Blank
     */
    private void showDetailedResultsDialog() {
        android.util.Log.d("ResultActivity", "showDetailedResultsDialog called");
        android.util.Log.d("ResultActivity", "detailedResults: " + detailedResults);
        
        if (detailedResults == null || detailedResults.isEmpty()) {
            android.widget.Toast.makeText(this, "Không có dữ liệu kết quả", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_fill_blank_results);
        
        android.widget.LinearLayout llResultsContainer = dialog.findViewById(R.id.llResultsContainer);
        MaterialButton btnClose = dialog.findViewById(R.id.btnClose);
        
        btnClose.setOnClickListener(v -> dialog.dismiss());
        
        // Parse JSON and create result items
        try {
            org.json.JSONArray resultsArray = new org.json.JSONArray(detailedResults);
            android.util.Log.d("ResultActivity", "Results array length: " + resultsArray.length());
            
            for (int i = 0; i < resultsArray.length(); i++) {
                org.json.JSONObject questionResult = resultsArray.getJSONObject(i);
                android.util.Log.d("ResultActivity", "Processing question " + (i + 1));
                
                // Inflate item layout
                android.view.View itemView = getLayoutInflater().inflate(R.layout.item_fill_blank_result, llResultsContainer, false);
                
                TextView tvQuestionNumber = itemView.findViewById(R.id.tvQuestionNumber);
                TextView tvSentence = itemView.findViewById(R.id.tvSentence);
                TextView tvUserAnswers = itemView.findViewById(R.id.tvUserAnswers);
                TextView tvCorrectAnswers = itemView.findViewById(R.id.tvCorrectAnswers);
                
                // Set data
                tvQuestionNumber.setText("Câu " + (i + 1));
                tvSentence.setText(questionResult.getString("sentence"));
                
                // User answers
                org.json.JSONArray userAnswersArray = questionResult.getJSONArray("userAnswers");
                StringBuilder userAnswersText = new StringBuilder();
                for (int j = 0; j < userAnswersArray.length(); j++) {
                    if (j > 0) userAnswersText.append(", ");
                    String answer = userAnswersArray.getString(j);
                    userAnswersText.append(answer.isEmpty() ? "(chưa điền)" : answer);
                }
                tvUserAnswers.setText(userAnswersText.toString());
                android.util.Log.d("ResultActivity", "User answers: " + userAnswersText.toString());
                
                // Correct answers
                org.json.JSONArray correctAnswersArray = questionResult.getJSONArray("correctAnswers");
                StringBuilder correctAnswersText = new StringBuilder();
                for (int j = 0; j < correctAnswersArray.length(); j++) {
                    if (j > 0) correctAnswersText.append(" / ");
                    correctAnswersText.append(correctAnswersArray.getString(j));
                }
                tvCorrectAnswers.setText(correctAnswersText.toString());
                android.util.Log.d("ResultActivity", "Correct answers: " + correctAnswersText.toString());
                
                llResultsContainer.addView(itemView);
                android.util.Log.d("ResultActivity", "Added view to container");
            }
            
            android.util.Log.d("ResultActivity", "Total views in container: " + llResultsContainer.getChildCount());
        } catch (org.json.JSONException e) {
            android.util.Log.e("ResultActivity", "JSON parsing error", e);
            e.printStackTrace();
            android.widget.Toast.makeText(this, "Lỗi khi hiển thị kết quả: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            dialog.dismiss();
            return;
        }
        
        dialog.show();
        
        // Set dialog size
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                (int) (getResources().getDisplayMetrics().heightPixels * 0.8)
            );
        }
    }
    
    /**
     * Hiển thị dialog với transcript
     */
    private void showTranscriptDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_transcript);
        
        TextView tvTranscriptTitle = dialog.findViewById(R.id.tvTranscriptTitle);
        TextView tvTranscriptContent = dialog.findViewById(R.id.tvTranscriptContent);
        MaterialButton btnClose = dialog.findViewById(R.id.btnClose);
        
        tvTranscriptTitle.setText(lesson.getTitle() + " - Transcript");
        tvTranscriptContent.setText(lesson.getTranscript());
        
        btnClose.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
        
        // Set dialog size
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                (int) (getResources().getDisplayMetrics().heightPixels * 0.7)
            );
        }
    }
    
}
