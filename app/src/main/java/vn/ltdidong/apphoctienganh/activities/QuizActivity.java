package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.ListeningLesson;
import vn.ltdidong.apphoctienganh.models.Question;
import vn.ltdidong.apphoctienganh.models.UserProgress;
import vn.ltdidong.apphoctienganh.viewmodel.ListeningViewModel;

/**
 * Activity để hiển thị và xử lý quiz
 * Hiển thị từng câu hỏi một, cho phép người dùng chọn đáp án
 * Tính điểm và lưu kết quả vào database
 */
public class QuizActivity extends AppCompatActivity {
    
    private ListeningViewModel viewModel;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private Map<Integer, String> userAnswers = new HashMap<>();
    private int lessonId;
    private ListeningLesson lesson;
    
    // UI components
    private TextView tvQuestionNumber;
    private TextView tvQuestionText;
    private RadioGroup radioGroupOptions;
    private RadioButton rbOptionA;
    private RadioButton rbOptionB;
    private RadioButton rbOptionC;
    private RadioButton rbOptionD;
    private MaterialButton btnPrevious;
    private MaterialButton btnNext;
    private MaterialButton btnSubmit;
    private ProgressBar progressBar;
    private TextView tvProgress;
    private MaterialCardView cardQuestion;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        
        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ListeningViewModel.class);
        
        // Lấy lesson ID từ Intent
        lessonId = getIntent().getIntExtra("lesson_id", -1);
        if (lessonId == -1) {
            Toast.makeText(this, "Error loading quiz", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize views
        initViews();
        
        // Load questions
        loadQuestions();
        
        // Setup buttons
        setupButtons();
    }
    
    /**
     * Khởi tạo tất cả views
     */
    private void initViews() {
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        radioGroupOptions = findViewById(R.id.radioGroupOptions);
        rbOptionA = findViewById(R.id.radioOptionA);
        rbOptionB = findViewById(R.id.radioOptionB);
        rbOptionC = findViewById(R.id.radioOptionC);
        rbOptionD = findViewById(R.id.radioOptionD);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnSubmit = btnNext;
        progressBar = findViewById(R.id.progressBarQuiz);
        tvProgress = null;
        cardQuestion = findViewById(R.id.cardQuestion);
    }
    
    /**
     * Load câu hỏi từ database
     */
    private void loadQuestions() {
        // Load lesson info
        viewModel.getLessonById(lessonId).observe(this, loadedLesson -> {
            lesson = loadedLesson;
        });
        
        // Load questions
        viewModel.getQuestionsByLesson(lessonId).observe(this, loadedQuestions -> {
            if (loadedQuestions != null && !loadedQuestions.isEmpty()) {
                questions = loadedQuestions;
                progressBar.setMax(questions.size());
                displayQuestion(0);
            } else {
                Toast.makeText(this, "No questions available", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    /**
     * Hiển thị câu hỏi tại index
     */
    private void displayQuestion(int index) {
        if (questions == null || index < 0 || index >= questions.size()) {
            return;
        }
        
        currentQuestionIndex = index;
        Question question = questions.get(index);
        
        // Update question number và progress
        tvQuestionNumber.setText("Question " + (index + 1) + " of " + questions.size());
        if (tvProgress != null) {
            tvProgress.setText((index + 1) + "/" + questions.size());
        }
        progressBar.setProgress(index + 1);
        
        // Display question text
        tvQuestionText.setText(question.getQuestionText());
        
        // Display options
        rbOptionA.setText("A. " + question.getOptionA());
        rbOptionB.setText("B. " + question.getOptionB());
        rbOptionC.setText("C. " + question.getOptionC());
        rbOptionD.setText("D. " + question.getOptionD());
        
        // Clear selection
        radioGroupOptions.clearCheck();
        
        // Load previous answer nếu có
        String previousAnswer = userAnswers.get(question.getId());
        if (previousAnswer != null) {
            switch (previousAnswer) {
                case "A":
                    rbOptionA.setChecked(true);
                    break;
                case "B":
                    rbOptionB.setChecked(true);
                    break;
                case "C":
                    rbOptionC.setChecked(true);
                    break;
                case "D":
                    rbOptionD.setChecked(true);
                    break;
            }
        }
        
        // Update buttons visibility
        updateButtonsVisibility();
        
        // Animation
        cardQuestion.setAlpha(0f);
        cardQuestion.animate().alpha(1f).setDuration(300).start();
    }
    
    /**
     * Update visibility của Previous, Next và Submit buttons
     */
    private void updateButtonsVisibility() {
        // Previous button
        if (currentQuestionIndex == 0) {
            btnPrevious.setVisibility(View.INVISIBLE);
        } else {
            btnPrevious.setVisibility(View.VISIBLE);
        }
        
        // Next and Submit buttons
        if (currentQuestionIndex == questions.size() - 1) {
            btnNext.setText("Submit");
        } else {
            btnNext.setText("Next");
        }
    }
    
    /**
     * Lưu câu trả lời hiện tại
     */
    private void saveCurrentAnswer() {
        int selectedId = radioGroupOptions.getCheckedRadioButtonId();
        if (selectedId == -1) return;
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        String answer = "";
        
        if (selectedId == R.id.radioOptionA) {
            answer = "A";
        } else if (selectedId == R.id.radioOptionB) {
            answer = "B";
        } else if (selectedId == R.id.radioOptionC) {
            answer = "C";
        } else if (selectedId == R.id.radioOptionD) {
            answer = "D";
        }
        
        userAnswers.put(currentQuestion.getId(), answer);
    }
    
    /**
     * Setup các buttons
     */
    private void setupButtons() {
        btnPrevious.setOnClickListener(v -> {
            saveCurrentAnswer();
            if (currentQuestionIndex > 0) {
                displayQuestion(currentQuestionIndex - 1);
            }
        });
        
        btnNext.setOnClickListener(v -> {
            if (radioGroupOptions.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }
            
            saveCurrentAnswer();
            
            if (currentQuestionIndex < questions.size() - 1) {
                displayQuestion(currentQuestionIndex + 1);
            } else {
                // Last question - submit quiz
                if (userAnswers.size() < questions.size()) {
                    Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
                    return;
                }
                calculateAndSaveResults();
            }
        });
        
        // btnSubmit is same as btnNext, handle submit in btnNext listener
    }
    
    /**
     * Tính điểm và lưu kết quả
     */
    private void calculateAndSaveResults() {
        int correctAnswers = 0;
        
        // Đếm số câu trả lời đúng
        for (Question question : questions) {
            String userAnswer = userAnswers.get(question.getId());
            if (userAnswer != null && userAnswer.equals(question.getCorrectAnswer())) {
                correctAnswers++;
            }
        }
        
        // Tính điểm phần trăm
        float score = (correctAnswers * 100.0f) / questions.size();
        
        // Tạo UserProgress object
        UserProgress progress = new UserProgress();
        progress.setLessonId(lessonId);
        progress.setCorrectAnswers(correctAnswers);
        progress.setTotalQuestions(questions.size());
        progress.setScore(score);
        progress.setStatus("COMPLETED");
        progress.setCompletedAt(System.currentTimeMillis());
        
        // Lưu vào database
        viewModel.saveProgress(progress);
        
        // Chuyển sang ResultActivity
        Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
        intent.putExtra("lesson_id", lessonId);
        intent.putExtra("correct_answers", correctAnswers);
        intent.putExtra("total_questions", questions.size());
        intent.putExtra("score", score);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        // Hiển thị dialog xác nhận thoát
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Exit Quiz")
                .setMessage("Are you sure you want to exit? Your progress will not be saved.")
                .setPositiveButton("Exit", (dialog, which) -> {
                    super.onBackPressed();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
