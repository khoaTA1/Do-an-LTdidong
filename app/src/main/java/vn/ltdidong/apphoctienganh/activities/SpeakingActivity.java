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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.noties.markwon.Markwon;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.api.AiService;
import vn.ltdidong.apphoctienganh.database.AppDatabase;
import vn.ltdidong.apphoctienganh.database.DailyChallengeDao;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;
import vn.ltdidong.apphoctienganh.managers.SkillManager;
import vn.ltdidong.apphoctienganh.models.DailyChallenge;
import vn.ltdidong.apphoctienganh.models.GroqChatCompletionResponse;

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

    private AiService aiService;

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

        aiService = AiService.getInstance();

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

        // Get current skill score to determine difficulty
        String userId = SharedPreferencesManager.getInstance(this).getUserId();
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        double speakingScore = 5.0; // Default
                        if (documentSnapshot.exists()) {
                            java.util.Map<String, Object> data = documentSnapshot.getData();
                            if (data != null) {
                                // Get score
                                if (data.containsKey("skill_scores")) {
                                    java.util.Map<String, Double> scores = (java.util.Map<String, Double>) data.get("skill_scores");
                                    if (scores != null && scores.containsKey(SkillManager.SKILL_SPEAKING)) {
                                        Object scoreObj = scores.get(SkillManager.SKILL_SPEAKING);
                                        if (scoreObj instanceof Number) {
                                            speakingScore = ((Number) scoreObj).doubleValue();
                                        }
                                    }
                                }

                                // Apply Time Decay before requesting topic
                                if (data.containsKey("last_practice_time")) {
                                    java.util.Map<String, Long> lastTimes = (java.util.Map<String, Long>) data.get("last_practice_time");
                                    if (lastTimes != null && lastTimes.containsKey(SkillManager.SKILL_SPEAKING)) {
                                        long lastTime = lastTimes.get(SkillManager.SKILL_SPEAKING);
                                        double originalScore = speakingScore;
                                        speakingScore = SkillManager.applyTimeDecay(speakingScore, lastTime);
                                        
                                        // If score decayed, update it in Firebase immediately so UI is consistent
                                        if (speakingScore < originalScore) {
                                            updateDecayedScore(userId, speakingScore);
                                        }
                                    }
                                }
                            }
                        }
                        requestTopicFromAi(speakingScore);
                    })
                    .addOnFailureListener(e -> requestTopicFromAi(5.0));
        } else {
            requestTopicFromAi(5.0);
        }
    }

    private void updateDecayedScore(String userId, double decayedScore) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                java.util.Map<String, Object> data = snapshot.getData();
                if (data != null && data.containsKey("skill_scores")) {
                    java.util.Map<String, Double> skillScores = (java.util.Map<String, Double>) data.get("skill_scores");
                    if (skillScores != null) {
                        skillScores.put(SkillManager.SKILL_SPEAKING, decayedScore);
                        db.collection("users").document(userId).update("skill_scores", skillScores);
                    }
                }
            }
        });
    }

    private void requestTopicFromAi(double score) {
        String level;
        if (score < 4.0) {
            level = "beginner (A1-A2)";
        } else if (score < 7.0) {
            level = "intermediate (B1-B2)";
        } else {
            level = "advanced (C1-C2)";
        }

        String prompt = "Give me a random, interesting short sentence for English speaking practice. " +
                "Level: " + level + ". No extra text. Just the sentence.";

        aiService.generateText(prompt).enqueue(new Callback<GroqChatCompletionResponse>() {
            @Override
            public void onResponse(Call<GroqChatCompletionResponse> call, Response<GroqChatCompletionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String topic = response.body().getOutputText();
                    tvTargetText.setText(topic.trim());
                    tvHeader.setText("Practice (" + level + ")");
                } else {
                    tvTargetText.setText("I love learning English every day.");
                    tvHeader.setText("Practice");
                }
            }

            @Override
            public void onFailure(Call<GroqChatCompletionResponse> call, Throwable t) {
                tvTargetText.setText("The weather is very nice today.");
                tvHeader.setText("Practice");
            }
        });
    }

    private void speakOut() {
        String text = tvTargetText.getText().toString();
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void gradeSpeaking(String target, String spoken) {
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

        aiService.generateText(prompt).enqueue(new Callback<GroqChatCompletionResponse>() {
            @Override
            public void onResponse(Call<GroqChatCompletionResponse> call, Response<GroqChatCompletionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().getOutputText();
                    tvScore.setText(""); 
                    showResultBottomSheet(result);
                    updateUserXPForSpeaking(target, spoken);

                    // Parse score and update Skill Score
                    double score = parseScoreFromAiResponse(result);
                    if (score >= 0) {
                        updateSpeakingSkillScore(score);
                    }
                } else {
                    evaluateSpeechLocal(target, spoken);
                }
            }

            @Override
            public void onFailure(Call<GroqChatCompletionResponse> call, Throwable t) {
                evaluateSpeechLocal(target, spoken);
            }
        });
    }

    private double parseScoreFromAiResponse(String responseText) {
        try {
            Pattern pattern = Pattern.compile("Score\\s*[:|-]?\\s*(\\d+(\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(responseText);
            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void updateSpeakingSkillScore(double lessonScore) {
        String userId = SharedPreferencesManager.getInstance(this).getUserId();
        if (userId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        java.util.Map<String, Object> data = documentSnapshot.getData();
                        java.util.Map<String, Double> skillScores = (java.util.Map<String, Double>) data.get("skill_scores");
                        java.util.Map<String, Long> lastPracticeTime = (java.util.Map<String, Long>) data.get("last_practice_time");

                        if (skillScores == null) skillScores = new java.util.HashMap<>();
                        if (lastPracticeTime == null) lastPracticeTime = new java.util.HashMap<>();

                        if (!skillScores.containsKey(SkillManager.SKILL_SPEAKING)) {
                            skillScores.put(SkillManager.SKILL_SPEAKING, 5.0);
                        }

                        Object currentObj = skillScores.get(SkillManager.SKILL_SPEAKING);
                        double currentScore = 5.0;
                        if (currentObj instanceof Number) {
                            currentScore = ((Number) currentObj).doubleValue();
                        }

                        long lastTime = 0;
                        if (lastPracticeTime.containsKey(SkillManager.SKILL_SPEAKING)) {
                            lastTime = lastPracticeTime.get(SkillManager.SKILL_SPEAKING);
                        }
                        double decayedScore = SkillManager.applyTimeDecay(currentScore, lastTime);
                        double newScore = SkillManager.calculateNewScore(decayedScore, lessonScore);

                        skillScores.put(SkillManager.SKILL_SPEAKING, newScore);
                        lastPracticeTime.put(SkillManager.SKILL_SPEAKING, System.currentTimeMillis());

                        final double finalCurrentScore = currentScore;
                        final double finalNewScore = newScore;

                        db.collection("users").document(userId)
                                .update("skill_scores", skillScores, "last_practice_time", lastPracticeTime)
                                .addOnSuccessListener(aVoid -> {
                                    String msg = String.format("Speaking Score: %.1f -> %.1f", finalCurrentScore, finalNewScore);
                                    Toast.makeText(SpeakingActivity.this, msg, Toast.LENGTH_LONG).show();
                                });
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
        
        btnKeepWriting.setText("Try Again");
        btnNewTopicSheet.setText("New Sentence");

        Markwon markwon = Markwon.create(this);
        markwon.setMarkdown(tvContent, markdownResult);

        btnKeepWriting.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            startListening(); 
        });

        btnNewTopicSheet.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            tvResultText.setText("...");
            tvScore.setText("");
            generateSpeakingTopic();
        });

        bottomSheetDialog.show();
    }

    private String normalizeText(String text) {
        if (text == null) return "";
        return text.replaceAll("[^a-zA-Z0-9\\s]", "").trim().replaceAll("\\s+", " ");
    }

    private void evaluateSpeechLocal(String target, String spokenText) {
        String normalizedTarget = normalizeText(target);
        String normalizedSpoken = normalizeText(spokenText);

        if (normalizedSpoken.equalsIgnoreCase(normalizedTarget)) {
            tvScore.setText("Excellent! Match.");
            tvScore.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            updateSpeakingSkillScore(10.0);
        } else if (normalizedSpoken.toLowerCase().contains(normalizedTarget.toLowerCase())) {
             tvScore.setText("Good job!");
             tvScore.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
             updateSpeakingSkillScore(7.0);
        } else {
            tvScore.setText("Try again.");
            tvScore.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            updateSpeakingSkillScore(3.0);
        }
    }

    private void updateUserXPForSpeaking(String target, String spoken) {
        String userId = SharedPreferencesManager.getInstance(this).getUserId();
        if (userId == null) return;
        
        String normalizedTarget = normalizeText(target);
        String normalizedSpoken = normalizeText(spoken);
        
        int earnedXP = 0;
        if (normalizedSpoken.equalsIgnoreCase(normalizedTarget)) {
            earnedXP = 50; 
        } else if (normalizedSpoken.toLowerCase().contains(normalizedTarget.toLowerCase())) {
            earnedXP = 30; 
        } else {
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
            earnedXP = (int) ((matchCount * 1.0 / targetWords.length) * 20); 
        }
        
        final int finalEarnedXP = earnedXP;
        
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long currentTotalXP = documentSnapshot.getLong("total_xp");
                        Long currentLevel = documentSnapshot.getLong("current_level");
                        Long currentLevelXP = documentSnapshot.getLong("current_level_xp");
                        Long xpToNextLevel = documentSnapshot.getLong("xp_to_next_level");
                        
                        int newTotalXP = (currentTotalXP != null) ? currentTotalXP.intValue() : 0;
                        int newLevel = (currentLevel != null) ? currentLevel.intValue() : 1;
                        int newLevelXP = (currentLevelXP != null) ? currentLevelXP.intValue() : 0;
                        int newNextLevelXP = (xpToNextLevel != null) ? xpToNextLevel.intValue() : 100;
                        
                        newTotalXP += finalEarnedXP;
                        newLevelXP += finalEarnedXP;
                        
                        boolean leveledUp = false;
                        while (newLevelXP >= newNextLevelXP) {
                            leveledUp = true;
                            newLevelXP -= newNextLevelXP;
                            newLevel++;
                            newNextLevelXP = 100 + (newLevel - 1) * 50;
                        }
                        
                        java.util.Map<String, Object> updates = new java.util.HashMap<>();
                        updates.put("total_xp", newTotalXP);
                        updates.put("current_level", newLevel);
                        updates.put("current_level_xp", newLevelXP);
                        updates.put("xp_to_next_level", newNextLevelXP);
                        
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    if (finalEarnedXP > 0) {
                                        String xpMessage = "+" + finalEarnedXP + " XP earned!";
                                        Toast.makeText(SpeakingActivity.this, xpMessage, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
        
        markDailyChallengeCompleted();
    }
    
    private void markDailyChallengeCompleted() {
        String userId = SharedPreferencesManager.getInstance(this).getUserId();
        if (userId == null) return;

        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        DailyChallengeDao dao = AppDatabase.getDatabase(getApplicationContext()).dailyChallengeDao();

        new Thread(() -> {
            DailyChallenge challenge = dao.getChallengeByDate(userId, today);
            if (challenge != null) {
                if (!challenge.isSpeakingCompleted()) {
                    challenge.setSpeakingCompleted(true);
                    challenge.calculateProgress();
                    challenge.calculateXP();
                    dao.update(challenge);
                    runOnUiThread(() -> Toast.makeText(this, "Daily Challenge: Speaking Completed!", Toast.LENGTH_SHORT).show());
                }
            } else {
                DailyChallenge newChallenge = new DailyChallenge();
                newChallenge.setUserId(userId);
                newChallenge.setDate(today);
                newChallenge.setSpeakingCompleted(true);
                newChallenge.calculateProgress();
                newChallenge.calculateXP();
                dao.insert(newChallenge);
                runOnUiThread(() -> Toast.makeText(this, "Daily Challenge: Speaking Completed!", Toast.LENGTH_SHORT).show());
            }
        }).start();
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
