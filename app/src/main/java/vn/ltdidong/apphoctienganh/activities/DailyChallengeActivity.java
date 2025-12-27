package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.ChallengeAdapter;
import vn.ltdidong.apphoctienganh.base.BaseActivity;
import vn.ltdidong.apphoctienganh.database.AppDatabase;
import vn.ltdidong.apphoctienganh.database.DailyChallengeDao;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;
import vn.ltdidong.apphoctienganh.managers.SkillManager;
import vn.ltdidong.apphoctienganh.models.ChallengeItem;
import vn.ltdidong.apphoctienganh.models.DailyChallenge;
import vn.ltdidong.apphoctienganh.models.User;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

/**
 * Activity hiển thị Daily Challenges
 * Người dùng hoàn thành các thử thách hàng ngày để nhận XP và rewards
 */
public class DailyChallengeActivity extends BaseActivity {

    private MaterialToolbar toolbar;
    private TextView tvDate;
    private TextView tvProgress;
    private TextView tvXP;
    private ProgressBar progressBar;
    private RecyclerView rvChallenges;
    private MaterialCardView cardReward;
    private TextView tvRewardMessage;
    private Button btnClaimReward;

    private DailyChallengeDao challengeDao;
    private Executor executor;
    private DailyChallenge todayChallenge;
    private String userId;
    private String todayDate;

