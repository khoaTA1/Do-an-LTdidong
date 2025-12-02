package vn.ltdidong.apphoctienganh.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

public class QuizActivity extends AppCompatActivity {

    private ListeningViewModel viewModel;
    private List<Question> questions;
    private Map<Integer, String> userAnswers = new HashMap<>();
    private int currentIndex = 0;
    private int lessonId;
    private ListeningLesson lesson;

    // UI
    private TextView tvNumber, tvText;
    private RadioButton rbA, rbB, rbC, rbD;
    private MaterialCardView cardA, cardB, cardC, cardD, cardQuestion;
    private MaterialButton btnNext;
    private ProgressBar progressBar;

    private final Handler handler = new Handler();
    private final int DELAY_NEXT = 1200; // 1.2s nhìn kết quả

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        lessonId = getIntent().getIntExtra("lesson_id", -1);
        if (lessonId == -1) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(ListeningViewModel.class);

        initViews();
        setupOptionClick();
        setupButtonEvents();
        loadData();
    }

    @SuppressLint("WrongViewCast")
    private void initViews() {
        tvNumber = findViewById(R.id.tvQuestionNumber);
        tvText = findViewById(R.id.tvQuestionText);

        rbA = findViewById(R.id.radioOptionA);
        rbB = findViewById(R.id.radioOptionB);
        rbC = findViewById(R.id.radioOptionC);
        rbD = findViewById(R.id.radioOptionD);

        btnNext = findViewById(R.id.btnNext);

        progressBar = findViewById(R.id.progressBarQuiz);

        cardQuestion = findViewById(R.id.cardQuestion);
        cardA = findViewById(R.id.cardOptionA);
        cardB = findViewById(R.id.cardOptionB);
        cardC = findViewById(R.id.cardOptionC);
        cardD = findViewById(R.id.cardOptionD);
    }

    private void loadData() {
        // Load lesson
        viewModel.getLessonById(lessonId).observe(this, l -> lesson = l);

        // Load questions
        viewModel.getQuestionsByLesson(lessonId).observe(this, list -> {
            if (list == null || list.isEmpty()) {
                Toast.makeText(this, "No questions found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            questions = list;
            progressBar.setMax(questions.size());
            showQuestion(0);
        });
    }

    private void setupOptionClick() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            rbA.setChecked(id == R.id.cardOptionA || id == R.id.radioOptionA);
            rbB.setChecked(id == R.id.cardOptionB || id == R.id.radioOptionB);
            rbC.setChecked(id == R.id.cardOptionC || id == R.id.radioOptionC);
            rbD.setChecked(id == R.id.cardOptionD || id == R.id.radioOptionD);
        };

        cardA.setOnClickListener(listener);
        cardB.setOnClickListener(listener);
        cardC.setOnClickListener(listener);
        cardD.setOnClickListener(listener);

        rbA.setOnClickListener(listener);
        rbB.setOnClickListener(listener);
        rbC.setOnClickListener(listener);
        rbD.setOnClickListener(listener);
    }

    private void setupButtonEvents() {
        btnNext.setOnClickListener(v -> {

            if (!rbA.isChecked() && !rbB.isChecked() && !rbC.isChecked() && !rbD.isChecked()) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }

            saveAnswer();
            showCheckColor();

            handler.postDelayed(() -> {

                if (currentIndex == questions.size() - 1)
                    submitQuiz();
                else
                    showQuestion(currentIndex + 1);

            }, DELAY_NEXT);
        });
    }

    private void showQuestion(int index) {
        currentIndex = index;

        Question q = questions.get(index);
        tvNumber.setText("Question " + (index + 1) + " of " + questions.size());
        tvText.setText(q.getQuestionText());
        progressBar.setProgress(index + 1);

        rbA.setText("A. " + q.getOptionA());
        rbB.setText("B. " + q.getOptionB());
        rbC.setText("C. " + q.getOptionC());
        rbD.setText("D. " + q.getOptionD());

        clearAllChecks();
        resetCardColor();

        // Load đáp án đã chọn (dùng index thay vì q.getId())
        if (userAnswers.containsKey(index)) {
            switch (userAnswers.get(index)) {
                case "A": rbA.setChecked(true); break;
                case "B": rbB.setChecked(true); break;
                case "C": rbC.setChecked(true); break;
                case "D": rbD.setChecked(true); break;
            }
        }

        btnNext.setText(index == questions.size() - 1 ? "Submit" : "Next");

        // animation
        cardQuestion.setAlpha(0);
        cardQuestion.animate().alpha(1).setDuration(300).start();
    }

    private void saveAnswer() {
        String ans = "";
        if (rbA.isChecked()) ans = "A";
        else if (rbB.isChecked()) ans = "B";
        else if (rbC.isChecked()) ans = "C";
        else if (rbD.isChecked()) ans = "D";

        if (!ans.isEmpty()) {
            // Dùng currentIndex thay vì q.getId() vì Firebase questions không có id
            userAnswers.put(currentIndex, ans);
        }
    }

    private void showCheckColor() {
        Question q = questions.get(currentIndex);
        String user = userAnswers.get(currentIndex);
        String correct = q.getCorrectAnswer();

        resetCardColor();

        int green = ContextCompat.getColor(this, android.R.color.holo_green_light);
        int red = ContextCompat.getColor(this, android.R.color.holo_red_light);

        // đúng
        highlightCard(correct, green);

        // sai
        if (!correct.equals(user)) highlightCard(user, red);
    }

    private void highlightCard(String option, int color) {
        if (option == null) return;
        switch (option) {
            case "A": cardA.setCardBackgroundColor(color); break;
            case "B": cardB.setCardBackgroundColor(color); break;
            case "C": cardC.setCardBackgroundColor(color); break;
            case "D": cardD.setCardBackgroundColor(color); break;
        }
    }

    private void resetCardColor() {
        int white = ContextCompat.getColor(this, android.R.color.white);
        cardA.setCardBackgroundColor(white);
        cardB.setCardBackgroundColor(white);
        cardC.setCardBackgroundColor(white);
        cardD.setCardBackgroundColor(white);
    }

    private void clearAllChecks() {
        rbA.setChecked(false);
        rbB.setChecked(false);
        rbC.setChecked(false);
        rbD.setChecked(false);
    }

    private void submitQuiz() {
        int correctCount = 0;

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            String user = userAnswers.get(i);
            if (user != null && user.equals(q.getCorrectAnswer())) correctCount++;
        }

        float score = (correctCount * 100f) / questions.size();

        UserProgress p = new UserProgress();
        p.setLessonId(lessonId);
        p.setCorrectAnswers(correctCount);
        p.setTotalQuestions(questions.size());
        p.setScore(score);
        p.setStatus("COMPLETED");
        p.setCompletedAt(System.currentTimeMillis());

        viewModel.saveProgress(p);

        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra("lesson_id", lessonId);
        i.putExtra("correct_answers", correctCount);
        i.putExtra("total_questions", questions.size());
        i.putExtra("score", score);
        startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Quiz")
                .setMessage("Are you sure you want to exit? Your progress will not be saved.")
                .setPositiveButton("Exit", (d, w) -> super.onBackPressed())
                .setNegativeButton("Cancel", null)
                .show();
    }
}
