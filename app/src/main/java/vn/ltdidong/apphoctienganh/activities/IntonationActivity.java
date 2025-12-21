package vn.ltdidong.apphoctienganh.activities;

import android.Manifest;
import android.app.ProgressDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Locale;

import io.noties.markwon.Markwon;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.api.AiService;
import vn.ltdidong.apphoctienganh.models.GroqChatCompletionResponse;

public class IntonationActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private MaterialCardView btnRecord;
    private TextView tvRecognizedText;
    private Button btnPlayNative, btnAnalyze;
    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private Intent recognitionIntent;
    private View btnBack;
    private TextView tvRecordStatus;
    private CardView resultCard;
    private boolean isListening = false;

    private AiService aiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intonation);

        btnRecord = findViewById(R.id.btnRecord);
        tvRecognizedText = findViewById(R.id.tvRecognizedText);
        btnPlayNative = findViewById(R.id.btnPlayNative);
        btnBack = findViewById(R.id.btnBack);
        tvRecordStatus = findViewById(R.id.tvRecordStatus);
        resultCard = findViewById(R.id.resultCard);
        
        // Initialize Analyze Button
        btnAnalyze = findViewById(R.id.btnAnalyze);

        aiService = AiService.getInstance();

        // Initialize TTS
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                textToSpeech.setPitch(1.0f);
                textToSpeech.setSpeechRate(0.9f);

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(IntonationActivity.this, "Language not supported", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                 tvRecordStatus.setText("Listening...");
                 tvRecordStatus.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                 resultCard.setVisibility(View.GONE); 
            }
            @Override
            public void onBeginningOfSpeech() {}
            @Override
            public void onRmsChanged(float rmsdB) {}
            @Override
            public void onBufferReceived(byte[] buffer) {}
            @Override
            public void onEndOfSpeech() {
                isListening = false;
                tvRecordStatus.setText("Tap the mic to start");
                tvRecordStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
            @Override
            public void onError(int error) {
                isListening = false;
                String errorMessage = getErrorText(error);
                tvRecordStatus.setText("Error: " + errorMessage);
                tvRecordStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String text = matches.get(0);
                    tvRecognizedText.setText(text);
                    btnPlayNative.setEnabled(true);
                    if (btnAnalyze != null) btnAnalyze.setEnabled(true);
                    resultCard.setVisibility(View.VISIBLE); 
                }
            }
            @Override
            public void onPartialResults(Bundle partialResults) {}
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        btnRecord.setOnClickListener(v -> {
            if (checkPermission()) {
                if (!isListening) {
                    speechRecognizer.startListening(recognitionIntent);
                    isListening = true;
                } else {
                    speechRecognizer.stopListening();
                    isListening = false;
                }
            } else {
                requestPermission();
            }
        });
        
        btnPlayNative.setOnClickListener(v -> {
            String text = tvRecognizedText.getText().toString();
            if (!text.equals("...") && !text.isEmpty()) {
                speakNative(text);
            }
        });

        if (btnAnalyze != null) {
            btnAnalyze.setOnClickListener(v -> {
                String text = tvRecognizedText.getText().toString();
                if (!text.isEmpty()) {
                    analyzeIntonation(text);
                }
            });
        }

        btnBack.setOnClickListener(v -> finish());
    }

    private void analyzeIntonation(String text) {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Analyzing rhythm & fluency...");
        pd.show();

        String prompt = "Analyze the rhythm and fluency of this sentence for an English learner: \"" + text + "\"\n" +
                "Provide:\n" +
                "1. Stress & Intonation guide (mark stressed words in bold)\n" +
                "2. Fluency tips (where to pause)\n" +
                "3. Rate complexity (Easy/Medium/Hard)\n" +
                "Keep it concise.";

        aiService.generateText(prompt).enqueue(new Callback<GroqChatCompletionResponse>() {
            @Override
            public void onResponse(Call<GroqChatCompletionResponse> call, Response<GroqChatCompletionResponse> response) {
                pd.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().getOutputText();
                    showAnalysisResult(result);
                } else {
                    Log.e("Analyze", "Fail: " + response.code() + " " + response.message());
                    Toast.makeText(IntonationActivity.this, "Analysis failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GroqChatCompletionResponse> call, Throwable t) {
                pd.dismiss();
                Log.e("Analyze", "Error", t);
                Toast.makeText(IntonationActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAnalysisResult(String markdown) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_result_sheet, null);
        bottomSheetDialog.setContentView(view);

        TextView tvContent = view.findViewById(R.id.tvMarkdownContent);
        Button btnKeepWriting = view.findViewById(R.id.btnKeepWriting);
        Button btnNewTopicSheet = view.findViewById(R.id.btnNewTopicSheet);
        
        // Customize sheet for analysis
        btnKeepWriting.setText("Close");
        btnNewTopicSheet.setVisibility(View.GONE); // Hide second button

        Markwon markwon = Markwon.create(this);
        markwon.setMarkdown(tvContent, markdown);

        btnKeepWriting.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void speakNative(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }
    
    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }
}