    private List<ChallengeItem> challengeItems;
    private ChallengeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_challenge);

        initViews();
        setupToolbar(toolbar, "Daily Challenges", true);
        
        executor = Executors.newSingleThreadExecutor();
        challengeDao = AppDatabase.getDatabase(this).dailyChallengeDao();
        
        SharedPreferencesManager prefsManager = SharedPreferencesManager.getInstance(this);
        userId = prefsManager.getUserId();
        todayDate = getTodayDate();
        
        loadTodayChallenge();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvDate = findViewById(R.id.tv_date);
        tvProgress = findViewById(R.id.tv_progress);
        tvXP = findViewById(R.id.tv_xp);
        progressBar = findViewById(R.id.progress_bar);
        rvChallenges = findViewById(R.id.rv_challenges);
        cardReward = findViewById(R.id.card_reward);
        tvRewardMessage = findViewById(R.id.tv_reward_message);
        btnClaimReward = findViewById(R.id.btn_claim_reward);

        tvDate.setText(getFormattedDate());
        
        challengeItems = new ArrayList<>();
        adapter = new ChallengeAdapter(challengeItems, this::onChallengeClick);
        rvChallenges.setLayoutManager(new LinearLayoutManager(this));
        rvChallenges.setAdapter(adapter);

        btnClaimReward.setOnClickListener(v -> claimReward());
    }

    private void loadTodayChallenge() {
        showLoading("Loading challenges...");
        
        executor.execute(() -> {
            todayChallenge = challengeDao.getChallengeByDate(userId, todayDate);
            
            if (todayChallenge == null) {
                // Create new challenge for today
                todayChallenge = createNewChallenge();
                challengeDao.insert(todayChallenge);
            }
            
            runOnUiThread(() -> {
                hideLoading();
                updateUI();
            });
        });
    }

    private DailyChallenge createNewChallenge() {
        DailyChallenge challenge = new DailyChallenge();
        challenge.setUserId(userId);
        challenge.setDate(todayDate);
        return challenge;
    }

    private void updateUI() {
        // Lấy dữ liệu user từ Firebase để xác định kỹ năng yếu nhất
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                String weakestSkill = SkillManager.SKILL_READING; // Default
                
                if (documentSnapshot.exists()) {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data != null && data.containsKey("skill_scores")) {
                        try {
                            Map<String, Double> skillScores = (Map<String, Double>) data.get("skill_scores");
                            if (skillScores != null && !skillScores.isEmpty()) {
                                // Tìm kỹ năng có điểm thấp nhất
                                weakestSkill = findWeakestSkill(skillScores);
                            }
                        } catch (Exception e) { 
                            e.printStackTrace(); 
                        }
                    }
                }
                
                // Update UI với kỹ năng yếu nhất
                updateUIWithWeakestSkill(weakestSkill);
            })
            .addOnFailureListener(e -> {
                // Fallback to default
                updateUIWithWeakestSkill(SkillManager.SKILL_READING);
            });
    }
    
    /**
     * Tìm kỹ năng có điểm thấp nhất
     */
    private String findWeakestSkill(Map<String, Double> skillScores) {
        String weakestSkill = SkillManager.SKILL_READING;
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
     * Cập nhật UI với kỹ năng yếu nhất
     */
    private void updateUIWithWeakestSkill(String weakestSkill) {
        // Update progress
        todayChallenge.calculateProgress();
        todayChallenge.calculateXP();
        
        int progress = todayChallenge.getProgressPercentage();
        progressBar.setProgress(progress);
        tvProgress.setText(todayChallenge.getCompletedChallenges() + "/" + todayChallenge.getTotalChallenges());
        tvXP.setText(todayChallenge.getXpEarned() + " XP");

        // Update challenge list dựa trên kỹ năng yếu nhất
        challengeItems.clear();
        challengeItems.addAll(getChallengesForSkill(weakestSkill));

        adapter.notifyDataSetChanged();

        // Show reward card if all completed
        if (todayChallenge.isAllCompleted()) {
            cardReward.setVisibility(View.VISIBLE);
            tvRewardMessage.setText("🎉 Congratulations! You've completed all challenges today!");
        } else {
            cardReward.setVisibility(View.GONE);
        }
    }
    
    /**
     * Lấy danh sách 6 nhiệm vụ cho kỹ năng cụ thể
     */
    private List<ChallengeItem> getChallengesForSkill(String skill) {
        List<ChallengeItem> challenges = new ArrayList<>();
        
        switch (skill) {
            case SkillManager.SKILL_READING:
                challenges.add(new ChallengeItem(
                    "📖 Read 1 Article",
                    "Complete one reading comprehension article",
                    15, "reading", todayChallenge.isReadingCompleted()));
                challenges.add(new ChallengeItem(
                    "📚 Read 3 Passages",
                    "Practice reading with 3 short passages",
                    20, "reading_multi", false));
                challenges.add(new ChallengeItem(
                    "🎯 Answer 10 Questions",
                    "Complete 10 reading comprehension questions",
                    15, "reading_quiz", false));
                challenges.add(new ChallengeItem(
                    "⏱️ Speed Reading",
                    "Read and answer questions within 5 minutes",
                    20, "reading_speed", false));
                challenges.add(new ChallengeItem(
                    "📰 Daily News",
                    "Read and summarize a news article",
                    15, "reading_news", false));
                challenges.add(new ChallengeItem(
                    "🔤 Vocabulary Building",
                    "Learn 10 new words from reading context",
                    15, "reading_vocab", false));
                break;
                
            case SkillManager.SKILL_WRITING:
                challenges.add(new ChallengeItem(
                    "✍️ Write an Essay",
                    "Complete a 200-word essay",
                    15, "writing", todayChallenge.isWritingCompleted()));
                challenges.add(new ChallengeItem(
                    "📝 Daily Journal",
                    "Write about your day (100 words)",
                    15, "writing_journal", false));
                challenges.add(new ChallengeItem(
                    "💌 Write a Letter",
                    "Compose a formal or informal letter",
                    20, "writing_letter", false));
                challenges.add(new ChallengeItem(
                    "📋 Summary Writing",
                    "Summarize a story in 50 words",
                    15, "writing_summary", false));
                challenges.add(new ChallengeItem(
                    "🎨 Creative Writing",
                    "Write a creative paragraph with given words",
                    20, "writing_creative", false));
                challenges.add(new ChallengeItem(
                    "✅ Grammar Practice",
                    "Complete 10 grammar correction exercises",
                    15, "writing_grammar", false));
                break;
                
            case SkillManager.SKILL_LISTENING:
                challenges.add(new ChallengeItem(
                    "🎧 Listen to 2 Audio",
                    "Complete two easy listening exercise (" + todayChallenge.getListeningEasyCount() + "/2)",
                    15, "listening_easy", todayChallenge.getListeningEasyCount() >= 2));
                challenges.add(new ChallengeItem(
                    "🎵 Fill blank exercise",
                    "Complete one fill-in-the-blank exercise (" + todayChallenge.getListeningFillBlankCount() + "/1)",
                    20, "listening_fill_blank", todayChallenge.getListeningFillBlankCount() >= 1));
                challenges.add(new ChallengeItem(
                    "📻 Find exp",
                    "Earn 50 exp from listening (" + todayChallenge.getListeningExpEarned() + "/50)",
                    15, "listening_exp", todayChallenge.getListeningExpEarned() >= 50));
                challenges.add(new ChallengeItem(
                    "🎧 Listen to 1 Audio",
                    "Complete one medium listening exercise (" + todayChallenge.getListeningMediumCount() + "/1)",
                    20, "listening_medium", todayChallenge.getListeningMediumCount() >= 1));
                challenges.add(new ChallengeItem(
                    "🎧 Listen to 1 Audio",
                    "Complete one difficult listening exercise (" + todayChallenge.getListeningHardCount() + "/1)",
                    20, "listening_hard", todayChallenge.getListeningHardCount() >= 1));
                challenges.add(new ChallengeItem(
                    "🔊 Pronunciation",
                    "Listen and repeat 20 words correctly",
                    15, "listening_pronunciation", false));
                break;
                
            case SkillManager.SKILL_SPEAKING:
                challenges.add(new ChallengeItem(
                    "🗣️ Speaking Practice",
                    "Complete one speaking exercise",
                    15, "speaking", todayChallenge.isSpeakingCompleted()));
                challenges.add(new ChallengeItem(
                    "🎤 Record Yourself",
                    "Record a 1-minute speech",
                    20, "speaking_record", false));
                challenges.add(new ChallengeItem(
                    "💬 Conversation Practice",
                    "Practice a dialogue with AI",
                    20, "speaking_conversation", false));
                challenges.add(new ChallengeItem(
                    "📢 Describe a Picture",
                    "Speak about an image for 2 minutes",
                    15, "speaking_describe", false));
                challenges.add(new ChallengeItem(
                    "🎭 Role Play",
                    "Act out a given scenario",
                    20, "speaking_roleplay", false));
                challenges.add(new ChallengeItem(
                    "🔤 Pronunciation Drill",
                    "Repeat 30 challenging words",
                    15, "speaking_drill", false));
                break;
                
            default:
                // Fallback: Mixed challenges
                challenges.add(new ChallengeItem(
                    "📖 Reading", "Practice reading", 15, "reading", todayChallenge.isReadingCompleted()));
                challenges.add(new ChallengeItem(
                    "✍️ Writing", "Practice writing", 15, "writing", todayChallenge.isWritingCompleted()));
                challenges.add(new ChallengeItem(
                    "🎧 Listening", "Practice listening", 15, "listening", todayChallenge.isListeningCompleted()));
                challenges.add(new ChallengeItem(
                    "🗣️ Speaking", "Practice speaking", 15, "speaking", todayChallenge.isSpeakingCompleted()));
                challenges.add(new ChallengeItem(
                    "📚 Learn 5 Words", "Expand vocabulary", 10, "vocabulary", todayChallenge.isLearnNewWordsCompleted()));
                challenges.add(new ChallengeItem(
                    "🎯 Complete Quiz", "Test knowledge", 10, "quiz", todayChallenge.isCompleteQuizCompleted()));
                break;
        }
        
        return challenges;
    }

    private void onChallengeClick(ChallengeItem item) {
        if (item.isCompleted()) {
            showToast("✅ Bạn đã hoàn thành nhiệm vụ này!");
            return;
        }
        
        Intent intent = null;
        String type = item.getType();
        
        // Route dựa trên skill
        if (type.startsWith("reading")) {
            intent = new Intent(this, ReadingComprehensionActivity.class);
        } else if (type.startsWith("writing")) {
            intent = new Intent(this, WritingActivity.class);
        } else if (type.equals("listening_easy")) {
            intent = new Intent(this, ListeningListActivity.class);
            intent.putExtra("filter_difficulty", "EASY");
        } else if (type.equals("listening_medium")) {
            intent = new Intent(this, ListeningListActivity.class);
            intent.putExtra("filter_difficulty", "MEDIUM");
        } else if (type.equals("listening_hard")) {
            intent = new Intent(this, ListeningListActivity.class);
            intent.putExtra("filter_difficulty", "HARD");
        } else if (type.equals("listening_fill_blank")) {
            intent = new Intent(this, FillBlankLessonListActivity.class);
        } else if (type.equals("listening_exp")) {
            // Hiển thị danh sách tất cả listening
            intent = new Intent(this, ListeningListActivity.class);
        } else if (type.equals("listening")) {
            intent = new Intent(this, ListeningListActivity.class);
        } else if (type.startsWith("speaking")) {
            intent = new Intent(this, SpeakingActivity.class);
        } else if (type.equals("vocabulary")) {
            // Open dictionary or word list
            intent = new Intent(this, MainActivity.class);
        } else if (type.equals("flashcard")) {
            intent = new Intent(this, WishlistActivity.class);
        } else if (type.equals("quiz")) {
            intent = new Intent(this, ListeningListActivity.class);
        }
        
        if (intent != null) {
            // Lưu loại challenge đang thực hiện để kiểm tra khi quay lại
            intent.putExtra("from_daily_challenge", true);
            intent.putExtra("challenge_type", item.getType());
            intent.putExtra("challenge_xp", item.getXpReward());
            startActivityForResult(intent, 100);
        } else {
            showToast("Chức năng đang phát triển!");
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String challengeType = data.getStringExtra("challenge_type");
            int xpEarned = data.getIntExtra("xp_earned", 15);
            String lessonDifficulty = data.getStringExtra("lesson_difficulty");
            
            if (challengeType != null) {
                // Nếu là challenge listening với độ khó cụ thể, cập nhật challenge type tương ứng
                if (challengeType.equals("listening") || challengeType.equals("listening_exp")) {
                    if ("EASY".equals(lessonDifficulty)) {
                        markChallengeCompleted("listening_easy", xpEarned);
                    } else if ("MEDIUM".equals(lessonDifficulty)) {
                        markChallengeCompleted("listening_medium", xpEarned);
                    } else if ("HARD".equals(lessonDifficulty)) {
                        markChallengeCompleted("listening_hard", xpEarned);
                    }
                    // Cộng exp cho challenge listening_exp
                    markChallengeCompleted("listening_exp", xpEarned);
                } else {
                    markChallengeCompleted(challengeType, xpEarned);
                }
            }
        }
    }
    
    /**
     * Đánh dấu nhiệm vụ hoàn thành và cộng XP
     */
    private void markChallengeCompleted(String challengeType, int xpEarned) {
        executor.execute(() -> {
            // Cập nhật DailyChallenge trong database
            boolean updated = false;
            
            switch (challengeType) {
                case "reading":
                    todayChallenge.setReadingCompleted(true);
                    updated = true;
                    break;
                case "writing":
                    todayChallenge.setWritingCompleted(true);
                    updated = true;
                    break;
                case "listening":
                    todayChallenge.setListeningCompleted(true);
                    updated = true;
                    break;
                case "listening_easy":
                    todayChallenge.setListeningEasyCount(todayChallenge.getListeningEasyCount() + 1);
                    if (todayChallenge.getListeningEasyCount() >= 2) {
                        todayChallenge.setListeningCompleted(true);
                    }
                    updated = true;
                    break;
                case "listening_medium":
                    todayChallenge.setListeningMediumCount(todayChallenge.getListeningMediumCount() + 1);
                    updated = true;
                    break;
                case "listening_hard":
                    todayChallenge.setListeningHardCount(todayChallenge.getListeningHardCount() + 1);
                    updated = true;
                    break;
                case "listening_fill_blank":
                    todayChallenge.setListeningFillBlankCount(todayChallenge.getListeningFillBlankCount() + 1);
                    updated = true;
                    break;
                case "listening_exp":
                    int currentExp = todayChallenge.getListeningExpEarned();
                    todayChallenge.setListeningExpEarned(currentExp + xpEarned);
                    updated = true;
                    break;
                case "speaking":
                    todayChallenge.setSpeakingCompleted(true);
                    updated = true;
                    break;
                case "vocabulary":
                    todayChallenge.setLearnNewWordsCompleted(true);
                    todayChallenge.setNewWordsCount(5);
                    updated = true;
                    break;
                case "flashcard":
                    todayChallenge.setPracticeFlashcardCompleted(true);
                    todayChallenge.setFlashcardCount(10);
                    updated = true;
                    break;
                case "quiz":
                    todayChallenge.setCompleteQuizCompleted(true);
                    updated = true;
                    break;
            }
            
            if (updated) {
                challengeDao.update(todayChallenge);
                
                // Cộng XP cho user trong Firebase
                updateUserXP(xpEarned);
                
                runOnUiThread(() -> {
                    showToast("✅ Hoàn thành! +" + xpEarned + " XP");
                    loadTodayChallenge();
                });
            }
        });
    }
    
    /**
     * Cộng XP cho user trong Firebase
     */
    private void updateUserXP(int earnedXP) {
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long currentTotalXP = documentSnapshot.getLong("total_xp");
                        Long currentLevel = documentSnapshot.getLong("current_level");
                        Long currentLevelXP = documentSnapshot.getLong("current_level_xp");
                        Long xpToNextLevel = documentSnapshot.getLong("xp_to_next_level");
                        
                        int newTotalXP = (currentTotalXP != null) ? currentTotalXP.intValue() : 0;
                        int newLevel = (currentLevel != null) ? currentLevel.intValue() : 1;
                        int newLevelXP = (currentLevelXP != null) ? currentLevelXP.intValue() : 0;
                        int newNextLevelXP = (xpToNextLevel != null) ? xpToNextLevel.intValue() : 100;
                        
                        newTotalXP += earnedXP;
                        newLevelXP += earnedXP;
                        
                        boolean leveledUp = false;
                        while (newLevelXP >= newNextLevelXP) {
                            leveledUp = true;
                            newLevelXP -= newNextLevelXP;
                            newLevel++;
                            newNextLevelXP = 100 + (newLevel - 1) * 50;
                        }
                        
                        Map<String, Object> updates = new java.util.HashMap<>();
                        updates.put("total_xp", newTotalXP);
                        updates.put("current_level", newLevel);
                        updates.put("current_level_xp", newLevelXP);
                        updates.put("xp_to_next_level", newNextLevelXP);
                        
                        FirebaseFirestore.getInstance().collection("users").document(userId)
                                .update(updates);
                    }
                });
    }

    private void claimReward() {
        showToast("Reward claimed! +50 Bonus XP");
        btnClaimReward.setEnabled(false);
        btnClaimReward.setText("Claimed");
        
        // Cộng 50 XP bonus
        updateUserXP(50);
    }

    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh challenges when returning to activity
        loadTodayChallenge();
    }
}
