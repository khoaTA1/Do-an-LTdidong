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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.ChatAdapter;
import vn.ltdidong.apphoctienganh.api.AiService;
import vn.ltdidong.apphoctienganh.models.ChatMessage;
import vn.ltdidong.apphoctienganh.models.GroqChatCompletionResponse;

public class ConversationActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 200;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private ImageButton btnMic, btnBack;
    private TextView tvTopicTitle;

    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private boolean isListening = false;

    private AiService aiService;
    private String currentTopic = "General Conversation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        // Get Topic from Intent
        if (getIntent().hasExtra("TOPIC")) {
            currentTopic = getIntent().getStringExtra("TOPIC");
        }

        initViews();
        setupRecyclerView();
        aiService = AiService.getInstance();
        setupTTS();
        setupSpeechRecognizer();

        // Start with a greeting from AI
        sendInitialGreeting();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.chatRecyclerView);
        btnMic = findViewById(R.id.btnMic);
        btnBack = findViewById(R.id.btnBack);
        tvTopicTitle = findViewById(R.id.tvTopicTitle);
        
        tvTopicTitle.setText("Topic: " + currentTopic);

        btnBack.setOnClickListener(v -> finish());
        
        btnMic.setOnClickListener(v -> {
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
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
    }

    private void setupTTS() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });
    }

    private void setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                btnMic.setImageResource(android.R.drawable.ic_btn_speak_now); // Indicate listening
                Toast.makeText(ConversationActivity.this, "Listening...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                btnMic.setImageResource(R.drawable.ic_speaking); // Reset icon (assuming ic_speaking exists or use default mic icon)
                isListening = false;
            }

            @Override
            public void onError(int error) {
                btnMic.setImageResource(R.drawable.ic_speaking); 
                isListening = false;
                // Optional: Show error message
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String userMessage = matches.get(0);
                    addMessage(userMessage, true);
                    processUserMessage(userMessage);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startListening() {
        speechRecognizer.startListening(speechRecognizerIntent);
        isListening = true;
    }

    private void stopListening() {
        speechRecognizer.stopListening();
        isListening = false;
        btnMic.setImageResource(R.drawable.ic_speaking);
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
                startListening();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendInitialGreeting() {
        String prompt = "You are an English tutor. Start a conversation about the topic: '" + currentTopic + "'. Ask a question to start.";
        callGemini(prompt, false);
    }

    private void addMessage(String text, boolean isUser) {
        messageList.add(new ChatMessage(text, isUser));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void processUserMessage(String userMessage) {
        // Context aware prompt
        String prompt = "We are talking about '" + currentTopic + "'. User said: '" + userMessage + "'. Reply naturally as a tutor and keep the conversation going. Keep it short (1-2 sentences).";
        callGemini(prompt, true);
    }

    private void callGemini(String prompt, boolean speakResponse) {
        aiService.generateText(prompt).enqueue(new Callback<GroqChatCompletionResponse>() {
            @Override
            public void onResponse(Call<GroqChatCompletionResponse> call, Response<GroqChatCompletionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String reply = response.body().getOutputText().trim();
                    addMessage(reply, false);
                    if (speakResponse) {
                        textToSpeech.speak(reply, TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                } else {
                    addMessage("Error: Could not get response from AI.", false);
                }
            }

            @Override
            public void onFailure(Call<GroqChatCompletionResponse> call, Throwable t) {
                addMessage("Network Error.", false);
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
