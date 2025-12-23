package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.ChatAdapter;
import vn.ltdidong.apphoctienganh.database.AITutorMessageDao;
import vn.ltdidong.apphoctienganh.database.AppDatabase;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;
import vn.ltdidong.apphoctienganh.managers.AITutorManager;
import vn.ltdidong.apphoctienganh.models.AITutorMessage;
import vn.ltdidong.apphoctienganh.models.ChatMessage;

/**
 * AI Tutor Activity - Trợ giảng AI thông minh
 * Features:
 * - Chat với AI tutor có ngữ cảnh
 * - Lưu lịch sử hội thoại
 * - Voice input/output
 * - Gợi ý học tập thông minh
 */
public class AITutorActivity extends AppCompatActivity {
    
    private static final int SPEECH_REQUEST_CODE = 100;
    
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private EditText etMessage;
    private ImageButton btnSend, btnMic, btnBack, btnSpeaker;
    private TextView tvTitle, tvSuggestion;
    private ProgressBar progressBar;
    
    private AITutorManager tutorManager;
    private TextToSpeech textToSpeech;
    private String userId;
    private int conversationId = -1;
    private boolean isSpeakerOn = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_tutor);
        
        initViews();
        setupManagers();
        setupRecyclerView();
        setupListeners();
        setupTTS();
        
        // Get or create conversation
        initConversation();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnMic = findViewById(R.id.btnMic);
        btnBack = findViewById(R.id.btnBack);
        btnSpeaker = findViewById(R.id.btnSpeaker);
        tvTitle = findViewById(R.id.tvTitle);
        tvSuggestion = findViewById(R.id.tvSuggestion);
        progressBar = findViewById(R.id.progressBar);
        
        messageList = new ArrayList<>();
    }
    
    private void setupManagers() {
        userId = SharedPreferencesManager.getInstance(this).getUserId();
        tutorManager = new AITutorManager(this);
    }
    
    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(this, messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnSend.setOnClickListener(v -> sendMessage());
        
        btnMic.setOnClickListener(v -> startVoiceInput());
        
        btnSpeaker.setOnClickListener(v -> {
            isSpeakerOn = !isSpeakerOn;
            btnSpeaker.setAlpha(isSpeakerOn ? 1.0f : 0.5f);
            Toast.makeText(this, 
                isSpeakerOn ? "Voice enabled" : "Voice disabled", 
                Toast.LENGTH_SHORT).show();
        });
        
        // Load personalized suggestion
        loadSmartSuggestion();
    }
    
    private void setupTTS() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });
    }
    
    private void initConversation() {
        // Check if conversation ID passed from intent
        conversationId = getIntent().getIntExtra("conversationId", -1);
        
        if (conversationId == -1) {
            // Create new conversation
            final String topicIntent = getIntent().getStringExtra("topic");
            final String topic = (topicIntent == null) ? "General Learning" : topicIntent;
            
            tutorManager.createConversation(userId, "Chat with AI Tutor", topic, 
                newConversationId -> {
                    conversationId = newConversationId;
                    runOnUiThread(() -> {
                        // Load messages
                        loadMessages();
                        // Send welcome message
                        sendWelcomeMessage(topic);
                    });
                });
        } else {
            // Load existing conversation
            loadMessages();
        }
    }
    
    private void loadMessages() {
        if (conversationId == -1) return;
        
        AITutorMessageDao messageDao = AppDatabase.getDatabase(this).aiTutorMessageDao();
        LiveData<List<AITutorMessage>> messagesLiveData = messageDao.getMessagesByConversation(conversationId);
        
        messagesLiveData.observe(this, messages -> {
            messageList.clear();
            for (AITutorMessage msg : messages) {
                messageList.add(new ChatMessage(msg.getMessage(), msg.isUser()));
            }
            chatAdapter.notifyDataSetChanged();
            if (!messageList.isEmpty()) {
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
        });
    }
    
    private void sendWelcomeMessage(String topic) {
        String welcomePrompt = "Greet the student and briefly introduce yourself as their AI English tutor. " +
                "Mention that you're here to help with: " + topic + ". Keep it short and friendly (2 sentences).";
        
        tutorManager.sendMessage(conversationId, welcomePrompt, userId, 
            new AITutorManager.AIResponseCallback() {
                @Override
                public void onResponse(String response) {
                    // Already handled by LiveData observer
                    runOnUiThread(() -> {
                        if (isSpeakerOn) {
                            speak(response);
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    // Welcome message is optional
                }
            });
    }
    
    private void sendMessage() {
        String userMessage = etMessage.getText().toString().trim();
        if (userMessage.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (conversationId == -1) {
            Toast.makeText(this, "Conversation not initialized", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Clear input
        etMessage.setText("");
        
        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        btnSend.setEnabled(false);
        
        // Send to AI
        tutorManager.sendMessage(conversationId, userMessage, userId, 
            new AITutorManager.AIResponseCallback() {
                @Override
                public void onResponse(String response) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnSend.setEnabled(true);
                        
                        // Speak AI response
                        if (isSpeakerOn) {
                            speak(response);
                        }
                        
                        // Scroll to bottom
                        if (!messageList.isEmpty()) {
                            recyclerView.smoothScrollToPosition(messageList.size() - 1);
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnSend.setEnabled(true);
                        Toast.makeText(AITutorActivity.this, 
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
    }
    
    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your question...");
        
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Voice input not supported", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spokenText = results.get(0);
                etMessage.setText(spokenText);
                sendMessage();
            }
        }
    }
    
    private void speak(String text) {
        if (textToSpeech != null && isSpeakerOn) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
    
    private void loadSmartSuggestion() {
        tutorManager.getPersonalizedSuggestion(userId, 
            new AITutorManager.SuggestionCallback() {
                @Override
                public void onSuggestion(String suggestion) {
                    runOnUiThread(() -> {
                        tvSuggestion.setText("💡 " + suggestion);
                        tvSuggestion.setVisibility(View.VISIBLE);
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> tvSuggestion.setVisibility(View.GONE));
                }
            });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
