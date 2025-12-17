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
import vn.ltdidong.apphoctienganh.models.ChallengeItem;
import vn.ltdidong.apphoctienganh.models.DailyChallenge;

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
        // Update progress
        todayChallenge.calculateProgress();
        todayChallenge.calculateXP();
        
        int progress = todayChallenge.getProgressPercentage();
        progressBar.setProgress(progress);
        tvProgress.setText(todayChallenge.getCompletedChallenges() + "/" + todayChallenge.getTotalChallenges());
        tvXP.setText(todayChallenge.getXpEarned() + " XP");

        // Update challenge list
        challengeItems.clear();
        challengeItems.add(new ChallengeItem(
            "Complete Writing Exercise",
            "Practice your writing skills",
            10,
            "writing",
            todayChallenge.isWritingCompleted()
        ));
        challengeItems.add(new ChallengeItem(
            "Complete Listening Exercise",
            "Practice your listening skills",
            10,
            "listening",
            todayChallenge.isListeningCompleted()
        ));
        challengeItems.add(new ChallengeItem(
            "Complete Speaking Exercise",
            "Practice your speaking skills",
            10,
            "speaking",
            todayChallenge.isSpeakingCompleted()
        ));
        challengeItems.add(new ChallengeItem(
            "Learn 5 New Words",
            todayChallenge.getNewWordsCount() + "/5 words learned",
            10,
            "vocabulary",
            todayChallenge.isLearnNewWordsCompleted()
        ));
        challengeItems.add(new ChallengeItem(
            "Practice 10 Flashcards",
            todayChallenge.getFlashcardCount() + "/10 cards reviewed",
            10,
            "flashcard",
            todayChallenge.isPracticeFlashcardCompleted()
        ));
        challengeItems.add(new ChallengeItem(
            "Complete 1 Quiz",
            "Test your knowledge",
            10,
            "quiz",
            todayChallenge.isCompleteQuizCompleted()
        ));

        adapter.notifyDataSetChanged();

        // Show reward card if all completed
        if (todayChallenge.isAllCompleted()) {
            cardReward.setVisibility(View.VISIBLE);
            tvRewardMessage.setText("🎉 Congratulations! You've completed all challenges today!");
        } else {
            cardReward.setVisibility(View.GONE);
        }
    }

    private void onChallengeClick(ChallengeItem item) {
        Intent intent = null;
        
        switch (item.getType()) {
            case "reading":
                intent = new Intent(this, ReadingComprehensionActivity.class);
                break;
            case "writing":
                intent = new Intent(this, WritingActivity.class);
                break;
            case "listening":
                intent = new Intent(this, ListeningListActivity.class);
                break;
            case "speaking":
                intent = new Intent(this, SpeakingActivity.class);
                break;
            case "vocabulary":
                // Open dictionary or word list
                intent = new Intent(this, MainActivity.class);
                break;
            case "flashcard":
                intent = new Intent(this, WishlistActivity.class);
                break;
            case "quiz":
                intent = new Intent(this, ListeningListActivity.class);
                break;
        }
        
        if (intent != null) {
            startActivity(intent);
        }
    }

    private void claimReward() {
        showToast("Reward claimed! +50 Bonus XP");
        btnClaimReward.setEnabled(false);
        btnClaimReward.setText("Claimed");
        
        // TODO: Update user XP in database
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
