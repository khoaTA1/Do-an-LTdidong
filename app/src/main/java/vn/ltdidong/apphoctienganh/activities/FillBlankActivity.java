package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.FillBlankQuestion;
import vn.ltdidong.apphoctienganh.models.ListeningLesson;
import vn.ltdidong.apphoctienganh.repositories.FillBlankRepository;

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
    private LinearProgressIndicator progressIndicator;

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

    // Repository
    private FillBlankRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_blank);
        
        // Initialize repository
        repository = new FillBlankRepository();

        initViews();
        loadData();
        // setupMediaPlayer() will be called after data is loaded

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
        progressIndicator = findViewById(R.id.progressIndicator);

        // Click listeners
        btnPlay.setOnClickListener(v -> togglePlayPause());
        btnReplay.setOnClickListener(v -> replayQuestion());
        btnCheck.setOnClickListener(v -> checkAnswer());
        btnNext.setOnClickListener(v -> nextQuestion());
        btnSubmit.setOnClickListener(v -> showSubmitDialog());

        findViewById(R.id.btnBack).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void loadData() {
        // Get lesson info from Intent (new approach - from lesson list)
        String lessonId = getIntent().getStringExtra("lesson_id");
        String lessonTitle = getIntent().getStringExtra("lesson_title");

        if (lessonId != null) {
            // New approach: Load từ FillBlankLessonListActivity
            tvLessonTitle.setText(lessonTitle != null ? lessonTitle : "Fill in the Blanks");
            
            // Load questions from Firebase using String ID
            // Mỗi câu hỏi sẽ có audioUrl riêng
            loadQuestionsFromFirebaseByStringId(lessonId);
        } else {
            // Old approach: Check for ListeningLesson object (backward compatibility)
            lesson = (ListeningLesson) getIntent().getSerializableExtra("lesson");

            if (lesson != null) {
                tvLessonTitle.setText(lesson.getTitle());
                // Load questions from Firebase using int ID
                loadQuestionsFromFirebase(lesson.getId());
            } else {
                // If no lesson provided, use default title and sample data
                tvLessonTitle.setText("Fill in the Blanks");
                questions = createSampleQuestions();
                displayQuestion();
            }
        }
    }

    /**Sử dụng Repository pattern giống như mode Listening cơ bản
     * @param lessonId ID của bài học (String format - document ID)
     */
    private void loadQuestionsFromFirebaseByStringId(String lessonId) {
        progressBar.setVisibility(View.VISIBLE);

        // Load trực tiếp từ document ID
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("fill_blank_lesson_listening")
                .document(lessonId)
                .collection("questions")
                .orderBy("orderIndex")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    if (!querySnapshot.isEmpty()) {
                        questions = new ArrayList<>();
                        
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            FillBlankQuestion q = new FillBlankQuestion();
                            q.setLessonId(0); // String ID không cần convert
                            q.setSentenceWithBlanks(doc.getString("sentenceWithBlanks"));
                            q.setCorrectAnswers(doc.getString("correctAnswers"));
                            q.setHint(doc.getString("hint"));
                            q.setAudioUrl(doc.getString("audioUrl"));
                            
                            // Get orderIndex as number from Firebase
                            Long orderIndexLong = doc.getLong("orderIndex");
                            q.setOrderIndex(orderIndexLong != null ? orderIndexLong.intValue() : 0);
                            
                            questions.add(q);
                        }
                        
                        Log.d("FillBlank", "Loaded " + questions.size() + " questions from Firebase");
                    } else {
                        Log.w("FillBlank", "No questions found for lesson " + lessonId);
                        Toast.makeText(this, "Chưa có câu hỏi cho bài học này", Toast.LENGTH_SHORT).show();
                        questions = createSampleQuestions();
                    }

                    // Không setup media player ở đây nữa, sẽ setup khi displayQuestion
                    displayQuestion();
                })
                .addOnFailureListener(e -> {
                    Log.e("FillBlank", "Error loading questions", e);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tải câu hỏi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    questions = createSampleQuestions();
                    // Không setup media player ở đây nữa
                    displayQuestion();
                });
    }

    /**Sử dụng Repository pattern giống như mode Listening cơ bản
     * @param lessonId ID của bài học
     */
    private void loadQuestionsFromFirebase(int lessonId) {
        progressBar.setVisibility(View.VISIBLE);

        // Sử dụng Repository để load dữ liệu
        repository.getFillBlankQuestionsByLesson(lessonId).observe(this, loadedQuestions -> {
            progressBar.setVisibility(View.GONE);

            if (loadedQuestions != null && !loadedQuestions.isEmpty()) {
                // Load thành công từ Firebase
                questions = loadedQuestions;
                Log.d("FillBlank", "Loaded " + questions.size() + " questions from Firebase");
            } else {
                // Không có dữ liệu trên Firebase -> dùng dữ liệu mẫu
                Log.w("FillBlank", "No questions found on Firebase for lesson " + lessonId + ". Using sample data.");
                Toast.makeText(this, "Chưa có dữ liệu trên Firebase. Dùng dữ liệu mẫu.", 
                    Toast.LENGTH_SHORT).show();
                questions = createSampleQuestions();
            }

            // Không setup media player ở đây nữa, sẽ setup khi displayQuestion
            // Hiển thị câu hỏi đầu tiên
            displayQuestion();
        });
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

        FillBlankQuestion q2 = new FillBlankQuestion();
        q2.setLessonId(lessonId);
        q2.setSentenceWithBlanks("First, I {blank} and take a shower.");
        q2.setCorrectAnswers("brush my teeth");
        q2.setHint("What do you do first in the morning?");
        q2.setOrderIndex(2);

        FillBlankQuestion q3 = new FillBlankQuestion();
        q3.setLessonId(lessonId);
        q3.setSentenceWithBlanks("Then I have {blank} with my family.");
        q3.setCorrectAnswers("breakfast");
        q3.setHint("What meal do you eat in the morning?");
        q3.setOrderIndex(3);

        list.add(q1);
        list.add(q2);
        list.add(q3);

        return list;
    }

    private void setupMediaPlayerForCurrentQuestion() {
        // Release previous media player if exists
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
        }
        
        // Reset UI
        btnPlay.setImageResource(R.drawable.ic_play);
        seekBarAudio.setProgress(0);
        
        // Get audio URL from current question
        String audioUrl = (currentQuestion != null) ? currentQuestion.getAudioUrl() : null;
        
        // Skip media player setup if no audio URL
        if (audioUrl == null || audioUrl.isEmpty()) {
            Log.w("FillBlank", "No audio URL for question " + (currentIndex + 1));
            progressBar.setVisibility(View.GONE);
            btnPlay.setEnabled(false);
            btnReplay.setEnabled(false);
            seekBarAudio.setEnabled(false);
            return;
        }

        try {
            mediaPlayer = new MediaPlayer();

            // Handle different audio URL formats
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
                Log.d("FillBlank", "Loading audio from URL: " + audioUrl);
                mediaPlayer.setDataSource(audioUrl);
            }

            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                Log.d("FillBlank", "Audio prepared successfully for question " + (currentIndex + 1));
                progressBar.setVisibility(View.GONE);
                btnPlay.setEnabled(true);
                btnReplay.setEnabled(true);
                seekBarAudio.setEnabled(true);
                seekBarAudio.setMax(mediaPlayer.getDuration());
                updateSeekBar();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                btnPlay.setImageResource(R.drawable.ic_play);
            });
            
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("FillBlank", "MediaPlayer error: what=" + what + ", extra=" + extra);
                Toast.makeText(this, "Lỗi phát audio", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                btnPlay.setEnabled(false);
                btnReplay.setEnabled(false);
                return true;
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
            Log.e("FillBlank", "Error setting up media player", e);
            Toast.makeText(this, "Không thể tải audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            btnPlay.setEnabled(false);
            btnReplay.setEnabled(false);
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
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(0); // Restart from beginning
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

        // Setup media player cho câu hỏi hiện tại (mỗi câu có audio riêng)
        setupMediaPlayerForCurrentQuestion();

        // Update UI
        tvQuestionNumber.setText("Câu " + (currentIndex + 1) + "/" + questions.size());
        
        // Update progress indicator
        if (progressIndicator != null && questions.size() > 0) {
            int progress = (int) (((currentIndex + 1) * 100.0) / questions.size());
            progressIndicator.setProgress(progress);
        }

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

        // Prepare detailed results as JSON string
        org.json.JSONArray resultsArray = new org.json.JSONArray();
        try {
            Log.d("FillBlank", "Creating detailed results for " + questions.size() + " questions");
            
            for (int i = 0; i < questions.size(); i++) {
                FillBlankQuestion q = questions.get(i);
                org.json.JSONObject questionResult = new org.json.JSONObject();
                questionResult.put("sentence", q.getSentenceWithBlanks());
                
                // Convert correctAnswers List to JSONArray manually
                org.json.JSONArray correctAnswersArray = new org.json.JSONArray();
                List<String> correctAnswers = q.getCorrectAnswersList();
                if (correctAnswers != null) {
                    for (String answer : correctAnswers) {
                        correctAnswersArray.put(answer);
                    }
                }
                questionResult.put("correctAnswers", correctAnswersArray);
                
                // Convert userAnswers to JSONArray
                org.json.JSONArray userAnswersArray = new org.json.JSONArray();
                List<String> userAns = userAnswers.get(i);
                if (userAns != null) {
                    for (String answer : userAns) {
                        userAnswersArray.put(answer);
                    }
                }
                questionResult.put("userAnswers", userAnswersArray);
                
                resultsArray.put(questionResult);
                Log.d("FillBlank", "Added question " + (i + 1) + " to results");
            }
            
            Log.d("FillBlank", "Final results JSON: " + resultsArray.toString());
        } catch (org.json.JSONException e) {
            Log.e("FillBlank", "Error creating JSON results", e);
            e.printStackTrace();
        }

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
        intent.putExtra("detailed_results", resultsArray.toString());
        Log.d("FillBlank", "Starting ResultActivity with detailed_results: " + resultsArray.toString());
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
