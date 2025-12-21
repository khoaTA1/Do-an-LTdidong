package vn.ltdidong.apphoctienganh.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Locale;

import io.noties.markwon.Markwon;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.api.GeminiApi;
import vn.ltdidong.apphoctienganh.models.GeminiRequest;
import vn.ltdidong.apphoctienganh.models.GeminiResponse;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;

public class SpeakingActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private TextView tvTargetText;
    private TextView tvResultText;
    private TextView tvScore;
    private Button btnListen;
    private ImageButton btnBack;
    private ImageView ivMic;
    private TextView tvHeader;
    private Button btnNextTopic;
    
    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private boolean isListening = false;

    private GeminiApi geminiApi;
    private static final String API_KEY = "AIzaSyDOJpBmNfXE6aWZGRrb8Dy9XlzED1_QQNY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speaking);

        tvTargetText = findViewById(R.id.tvTargetText);
        tvResultText = findViewById(R.id.tvResultText);
        tvScore = findViewById(R.id.tvScore);
        btnListen = findViewById(R.id.btnListen);
        btnBack = findViewById(R.id.btnBack);
        ivMic = findViewById(R.id.ivMic);
        tvHeader = findViewById(R.id.tvHeader);
        btnNextTopic = findViewById(R.id.btnNextTopic);

        // Initialize Gemini API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        geminiApi = retrofit.create(GeminiApi.class);

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(SpeakingActivity.this, "Language not supported", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SpeakingActivity.this, "Initialization failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                tvResultText.setText("Listening...");
                tvResultText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                tvResultText.setText("Processing...");
                ivMic.setImageResource(R.drawable.ic_speaking); // Reset icon
                isListening = false;
            }

            @Override
            public void onError(int error) {
                String errorMessage = getErrorText(error);
                tvResultText.setText(errorMessage);
                ivMic.setImageResource(R.drawable.ic_speaking); // Reset icon
                isListening = false;
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    tvResultText.setText(spokenText);
                    tvResultText.setTextColor(getResources().getColor(android.R.color.black));
                    gradeSpeaking(tvTargetText.getText().toString(), spokenText);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        btnListen.setOnClickListener(v -> speakOut());
        
        ivMic.setOnClickListener(v -> {
            if (checkPermission()) {
                if (!isListening) {
                    startListening();
                } else {
                    stopListening();
                }
            } else {
                requestPermission();
            }
        });

        btnBack.setOnClickListener(v -> finish());

        btnNextTopic.setOnClickListener(v -> {
            tvResultText.setText("...");
            tvScore.setText("");
            generateSpeakingTopic();
        });
        
        // Load initial topic
        generateSpeakingTopic();
    }

    private void startListening() {
        speechRecognizer.startListening(speechRecognizerIntent);
        isListening = true;
        ivMic.setImageResource(android.R.drawable.ic_btn_speak_now); // Change icon to indicate active state
        // Note: You might want to add a custom 'active' mic icon resource if 'ic_btn_speak_now' isn't what you want
    }

    private void stopListening() {
        speechRecognizer.stopListening();
        isListening = false;
        ivMic.setImageResource(R.drawable.ic_speaking);
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    private void generateSpeakingTopic() {
        tvHeader.setText("Generating topic...");
        tvTargetText.setText("Loading...");
        String prompt = "Give me a random, interesting short sentence for English speaking practice. " +
                "Topics can be about travel, food, work, hobbies, or daily life. " +
                "Ensure it is suitable for A2-B1 level. No extra text.";

        geminiApi.generateContent(API_KEY, new GeminiRequest(prompt)).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String topic = response.body().getOutputText();
                    tvTargetText.setText(topic.trim());
                    tvHeader.setText("Practice");
                } else {
                    Log.e("SpeakingActivity", "Gemini API Error: " + response.code() + " - " + response.message());
                    tvTargetText.setText("Error: " + response.code() + " Check API Key/Model.");
                    tvHeader.setText("Error");
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                Log.e("SpeakingActivity", "API Call Failed", t);
                tvTargetText.setText("Network Error: " + t.getMessage());
                tvHeader.setText("Error");
            }
        });
    }

    private void speakOut() {
        String text = tvTargetText.getText().toString();
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void gradeSpeaking(String target, String spoken) {
        // Removed ProgressDialog
        tvScore.setText("Evaluating...");
        tvScore.setTextColor(getResources().getColor(android.R.color.darker_gray));

        String prompt = "Act as an English teacher. Evaluate this speaking attempt.\n" +
                "Target sentence: \"" + target + "\"\n" +
                "Student said: \"" + spoken + "\"\n" +
                "Compare them carefully.\n" +
                "1. Score (0-10)\n" +
                "2. Pronunciation feedback (which words were wrong?)\n" +
                "3. Encourage the student.\n" +
                "Format clearly.";

        geminiApi.generateContent(API_KEY, new GeminiRequest(prompt)).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().getOutputText();
                    tvScore.setText(""); // Clear temporary status
                    showResultBottomSheet(result);
                    // Update XP when speaking is graded
                    updateUserXPForSpeaking(target, spoken);
                } else {
                    Log.e("SpeakingActivity", "Grade Error: " + response.code());
                    evaluateSpeechLocal(target, spoken);
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                Log.e("SpeakingActivity", "Grade Failure", t);
                evaluateSpeechLocal(target, spoken);
            }
        });
    }

    private void showResultBottomSheet(String markdownResult) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_result_sheet, null);
        bottomSheetDialog.setContentView(view);

        TextView tvContent = view.findViewById(R.id.tvMarkdownContent);
        Button btnKeepWriting = view.findViewById(R.id.btnKeepWriting);
        Button btnNewTopicSheet = view.findViewById(R.id.btnNewTopicSheet);
        
        // Rename buttons for context
        btnKeepWriting.setText("Try Again");
        btnNewTopicSheet.setText("New Sentence");

        Markwon markwon = Markwon.create(this);
        markwon.setMarkdown(tvContent, markdownResult);

        btnKeepWriting.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            startListening(); // Retry immediately
        });

        btnNewTopicSheet.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            tvResultText.setText("...");
            tvScore.setText("");
            generateSpeakingTopic();
        });

        bottomSheetDialog.show();
    }

    // Helper to normalize text for comparison (remove punctuation, extra spaces)
    private String normalizeText(String text) {
        if (text == null) return "";
        return text.replaceAll("[^a-zA-Z0-9\\s]", "") // Remove non-alphanumeric chars
                   .trim()
                   .replaceAll("\\s+", " "); // Normalize spaces
    }

    private void evaluateSpeechLocal(String target, String spokenText) {
        String normalizedTarget = normalizeText(target);
        String normalizedSpoken = normalizeText(spokenText);

        if (normalizedSpoken.equalsIgnoreCase(normalizedTarget)) {
            tvScore.setText("Excellent! Match.");
            tvScore.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (normalizedSpoken.toLowerCase().contains(normalizedTarget.toLowerCase())) {
             tvScore.setText("Good job!");
             tvScore.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        } else {
            tvScore.setText("Try again.");
            tvScore.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    /**
     * Cập nhật kinh nghiệm (XP) cho user khi hoàn thành speaking
     */
    private void updateUserXPForSpeaking(String target, String spoken) {
        String userId = SharedPreferencesManager.getInstance(this).getUserId();
        if (userId == null) return;
        
        // Tính XP dựa trên độ chính xác
        String normalizedTarget = normalizeText(target);
        String normalizedSpoken = normalizeText(spoken);
        
        int earnedXP = 0;
        if (normalizedSpoken.equalsIgnoreCase(normalizedTarget)) {
            earnedXP = 50; // Perfect match
        } else if (normalizedSpoken.toLowerCase().contains(normalizedTarget.toLowerCase())) {
            earnedXP = 30; // Partial match
        } else {
            // Calculate similarity (simple word matching)
            String[] targetWords = normalizedTarget.toLowerCase().split("\\s+");
            String[] spokenWords = normalizedSpoken.toLowerCase().split("\\s+");
            int matchCount = 0;
            for (String targetWord : targetWords) {
                for (String spokenWord : spokenWords) {
                    if (targetWord.equals(spokenWord)) {
                        matchCount++;
                        break;
                    }
                }
            }
            earnedXP = (int) ((matchCount * 1.0 / targetWords.length) * 20); // Up to 20 XP
        }
        
        final int finalEarnedXP = earnedXP;
        
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
                        newTotalXP += finalEarnedXP;
                        newLevelXP += finalEarnedXP;
                        
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
                                    if (finalEarnedXP > 0) {
                                        String xpMessage = "+" + finalEarnedXP + " XP earned!";
                                        if (finalLeveledUp) {
                                            xpMessage += "\n🎉 Level Up! You're now Level " + finalLevel + "!";
                                        }
                                        Toast.makeText(SpeakingActivity.this, xpMessage, Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                });
    }
    
    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
