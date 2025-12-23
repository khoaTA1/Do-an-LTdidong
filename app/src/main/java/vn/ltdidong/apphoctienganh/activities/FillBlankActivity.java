package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.FillBlankQuestion;
import vn.ltdidong.apphoctienganh.models.ListeningLesson;

/**
 * Activity cho bài tập Fill-in-the-blank
 * Người dùng nghe audio và điền từ còn thiếu vào chỗ trống
 */
public class FillBlankActivity extends AppCompatActivity {

    // UI Components
    private TextView tvLessonTitle, tvQuestionNumber, tvSentence, tvHint;
    private LinearLayout llInputFields;
    private Button btnCheck, btnNext, btnSubmit;
    private ImageButton btnPlay, btnReplay;
    private SeekBar seekBarAudio;
    private ProgressBar progressBar;
    private MaterialCardView cardHint;

    // Data
    private ListeningLesson lesson;
    private List<FillBlankQuestion> questions;
    private int currentIndex = 0;
    private FillBlankQuestion currentQuestion;

    // User answers
    private Map<Integer, List<String>> userAnswers = new HashMap<>();

    // Media Player
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_blank);

        initViews();
        loadData();
        setupMediaPlayer();
        displayQuestion();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(FillBlankActivity.this)
                        .setTitle("Thoát bài tập?")
                        .setMessage("Tiến độ của bạn sẽ không được lưu.")
                        .setPositiveButton("Thoát", (dialog, which) -> {
                            setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
    }

    private void initViews() {
        tvLessonTitle = findViewById(R.id.tvLessonTitle);
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvSentence = findViewById(R.id.tvSentence);
        tvHint = findViewById(R.id.tvHint);
        llInputFields = findViewById(R.id.llInputFields);

        btnCheck = findViewById(R.id.btnCheck);
        btnNext = findViewById(R.id.btnNext);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnPlay = findViewById(R.id.btnPlay);
        btnReplay = findViewById(R.id.btnReplay);

        seekBarAudio = findViewById(R.id.seekBarAudio);
        progressBar = findViewById(R.id.progressBar);
        cardHint = findViewById(R.id.cardHint);

        // Click listeners
        btnPlay.setOnClickListener(v -> togglePlayPause());
        btnReplay.setOnClickListener(v -> replayQuestion());
        btnCheck.setOnClickListener(v -> checkAnswer());
        btnNext.setOnClickListener(v -> nextQuestion());
        btnSubmit.setOnClickListener(v -> showSubmitDialog());

        findViewById(R.id.btnBack).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void loadData() {
        // Get lesson from Intent
        lesson = (ListeningLesson) getIntent().getSerializableExtra("lesson");

        // TODO: Load fill-blank questions from Firebase
        // Tạm thời dùng dữ liệu mẫu
        questions = createSampleQuestions();

        if (lesson != null) {
            tvLessonTitle.setText(lesson.getTitle());
        } else {
            // If no lesson provided, use default title
            tvLessonTitle.setText("Fill in the Blanks");
        }
    }

    private List<FillBlankQuestion> createSampleQuestions() {
        List<FillBlankQuestion> list = new ArrayList<>();

        // Use lesson ID if available, otherwise use default ID (0)
        int lessonId = (lesson != null) ? lesson.getId() : 0;

        FillBlankQuestion q1 = new FillBlankQuestion();
        q1.setLessonId(lessonId);
        q1.setSentenceWithBlanks("I wake up at {blank} every day.");
        q1.setCorrectAnswers("7 AM");
        q1.setHint("What time? (Format: number + AM/PM)");
        q1.setOrderIndex(1);
        q1.setAudioTimestamp(0);

        FillBlankQuestion q2 = new FillBlankQuestion();
        q2.setLessonId(lessonId);
        q2.setSentenceWithBlanks("First, I {blank} and take a shower.");
        q2.setCorrectAnswers("brush my teeth");
        q2.setHint("What do you do first in the morning?");
        q2.setOrderIndex(2);
        q2.setAudioTimestamp(5);

        FillBlankQuestion q3 = new FillBlankQuestion();
        q3.setLessonId(lessonId);
        q3.setSentenceWithBlanks("Then I have {blank} with my family.");
        q3.setCorrectAnswers("breakfast");
        q3.setHint("What meal do you eat in the morning?");
        q3.setOrderIndex(3);
        q3.setAudioTimestamp(10);

        list.add(q1);
        list.add(q2);
        list.add(q3);

        return list;
    }

    private void setupMediaPlayer() {
        // Skip media player setup if no lesson with audio is provided
        if (lesson == null || lesson.getAudioUrl() == null || lesson.getAudioUrl().isEmpty()) {
            progressBar.setVisibility(View.GONE);
            btnPlay.setEnabled(false);
            btnReplay.setEnabled(false);
            seekBarAudio.setEnabled(false);
            return;
        }

        try {
            mediaPlayer = new MediaPlayer();

            // Handle different audio URL formats
            String audioUrl = lesson.getAudioUrl();
            if (audioUrl.startsWith("raw://")) {
                // Local resource
                String resourceName = audioUrl.substring(6);
                int resId = getResources().getIdentifier(resourceName, "raw", getPackageName());
                if (resId != 0) {
                    mediaPlayer.setDataSource(this,
                            android.net.Uri.parse("android.resource://" + getPackageName() + "/" + resId));
                }
            } else {
                // URL from Firebase or web
                mediaPlayer.setDataSource(audioUrl);
            }

            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                progressBar.setVisibility(View.GONE);
                seekBarAudio.setMax(mediaPlayer.getDuration());
                updateSeekBar();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                btnPlay.setImageResource(R.drawable.ic_play);
            });

            seekBarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && mediaPlayer != null) {
                        mediaPlayer.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

        } catch (IOException e) {
            Toast.makeText(this, "Không thể tải audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSeekBar() {
        if (mediaPlayer != null && isPlaying) {
            seekBarAudio.setProgress(mediaPlayer.getCurrentPosition());
            handler.postDelayed(this::updateSeekBar, 100);
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer == null)
            return;

        if (isPlaying) {
            mediaPlayer.pause();
            btnPlay.setImageResource(R.drawable.ic_play);
        } else {
            mediaPlayer.start();
            btnPlay.setImageResource(R.drawable.ic_pause);
            updateSeekBar();
        }
        isPlaying = !isPlaying;
    }

    private void replayQuestion() {
        if (mediaPlayer != null && currentQuestion != null) {
            int timestamp = currentQuestion.getAudioTimestamp() * 1000; // Convert to milliseconds
            mediaPlayer.seekTo(timestamp);
            if (!isPlaying) {
                togglePlayPause();
            }
        }
    }

    private void displayQuestion() {
        if (currentIndex >= questions.size()) {
            return;
        }

        currentQuestion = questions.get(currentIndex);

        // Update UI
        tvQuestionNumber.setText("Câu " + (currentIndex + 1) + "/" + questions.size());

        // Display sentence with blanks replaced by underscores
        String displayText = currentQuestion.getSentenceWithBlanks().replace("{blank}", "______");
        tvSentence.setText(displayText);

        // Show/hide hint
        if (currentQuestion.getHint() != null && !currentQuestion.getHint().isEmpty()) {
            tvHint.setText("💡 Gợi ý: " + currentQuestion.getHint());
            cardHint.setVisibility(View.VISIBLE);
        } else {
            cardHint.setVisibility(View.GONE);
        }

        // Create input fields
        createInputFields();

        // Restore previous answers if any
        if (userAnswers.containsKey(currentIndex)) {
            List<String> savedAnswers = userAnswers.get(currentIndex);
            for (int i = 0; i < savedAnswers.size() && i < llInputFields.getChildCount(); i++) {
                EditText et = (EditText) llInputFields.getChildAt(i);
                et.setText(savedAnswers.get(i));
            }
        }

        // Update button visibility
        btnCheck.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.GONE);
        btnSubmit.setVisibility(currentIndex == questions.size() - 1 ? View.VISIBLE : View.GONE);

        // Replay audio for this question
        replayQuestion();
    }

    private void createInputFields() {
        llInputFields.removeAllViews();

        int blankCount = currentQuestion.getBlankCount();
        for (int i = 0; i < blankCount; i++) {
            EditText editText = new EditText(this);
            editText.setHint("Điền từ " + (i + 1));
            editText.setBackgroundResource(R.drawable.bg_edit_text);
            editText.setPadding(32, 24, 32, 24);
            editText.setTextSize(16);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 24);
            editText.setLayoutParams(params);

            llInputFields.addView(editText);
        }
    }

    private void checkAnswer() {
        // Get user answers from input fields
        List<String> answers = new ArrayList<>();
        for (int i = 0; i < llInputFields.getChildCount(); i++) {
            EditText et = (EditText) llInputFields.getChildAt(i);
            String answer = et.getText().toString().trim();

            if (TextUtils.isEmpty(answer)) {
                Toast.makeText(this, "Vui lòng điền tất cả các ô trống!", Toast.LENGTH_SHORT).show();
                return;
            }
            answers.add(answer);
        }

        // Save answers
        userAnswers.put(currentIndex, answers);

        // Check correctness
        int correctCount = currentQuestion.checkAnswers(answers);
        int totalBlanks = currentQuestion.getBlankCount();

        // Show feedback
        showFeedback(correctCount, totalBlanks);

        // Update UI
        btnCheck.setVisibility(View.GONE);
        if (currentIndex < questions.size() - 1) {
            btnNext.setVisibility(View.VISIBLE);
        }
    }

    private void showFeedback(int correct, int total) {
        String message;
        String correctSentence = currentQuestion.getCorrectSentence();

        if (correct == total) {
            message = "✅ Chính xác! Tuyệt vời!\n\nĐáp án: " + correctSentence;
        } else {
            message = "❌ Sai rồi! Bạn đúng " + correct + "/" + total + " chỗ trống.\n\nĐáp án đúng: " + correctSentence;
        }

        new AlertDialog.Builder(this)
                .setTitle(correct == total ? "Đúng rồi!" : "Chưa đúng")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void nextQuestion() {
        currentIndex++;
        if (currentIndex < questions.size()) {
            displayQuestion();
        } else {
            // Finished all questions
            showResults();
        }
    }

    private void showSubmitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Nộp bài")
                .setMessage("Bạn có chắc chắn muốn nộp bài không?")
                .setPositiveButton("Nộp bài", (dialog, which) -> showResults())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showResults() {
        // Calculate total score
        int totalCorrect = 0;
        int totalBlanks = 0;

        for (int i = 0; i < questions.size(); i++) {
            FillBlankQuestion q = questions.get(i);
            totalBlanks += q.getBlankCount();

            if (userAnswers.containsKey(i)) {
                totalCorrect += q.checkAnswers(userAnswers.get(i));
            }
        }

        float score = totalBlanks > 0 ? (totalCorrect * 100f / totalBlanks) : 0;

        // Navigate to result screen
        Intent intent = new Intent(this, ResultActivity.class);
        // Use lesson ID if available, otherwise use default 0
        int lessonId = (lesson != null) ? lesson.getId() : 0;
        String lessonTitle = (lesson != null) ? lesson.getTitle() : "Fill in the Blanks";

        intent.putExtra("lesson_id", lessonId);
        intent.putExtra("lesson_title", lessonTitle);
        intent.putExtra("correct_answers", totalCorrect);
        intent.putExtra("total_questions", totalBlanks);
        intent.putExtra("score", score);
        intent.putExtra("quiz_type", "fill_blank");
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }
}
