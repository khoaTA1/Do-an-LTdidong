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
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.api.GeminiApi;
import vn.ltdidong.apphoctienganh.models.GeminiRequest;
import vn.ltdidong.apphoctienganh.models.GeminiResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import io.noties.markwon.Markwon;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;

public class WritingActivity extends AppCompatActivity {

    private TextView tvTopic, tvWordCount;
    private EditText etWritingArea;
    private Button btnNewTopic, btnSubmit;
    private ImageButton btnBack;

    private GeminiApi geminiApi;
    private static final String API_KEY = "AIzaSyDOJpBmNfXE6aWZGRrb8Dy9XlzED1_QQNY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing);

        // 1. Cấu hình Retrofit cho Gemini
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        geminiApi = retrofit.create(GeminiApi.class);

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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateWordCount(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
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

        // Prompt (Câu lệnh) gửi cho AI
        String prompt = "Give me 1 interesting English writing topic for intermediate learners. " +
                "Short, clear, no extra text. Just the topic.";

        geminiApi.generateContent(API_KEY, new GeminiRequest(prompt)).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String topic = response.body().getOutputText();
                    tvTopic.setText(topic.trim());
                } else {
                    tvTopic.setText("Describe your favorite hobby."); // Fallback nếu lỗi
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
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

        geminiApi.generateContent(API_KEY, new GeminiRequest(prompt)).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().getOutputText();
                    showResultBottomSheet(result);
                    // Update XP when writing is graded
                    updateUserXPForWriting(text.split("\\s+").length);
                } else {
                    Toast.makeText(WritingActivity.this, "AI Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(WritingActivity.this, "Connection failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
     * Cập nhật kinh nghiệm (XP) cho user khi hoàn thành bài viết
     */
    private void updateUserXPForWriting(int wordCount) {
        String userId = SharedPreferencesManager.getInstance(this).getUserId();
        if (userId == null) return;
        
        // Tính XP dựa trên số từ đã viết
        // 1 XP cho mỗi từ, tối đa 100 XP, bonus 20 XP nếu viết đủ 50 từ
        final int earnedXP = Math.min(wordCount, 100) + (wordCount >= 50 ? 20 : 0);
        
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
                        newTotalXP += earnedXP;
                        newLevelXP += earnedXP;
                        
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
                                    String xpMessage = "+" + earnedXP + " XP earned!";
                                    if (finalLeveledUp) {
                                        xpMessage += "\n🎉 Level Up! You're now Level " + finalLevel + "!";
                                    }
                                    Toast.makeText(WritingActivity.this, xpMessage, Toast.LENGTH_LONG).show();
                                });
                    }
                });
    }
}