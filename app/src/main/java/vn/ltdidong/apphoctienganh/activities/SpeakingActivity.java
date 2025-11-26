package vn.ltdidong.apphoctienganh.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

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

public class SpeakingActivity extends AppCompatActivity {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView tvTargetText;
    private TextView tvResultText;
    private TextView tvScore;
    private Button btnListen;
    private ImageButton btnBack;
    private ImageView ivMic;
    private TextView tvHeader;
    private Button btnNextTopic;
    
    private TextToSpeech textToSpeech;
    private GeminiApi geminiApi;
    private static final String API_KEY = "AIzaSyBH8pQnKESAndJW5MrQ961lMWCFFMHez0I";

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

        btnListen.setOnClickListener(v -> speakOut());
        ivMic.setOnClickListener(v -> startVoiceInput());
        btnBack.setOnClickListener(v -> finish());

        btnNextTopic.setOnClickListener(v -> {
            tvResultText.setText("...");
            tvScore.setText("");
            generateSpeakingTopic();
        });
        
        // Load initial topic
        generateSpeakingTopic();
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
                    tvHeader.setText("Speaking Practice");
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

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the sentence...");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Speech not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null && !result.isEmpty()) {
                    String spokenText = result.get(0);
                    tvResultText.setText(spokenText);
                    gradeSpeaking(tvTargetText.getText().toString(), spokenText);
                }
            }
        }
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
            startVoiceInput(); // Retry immediately
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

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
