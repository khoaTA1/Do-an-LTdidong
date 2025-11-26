package vn.ltdidong.apphoctienganh.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.ListeningLesson;
import vn.ltdidong.apphoctienganh.viewmodel.ListeningViewModel;

/**
 * Activity hiển thị kết quả quiz
 * Cho phép xem transcript, làm lại hoặc quay về danh sách bài học
 */
public class ResultActivity extends AppCompatActivity {
    
    private ListeningViewModel viewModel;
    private int lessonId;
    private int correctAnswers;
    private int totalQuestions;
    private float score;
    private ListeningLesson currentLesson;
    
    // UI Components
    private ImageView ivResultIcon;
    private TextView tvCongratulations;
    private TextView tvMessage;
    private TextView tvScorePercentage;
    private TextView tvCorrectAnswers;
    private Button btnViewTranscript;
    private Button btnTryAgain;
    private Button btnBackToLessons;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        
        // Get data từ Intent
        lessonId = getIntent().getIntExtra("LESSON_ID", -1);
        correctAnswers = getIntent().getIntExtra("CORRECT_ANSWERS", 0);
        totalQuestions = getIntent().getIntExtra("TOTAL_QUESTIONS", 0);
        score = getIntent().getFloatExtra("SCORE", 0f);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ListeningViewModel.class);
        
        // Initialize views
        initViews();
        
        // Display result
        displayResult();
        
        // Load lesson data
        loadLessonData();
        
        // Setup buttons
        setupButtons();
    }
    
    /**
     * Khởi tạo các views
     */
    private void initViews() {
        ivResultIcon = findViewById(R.id.ivResultIcon);
        tvCongratulations = findViewById(R.id.tvCongratulations);
        tvMessage = findViewById(R.id.tvMessage);
        tvScorePercentage = findViewById(R.id.tvScorePercentage);
        tvCorrectAnswers = findViewById(R.id.tvCorrectAnswers);
        btnViewTranscript = findViewById(R.id.btnViewTranscript);
        btnTryAgain = findViewById(R.id.btnTryAgain);
        btnBackToLessons = findViewById(R.id.btnBackToLessons);
    }
    
    /**
     * Hiển thị kết quả
     */
    private void displayResult() {
        // Hiển thị điểm số
        tvScorePercentage.setText(String.format("%.0f%%", score));
        tvCorrectAnswers.setText(getString(R.string.correct_answers, correctAnswers, totalQuestions));
        
        // Hiển thị message và icon dựa trên điểm số
        String message;
        int iconRes;
        
        if (score >= 90) {
            message = getString(R.string.excellent);
            iconRes = android.R.drawable.star_big_on;
        } else if (score >= 70) {
            message = getString(R.string.good_job);
            iconRes = android.R.drawable.star_big_on;
        } else if (score >= 50) {
            message = getString(R.string.keep_practicing);
            iconRes = android.R.drawable.star_big_off;
        } else {
            message = getString(R.string.try_harder);
            iconRes = android.R.drawable.star_big_off;
            tvCongratulations.setText("Keep Learning!");
        }
        
        tvMessage.setText(message);
        ivResultIcon.setImageResource(iconRes);
    }
    
    /**
     * Load lesson data để lấy transcript
     */
    private void loadLessonData() {
        viewModel.getLessonById(lessonId).observe(this, lesson -> {
            if (lesson != null) {
                currentLesson = lesson;
            }
        });
    }
    
    /**
     * Setup các buttons
     */
    private void setupButtons() {
        // View Transcript button
        btnViewTranscript.setOnClickListener(v -> {
            if (currentLesson != null) {
                showTranscriptDialog(currentLesson.getTranscript());
            }
        });
        
        // Try Again button - làm lại bài này
        btnTryAgain.setOnClickListener(v -> {
            Intent intent = new Intent(this, LessonDetailActivity.class);
            intent.putExtra("LESSON_ID", lessonId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        
        // Back to Lessons button - quay về danh sách
        btnBackToLessons.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListeningListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        
        // Handle back press - force user to use buttons
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Redirect to back to lessons button
                btnBackToLessons.performClick();
            }
        });
    }
    
    /**
     * Hiển thị dialog với transcript
     */
    private void showTranscriptDialog(String transcript) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_transcript);
        
        TextView tvTranscriptContent = dialog.findViewById(R.id.tvTranscriptContent);
        Button btnClose = dialog.findViewById(R.id.btnClose);
        
        tvTranscriptContent.setText(transcript);
        
        btnClose.setOnClickListener(v -> dialog.dismiss());
        
        // Set dialog size
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        
        dialog.show();
    }
}
