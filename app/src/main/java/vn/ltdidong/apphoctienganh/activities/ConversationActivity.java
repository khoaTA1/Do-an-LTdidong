package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.ChatAdapter;
import vn.ltdidong.apphoctienganh.api.GeminiApi;
import vn.ltdidong.apphoctienganh.models.ChatMessage;
import vn.ltdidong.apphoctienganh.models.GeminiRequest;
import vn.ltdidong.apphoctienganh.models.GeminiResponse;

public class ConversationActivity extends AppCompatActivity {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private ImageButton btnMic, btnBack;
    private EditText etInput; // Optional: for typing if needed later
    private TextView tvTopicTitle;

    private TextToSpeech textToSpeech;
    private GeminiApi geminiApi;
    // Updated API Key to match SpeakingActivity
    private static final String API_KEY = "AIzaSyCJvhWIWINzPIr0liu_WKEUHiqw5wpgLCo"; 
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
        setupGemini();
        setupTTS();

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
        btnMic.setOnClickListener(v -> startVoiceInput());
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
    }

    private void setupGemini() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        geminiApi = retrofit.create(GeminiApi.class);
    }

    private void setupTTS() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });
    }

    private void sendInitialGreeting() {
        String prompt = "You are an English tutor. Start a conversation about the topic: '" + currentTopic + "'. Ask a question to start.";
        callGemini(prompt, false);
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Speech input not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String userMessage = result.get(0);
                addMessage(userMessage, true);
                
                // Generate AI response
                processUserMessage(userMessage);
            }
        }
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
        geminiApi.generateContent(API_KEY, new GeminiRequest(prompt)).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
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
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                addMessage("Network Error.", false);
            }
        });
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
