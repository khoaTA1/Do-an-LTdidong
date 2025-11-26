package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.Question;
import vn.ltdidong.apphoctienganh.models.UserProgress;
import vn.ltdidong.apphoctienganh.viewmodel.ListeningViewModel;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity để làm bài quiz listening
 * Hiển thị từng câu hỏi và cho phép người dùng chọn đáp án
 */
public class QuizActivity extends AppCompatActivity {
    
    private ListeningViewModel viewModel;
    private int lessonId;
    private List<Question> questions = new ArrayList<>();
    private Map<Integer, String> userAnswers = new HashMap<>(); // questionId -> selectedAnswer
    
    private int currentQuestionIndex = 0;
    
    // UI Components
    private TextView tvQuestionNumber;
    private ProgressBar progressBarQuiz;
    private TextView tvQuestionText;
    private RadioGroup radioGroupOptions;
    private RadioButton radioOptionA;
    private RadioButton radioOptionB;
    private RadioButton radioOptionC;
    private RadioButton radioOptionD;
    private MaterialCardView cardOptionA;
    private MaterialCardView cardOptionB;
    private MaterialCardView cardOptionC;
    private MaterialCardView cardOptionD;
    private Button btnPrevious;
    private Button btnNext;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        
        // Get lesson ID
        lessonId = getIntent().getIntExtra("LESSON_ID", -1);
        if (lessonId == -1) {
            Toast.makeText(this, "Error: Lesson not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ListeningViewModel.class);
        
        // Initialize views
        initViews();
        
        // Setup toolbar
        setupToolbar();
        
        // Load questions
        loadQuestions();
        
        // Setup buttons
        setupButtons();
        
        // Handle back press
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Confirm trước khi thoát
                new androidx.appcompat.app.AlertDialog.Builder(QuizActivity.this)
                    .setTitle("Exit Quiz")
                    .setMessage("Are you sure you want to exit? Your progress will be lost.")
                    .setPositiveButton("Exit", (dialog, which) -> finish())
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });
    }
    
    /**
     * Khởi tạo các views
     */
    private void initViews() {
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        progressBarQuiz = findViewById(R.id.progressBarQuiz);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        radioGroupOptions = findViewById(R.id.radioGroupOptions);
        radioOptionA = findViewById(R.id.radioOptionA);
        radioOptionB = findViewById(R.id.radioOptionB);
        radioOptionC = findViewById(R.id.radioOptionC);
        radioOptionD = findViewById(R.id.radioOptionD);
        cardOptionA = findViewById(R.id.cardOptionA);
        cardOptionB = findViewById(R.id.cardOptionB);
        cardOptionC = findViewById(R.id.cardOptionC);
        cardOptionD = findViewById(R.id.cardOptionD);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
    }
    
    /**
     * Setup toolbar
     */
    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    /**
     * Load câu hỏi từ database
     */
    private void loadQuestions() {
        viewModel.getQuestionsByLesson(lessonId).observe(this, questionList -> {
            if (questionList != null && !questionList.isEmpty()) {
                questions = questionList;
                progressBarQuiz.setMax(questions.size());
                displayQuestion(0);
            } else {
                Toast.makeText(this, "No questions available", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    /**
     * Hiển thị câu hỏi theo index
     */
    private void displayQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;
        
        currentQuestionIndex = index;
        Question question = questions.get(index);
        
        // Update question number và progress
        tvQuestionNumber.setText(getString(R.string.question_number, index + 1, questions.size()));
        progressBarQuiz.setProgress(index + 1);
        
        // Hiển thị câu hỏi
        tvQuestionText.setText(question.getQuestionText());
        
        // Hiển thị các options
        radioOptionA.setText(String.format("A. %s", question.getOptionA()));
        radioOptionB.setText(String.format("B. %s", question.getOptionB()));
        radioOptionC.setText(String.format("C. %s", question.getOptionC()));
        radioOptionD.setText(String.format("D. %s", question.getOptionD()));
        
        // Clear selection và reset card colors
        radioGroupOptions.clearCheck();
        resetCardColors();
        
        // Restore previous answer nếu có
        String previousAnswer = userAnswers.get(question.getId());
        if (previousAnswer != null) {
            selectOption(previousAnswer);
        }
        
        // Update button states
        updateButtonStates();
    }
    
    /**
     * Setup các buttons
     */
    private void setupButtons() {
        // Previous button
        btnPrevious.setOnClickListener(v -> {
            saveCurrentAnswer();
            if (currentQuestionIndex > 0) {
                displayQuestion(currentQuestionIndex - 1);
            }
        });
        
        // Next/Submit button
        btnNext.setOnClickListener(v -> {
            if (!saveCurrentAnswer()) {
                Toast.makeText(this, getString(R.string.select_answer), Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (currentQuestionIndex < questions.size() - 1) {
                // Chuyển sang câu tiếp theo
                displayQuestion(currentQuestionIndex + 1);
            } else {
                // Submit quiz
                submitQuiz();
            }
        });
        
        // Setup option card clicks
        setupOptionCardClicks();
    }
    
    /**
     * Setup click listeners cho các option cards
     */
    private void setupOptionCardClicks() {
        cardOptionA.setOnClickListener(v -> radioOptionA.setChecked(true));
        cardOptionB.setOnClickListener(v -> radioOptionB.setChecked(true));
        cardOptionC.setOnClickListener(v -> radioOptionC.setChecked(true));
        cardOptionD.setOnClickListener(v -> radioOptionD.setChecked(true));
        
        // Update card color khi select
        radioGroupOptions.setOnCheckedChangeListener((group, checkedId) -> {
            resetCardColors();
            MaterialCardView selectedCard = null;
            
            if (checkedId == R.id.radioOptionA) selectedCard = cardOptionA;
            else if (checkedId == R.id.radioOptionB) selectedCard = cardOptionB;
            else if (checkedId == R.id.radioOptionC) selectedCard = cardOptionC;
            else if (checkedId == R.id.radioOptionD) selectedCard = cardOptionD;
            
            if (selectedCard != null) {
                selectedCard.setStrokeColor(getColor(R.color.primary));
                selectedCard.setStrokeWidth(4);
            }
        });
    }
    
    /**
     * Reset màu của tất cả option cards
     */
    private void resetCardColors() {
        cardOptionA.setStrokeColor(getColor(R.color.transparent));
        cardOptionB.setStrokeColor(getColor(R.color.transparent));
        cardOptionC.setStrokeColor(getColor(R.color.transparent));
        cardOptionD.setStrokeColor(getColor(R.color.transparent));
    }
    
    /**
     * Lưu đáp án hiện tại
     * @return true nếu có chọn đáp án, false nếu chưa chọn
     */
    private boolean saveCurrentAnswer() {
        int selectedId = radioGroupOptions.getCheckedRadioButtonId();
        if (selectedId == -1) {
            return false; // Chưa chọn đáp án
        }
        
        String answer = "";
        if (selectedId == R.id.radioOptionA) answer = "A";
        else if (selectedId == R.id.radioOptionB) answer = "B";
        else if (selectedId == R.id.radioOptionC) answer = "C";
        else if (selectedId == R.id.radioOptionD) answer = "D";
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        userAnswers.put(currentQuestion.getId(), answer);
        
        return true;
    }
    
    /**
     * Select option theo key
     */
    private void selectOption(String optionKey) {
        switch (optionKey.toUpperCase()) {
            case "A":
                radioOptionA.setChecked(true);
                break;
            case "B":
                radioOptionB.setChecked(true);
                break;
            case "C":
                radioOptionC.setChecked(true);
                break;
            case "D":
                radioOptionD.setChecked(true);
                break;
        }
    }
    
    /**
     * Update trạng thái các buttons
     */
    private void updateButtonStates() {
        // Previous button
        btnPrevious.setEnabled(currentQuestionIndex > 0);
        
        // Next/Submit button
        if (currentQuestionIndex == questions.size() - 1) {
            btnNext.setText(getString(R.string.submit_quiz));
        } else {
            btnNext.setText(getString(R.string.next_question));
        }
    }
    
    /**
     * Submit quiz và tính điểm
     */
    private void submitQuiz() {
        // Tính số câu đúng
        int correctCount = 0;
        for (Question question : questions) {
            String userAnswer = userAnswers.get(question.getId());
            if (userAnswer != null && userAnswer.equals(question.getCorrectAnswer())) {
                correctCount++;
            }
        }
        
        int totalQuestions = questions.size();
        float score = (correctCount * 100.0f) / totalQuestions;
        
        // Lưu progress vào database
        UserProgress progress = new UserProgress();
        progress.setLessonId(lessonId);
        progress.setCorrectAnswers(correctCount);
        progress.setTotalQuestions(totalQuestions);
        progress.setScore(score);
        progress.setStatus("COMPLETED");
        progress.setCompletedAt(System.currentTimeMillis());
        
        viewModel.saveProgress(progress);
        
        // Chuyển sang ResultActivity
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("LESSON_ID", lessonId);
        intent.putExtra("CORRECT_ANSWERS", correctCount);
        intent.putExtra("TOTAL_QUESTIONS", totalQuestions);
        intent.putExtra("SCORE", score);
        startActivity(intent);
        finish();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
