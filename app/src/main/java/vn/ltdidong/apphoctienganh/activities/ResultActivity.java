package vn.ltdidong.apphoctienganh.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.ListeningLesson;
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
        
        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ListeningViewModel.class);
        
        // Lấy data từ Intent
        lessonId = getIntent().getIntExtra("lesson_id", -1);
        correctAnswers = getIntent().getIntExtra("correct_answers", 0);
        totalQuestions = getIntent().getIntExtra("total_questions", 0);
        score = getIntent().getFloatExtra("score", 0);
        
        // Initialize views
        initViews();
        
        // Load lesson data
        loadLessonData();
        
        // Display results
        displayResults();
        
        // Setup buttons
        setupButtons();
    }
    
    /**
     * Khởi tạo tất cả views
     */
    @SuppressLint("WrongViewCast")
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
        btnViewTranscript.setOnClickListener(v -> {
            if (lesson != null) {
                showTranscriptDialog();
            }
        });
        
        btnTryAgain.setOnClickListener(v -> {
            // Quay lại QuizActivity để làm lại
            Intent intent = new Intent(ResultActivity.this, QuizActivity.class);
            intent.putExtra("lesson_id", lessonId);
            startActivity(intent);
            finish();
        });
        
        btnBackToLessons.setOnClickListener(v -> {
            // Quay về ListeningListActivity
            Intent intent = new Intent(ResultActivity.this, ListeningListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
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
    
    @Override
    public void onBackPressed() {
        // Quay về ListeningListActivity
        Intent intent = new Intent(ResultActivity.this, ListeningListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
