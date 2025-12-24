package vn.ltdidong.apphoctienganh.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.api.AiService;
import vn.ltdidong.apphoctienganh.models.GroqChatCompletionResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import io.noties.markwon.Markwon;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;
import vn.ltdidong.apphoctienganh.managers.SkillManager;
import vn.ltdidong.apphoctienganh.database.AppDatabase;
import vn.ltdidong.apphoctienganh.database.DailyChallengeDao;
import vn.ltdidong.apphoctienganh.models.DailyChallenge;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WritingActivity extends AppCompatActivity {

    private TextView tvTopic, tvWordCount;
    private EditText etWritingArea;
    private Button btnNewTopic, btnSubmit;
    private ImageButton btnBack;

    private AiService aiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing);

        aiService = AiService.getInstance();

        // 2. Ánh xạ View
        tvTopic = findViewById(R.id.tvTopic);
        tvWordCount = findViewById(R.id.tvWordCount);
        etWritingArea = findViewById(R.id.etWritingArea);
        btnNewTopic = findViewById(R.id.btnNewTopic);
        btnSubmit = findViewById(R.id.btnSubmit); // Nút Grade mới
        btnBack = findViewById(R.id.btnBack);

        // 3. Tạo đề bài ngay khi vào
        generateAiTopic();

        // 4. Các sự kiện
        btnBack.setOnClickListener(v -> finish());
        btnNewTopic.setOnClickListener(v -> generateAiTopic());

        etWritingArea.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateWordCount(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // SỰ KIỆN CHẤM ĐIỂM
        btnSubmit.setOnClickListener(v -> {
            String userText = etWritingArea.getText().toString();
            String currentTopic = tvTopic.getText().toString();

            if (userText.split("\\s+").length < 5) {
                Toast.makeText(this, "Please write at least 5 words!", Toast.LENGTH_SHORT).show();
            } else {
                gradeWriting(currentTopic, userText);
            }
        });
    }

    // --- HÀM 1: DÙNG AI TẠO ĐỀ BÀI ---
    private void generateAiTopic() {
        tvTopic.setText("Generating topic...");

        // 1. Get current skill score to determine difficulty
        String userId = SharedPreferencesManager.getInstance(this).getUserId();
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        double writingScore = 5.0; // Default
                        if (documentSnapshot.exists()) {
                            java.util.Map<String, Object> data = documentSnapshot.getData();
                            if (data != null && data.containsKey("skill_scores")) {
                                java.util.Map<String, Double> scores = (java.util.Map<String, Double>) data
                                        .get("skill_scores");
                                if (scores != null && scores.containsKey(SkillManager.SKILL_WRITING)) {
                                    writingScore = scores.get(SkillManager.SKILL_WRITING);
                                }
                            }
                        }
                        requestTopicFromAi(writingScore);
                    })
                    .addOnFailureListener(e -> requestTopicFromAi(5.0));
        } else {
            requestTopicFromAi(5.0);
        }
    }

    private void requestTopicFromAi(double score) {
        String level;
        String difficultyNote;

        if (score < 4.0) {
            level = "beginner (A1-A2)";
            difficultyNote = "Level: Beginner (A1-A2)";
        } else if (score < 7.0) {
            level = "intermediate (B1-B2)";
            difficultyNote = "Level: Intermediate (B1-B2)";
        } else {
            level = "advanced (C1-C2)";
            difficultyNote = "Level: Advanced (C1-C2)";
        }

        // Display difficulty note
        Toast.makeText(this, difficultyNote, Toast.LENGTH_SHORT).show();

        // Prompt (Câu lệnh) gửi cho AI
        String prompt = "Give me 1 interesting English writing topic for " + level + " learners. " +
                "Short, clear, no extra text. Just the topic.";

        aiService.generateText(prompt).enqueue(new Callback<GroqChatCompletionResponse>() {
            @Override
            public void onResponse(Call<GroqChatCompletionResponse> call,
                    Response<GroqChatCompletionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String topic = response.body().getOutputText();
                    tvTopic.setText(topic.trim() + "\n(" + difficultyNote + ")");
                } else {
                    tvTopic.setText("Describe your favorite hobby.\n(Fallback Topic)"); // Fallback nếu lỗi
                }
            }

            @Override
            public void onFailure(Call<GroqChatCompletionResponse> call, Throwable t) {
                tvTopic.setText("Error loading topic. Describe your family.");
            }
        });
    }

    // --- HÀM 2: DÙNG AI CHẤM ĐIỂM ---
    private void gradeWriting(String topic, String text) {
        // Hiện loading
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("AI is reading your essay...");
        progressDialog.show();

        // Prompt chi tiết để AI chấm điểm
        String prompt = "Act as an English teacher. Evaluate this writing.\n" +
                "Topic: " + topic + "\n" +
                "Student's writing: \"" + text + "\"\n" +
                "Please provide:\n" +
                "1. Score (0-10)\n" +
                "2. Corrected version (fix grammar/vocab)\n" +
                "3. Short feedback.\n" +
                "Format clearly.";

        aiService.generateText(prompt).enqueue(new Callback<GroqChatCompletionResponse>() {
            @Override
            public void onResponse(Call<GroqChatCompletionResponse> call,
                    Response<GroqChatCompletionResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().getOutputText();
                    showResultBottomSheet(result);
                    // Update XP when writing is graded
                    updateUserXPForWriting(text.split("\\s+").length);

                    // Parse score and update Skill Score
                    double score = parseScoreFromAiResponse(result);
                    if (score >= 0) {
                        updateWritingSkillScore(score);
                    }
                } else {
                    Toast.makeText(WritingActivity.this, "AI Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GroqChatCompletionResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(WritingActivity.this, "Connection failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double parseScoreFromAiResponse(String responseText) {
        // Simple regex to find "Score: 7/10" or "Score: 7.5"
        // Adjust regex based on expected AI format from the prompt: "1. Score (0-10)"
        try {
            Pattern pattern = Pattern.compile("Score\\s*[:|-]?\\s*(\\d+(\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(responseText);
            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // Not found
    }

    private void updateWritingSkillScore(double lessonScore) {
        String userId = SharedPreferencesManager.getInstance(this).getUserId();
        if (userId == null)
            return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get current scores map
                        java.util.Map<String, Object> data = documentSnapshot.getData();
                        java.util.Map<String, Double> skillScores = (java.util.Map<String, Double>) data
                                .get("skill_scores");
                        java.util.Map<String, Long> lastPracticeTime = (java.util.Map<String, Long>) data
                                .get("last_practice_time");

                        if (skillScores == null)
                            skillScores = new java.util.HashMap<>();
                        if (lastPracticeTime == null)
                            lastPracticeTime = new java.util.HashMap<>();

                        // Initialize if missing
                        if (!skillScores.containsKey(SkillManager.SKILL_WRITING)) {
                            skillScores.put(SkillManager.SKILL_WRITING, 5.0);
                        }

                        // Get current score
                        Object currentObj = skillScores.get(SkillManager.SKILL_WRITING);
                        double currentScore = 5.0;
                        if (currentObj instanceof Number) {
                            currentScore = ((Number) currentObj).doubleValue();
                        }

                        // 1. Apply Decay first
                        long lastTime = 0;
                        if (lastPracticeTime.containsKey(SkillManager.SKILL_WRITING)) {
                            lastTime = lastPracticeTime.get(SkillManager.SKILL_WRITING);
                        }
                        double decayedScore = SkillManager.applyTimeDecay(currentScore, lastTime);

                        // 2. Calculate New Score
                        double newScore = SkillManager.calculateNewScore(decayedScore, lessonScore);

                        // 3. Update Map
                        skillScores.put(SkillManager.SKILL_WRITING, newScore);
                        lastPracticeTime.put(SkillManager.SKILL_WRITING, System.currentTimeMillis());

                        // 4. Save to Firestore
                        final double finalCurrentScore = currentScore;
                        final double finalNewScore = newScore;

                        db.collection("users").document(userId)
                                .update(
                                        "skill_scores", skillScores,
                                        "last_practice_time", lastPracticeTime)
                                .addOnSuccessListener(aVoid -> {
                                    String msg = String.format("Writing Score: %.1f -> %.1f", finalCurrentScore,
                                            finalNewScore);
                                    Toast.makeText(WritingActivity.this, msg, Toast.LENGTH_LONG).show();
                                })
                                .addOnFailureListener(e -> {
                                    android.util.Log.e("WritingActivity", "Error updating skill score", e);
                                });
                    }
                });
    }

    private void showResultBottomSheet(String markdownResult) {
        // 1. Tạo BottomSheet Dialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        // 2. Nạp giao diện từ file XML vừa tạo
        View view = getLayoutInflater().inflate(R.layout.layout_result_sheet, null);
        bottomSheetDialog.setContentView(view);

        // 3. Ánh xạ các view trong BottomSheet
        TextView tvContent = view.findViewById(R.id.tvMarkdownContent);
        Button btnKeepWriting = view.findViewById(R.id.btnKeepWriting);
        Button btnNewTopicSheet = view.findViewById(R.id.btnNewTopicSheet);

        // 4. Dùng Markwon để render Markdown đẹp lung linh
        Markwon markwon = Markwon.create(this);
        markwon.setMarkdown(tvContent, markdownResult);

        // 5. Xử lý sự kiện nút bấm
        btnKeepWriting.setOnClickListener(v -> {
            bottomSheetDialog.dismiss(); // Đóng dialog để viết tiếp
        });

        btnNewTopicSheet.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            generateAiTopic(); // Gọi hàm tạo đề mới
            etWritingArea.setText(""); // Xóa bài cũ
        });

        // 6. Hiển thị lên
        bottomSheetDialog.show();
    }

    private void updateWordCount(String text) {
        if (text.trim().isEmpty()) {
            tvWordCount.setText("0 words");
            return;
        }
        String[] words = text.trim().split("\\s+");
        tvWordCount.setText(words.length + " words");
    }

    /**
     * Cập nhật kinh nghiệm (XP) cho user khi hoàn thành writing
     */
    private void updateUserXPForWriting(int wordCount) {
        String userId = SharedPreferencesManager.getInstance(this).getUserId();
        if (userId == null)
            return;

        // Tính XP dựa trên số từ viết được
        // 5 XP cho mỗi 10 từ, tối thiểu 20 XP, tối đa 100 XP
        int earnedXP = Math.max(20, Math.min(100, (wordCount / 10) * 5));

        // Bonus nếu viết trên 100 từ
        if (wordCount >= 100) {
            earnedXP += 30;
        }

        final int finalXP = earnedXP;

        // Lấy thông tin user từ Firebase và cập nhật XP
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
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
                        newTotalXP += finalXP;
                        newLevelXP += finalXP;

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

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    String xpMessage = "+" + finalXP + " XP earned! (" + wordCount + " words)";
                                    if (finalLeveledUp) {
                                        xpMessage += "\n🎉 Level Up! You're now Level " + finalLevel + "!";
                                    }
                                    Toast.makeText(WritingActivity.this, xpMessage, Toast.LENGTH_LONG).show();
                                })
                                .addOnFailureListener(e -> {
                                    android.util.Log.e("WritingActivity", "Failed to update XP", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("WritingActivity", "Failed to get user data", e);
                });

        markDailyChallengeCompleted();
    }

    private void markDailyChallengeCompleted() {
        String userId = SharedPreferencesManager.getInstance(this).getUserId();
        if (userId == null)
            return;

        // Use today's date
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date());

        DailyChallengeDao dao = AppDatabase.getDatabase(getApplicationContext()).dailyChallengeDao();

        // Run on background thread
        new Thread(() -> {
            DailyChallenge challenge = dao.getChallengeByDate(userId, today);
            if (challenge != null) {
                if (!challenge.isWritingCompleted()) {
                    challenge.setWritingCompleted(true);
                    challenge.calculateProgress();
                    challenge.calculateXP();
                    dao.update(challenge);

                    runOnUiThread(() -> Toast.makeText(this, "Daily Challenge: Writing Completed!", Toast.LENGTH_SHORT)
                            .show());
                }
            } else {
                // Create new if not exists (though typically created on app start)
                DailyChallenge newChallenge = new DailyChallenge();
                newChallenge.setUserId(userId);
                newChallenge.setDate(today);
                newChallenge.setWritingCompleted(true);
                newChallenge.calculateProgress();
                newChallenge.calculateXP();
                dao.insert(newChallenge);

                runOnUiThread(
                        () -> Toast.makeText(this, "Daily Challenge: Writing Completed!", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
