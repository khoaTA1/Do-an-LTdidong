package vn.ltdidong.apphoctienganh.managers;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.ltdidong.apphoctienganh.api.GeminiApi;
import vn.ltdidong.apphoctienganh.database.AITutorMessageDao;
import vn.ltdidong.apphoctienganh.database.AppDatabase;
import vn.ltdidong.apphoctienganh.database.ChatConversationDao;
import vn.ltdidong.apphoctienganh.models.AITutorMessage;
import vn.ltdidong.apphoctienganh.models.ChatConversation;
import vn.ltdidong.apphoctienganh.models.GeminiRequest;
import vn.ltdidong.apphoctienganh.models.GeminiResponse;
import vn.ltdidong.apphoctienganh.models.SkillProgress;

/**
 * Manager cho AI Tutor - Quản lý hội thoại AI thông minh
 * Features:
 * - Context-aware responses (biết người dùng đang học gì)
 * - Personalized suggestions (gợi ý dựa trên tiến độ)
 * - Multi-turn conversation (nhớ ngữ cảnh)
 * - Save chat history (lưu lịch sử)
 */
public class AITutorManager {
    
    private static final String TAG = "AITutorManager";
    private static final String API_KEY = "AIzaSyDOJpBmNfXE6aWZGRrb8Dy9XlzED1_QQNY";
    
    private Context context;
    private AppDatabase database;
    private ChatConversationDao conversationDao;
    private AITutorMessageDao messageDao;
    private GeminiApi geminiApi;
    private FirebaseFirestore firestore;
    
    public AITutorManager(Context context) {
        this.context = context;
        this.database = AppDatabase.getDatabase(context);
        this.conversationDao = database.chatConversationDao();
        this.messageDao = database.aiTutorMessageDao();
        this.firestore = FirebaseFirestore.getInstance();
        
        // Initialize Gemini API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        geminiApi = retrofit.create(GeminiApi.class);
    }
    
    /**
     * Tạo cuộc hội thoại mới
     */
    public void createConversation(String userId, String title, String topic, ConversationCallback callback) {
        new Thread(() -> {
            ChatConversation conversation = new ChatConversation(userId, title, topic);
            long conversationId = conversationDao.insert(conversation);
            
            if (callback != null) {
                callback.onConversationCreated((int) conversationId);
            }
        }).start();
    }
    
    /**
     * Gửi tin nhắn và nhận phản hồi từ AI với context
     */
    public void sendMessage(int conversationId, String userMessage, String userId, AIResponseCallback callback) {
        new Thread(() -> {
            // 1. Lưu tin nhắn của user
            AITutorMessage userMsg = new AITutorMessage(conversationId, userMessage, true);
            messageDao.insert(userMsg);
            
            // 2. Cập nhật conversation
            conversationDao.updateLastMessage(conversationId, System.currentTimeMillis());
            
            // 3. Lấy context từ lịch sử chat và user progress
            String context = buildContextPrompt(conversationId, userId);
            
            // 4. Gọi Gemini AI
            String fullPrompt = context + "\n\nUser: " + userMessage + "\n\nAI Tutor:";
            
            geminiApi.generateContent(API_KEY, new GeminiRequest(fullPrompt))
                .enqueue(new Callback<GeminiResponse>() {
                    @Override
                    public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String aiResponse = response.body().getOutputText().trim();
                            
                            // Lưu phản hồi AI
                            new Thread(() -> {
                                AITutorMessage aiMsg = new AITutorMessage(conversationId, aiResponse, false);
                                messageDao.insert(aiMsg);
                                conversationDao.updateLastMessage(conversationId, System.currentTimeMillis());
                                
                                // Sync to Firebase (optional)
                                syncMessageToFirebase(userId, conversationId, aiMsg);
                            }).start();
                            
                            if (callback != null) {
                                callback.onResponse(aiResponse);
                            }
                        } else {
                            if (callback != null) {
                                callback.onError("Failed to get AI response");
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<GeminiResponse> call, Throwable t) {
                        Log.e(TAG, "AI call failed", t);
                        if (callback != null) {
                            callback.onError("Network error: " + t.getMessage());
                        }
                    }
                });
        }).start();
    }
    
    /**
     * Xây dựng context prompt dựa trên lịch sử và tiến độ user
     */
    private String buildContextPrompt(int conversationId, String userId) {
        StringBuilder contextBuilder = new StringBuilder();
        
        // System prompt
        contextBuilder.append("You are an intelligent English tutor assistant. ");
        contextBuilder.append("You help students learn English effectively by providing personalized guidance, ");
        contextBuilder.append("answering questions, explaining concepts, and giving encouragement. ");
        contextBuilder.append("Keep responses concise (2-3 sentences), friendly, and educational.\n\n");
        
        // Lấy thông tin về tiến độ học tập
        try {
            List<SkillProgress> skillProgress = database.skillProgressDao().getAllByUser(userId);
            if (!skillProgress.isEmpty()) {
                contextBuilder.append("Student's Progress:\n");
                for (SkillProgress progress : skillProgress) {
                    contextBuilder.append("- ").append(progress.getSkillType())
                            .append(", Avg Score: ").append(String.format("%.1f", progress.getAverageScore()))
                            .append(", Accuracy: ").append(String.format("%.1f%%", progress.getAverageAccuracy()))
                            .append("\n");
                }
                contextBuilder.append("\n");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting skill progress", e);
        }
        
        // Lấy conversation topic
        try {
            ChatConversation conversation = conversationDao.getByIdSync(conversationId);
            if (conversation != null && conversation.getTopic() != null) {
                contextBuilder.append("Current Topic: ").append(conversation.getTopic()).append("\n\n");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting conversation", e);
        }
        
        // Lấy 5 tin nhắn gần nhất để có context
        try {
            List<AITutorMessage> recentMessages = messageDao.getRecentMessages(conversationId);
            if (!recentMessages.isEmpty()) {
                contextBuilder.append("Recent Conversation:\n");
                for (int i = recentMessages.size() - 1; i >= 0; i--) {
                    AITutorMessage msg = recentMessages.get(i);
                    String sender = msg.isUser() ? "Student" : "Tutor";
                    contextBuilder.append(sender).append(": ")
                            .append(msg.getMessage()).append("\n");
                }
                contextBuilder.append("\n");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting recent messages", e);
        }
        
        return contextBuilder.toString();
    }
    
    /**
     * Tạo gợi ý học tập thông minh dựa trên AI
     */
    public void getPersonalizedSuggestion(String userId, SuggestionCallback callback) {
        new Thread(() -> {
            // Build context
            StringBuilder prompt = new StringBuilder();
            prompt.append("Based on this student's English learning progress, ");
            prompt.append("suggest ONE specific thing they should practice today. ");
            prompt.append("Keep it short (1-2 sentences) and actionable.\n\n");
            
            // Add progress info
            try {
                List<SkillProgress> skillProgress = database.skillProgressDao().getAllByUser(userId);
                if (!skillProgress.isEmpty()) {
                    prompt.append("Their skills:\n");
                    for (SkillProgress progress : skillProgress) {
                        prompt.append("- ").append(progress.getSkillType())
                                .append(": ").append(progress.getAverageScore()).append("%\n");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error building suggestion", e);
            }
            
            // Call AI
            geminiApi.generateContent(API_KEY, new GeminiRequest(prompt.toString()))
                .enqueue(new Callback<GeminiResponse>() {
                    @Override
                    public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String suggestion = response.body().getOutputText().trim();
                            if (callback != null) {
                                callback.onSuggestion(suggestion);
                            }
                        } else {
                            if (callback != null) {
                                callback.onError("Failed to generate suggestion");
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<GeminiResponse> call, Throwable t) {
                        if (callback != null) {
                            callback.onError(t.getMessage());
                        }
                    }
                });
        }).start();
    }
    
    /**
     * Sync message to Firebase (optional - for backup)
     */
    private void syncMessageToFirebase(String userId, int conversationId, AITutorMessage message) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("conversationId", conversationId);
            messageData.put("message", message.getMessage());
            messageData.put("isUser", message.isUser());
            messageData.put("timestamp", message.getTimestamp());
            
            firestore.collection("users")
                    .document(userId)
                    .collection("ai_chat_history")
                    .add(messageData)
                    .addOnSuccessListener(doc -> Log.d(TAG, "Message synced to Firebase"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to sync message", e));
        } catch (Exception e) {
            Log.e(TAG, "Error syncing to Firebase", e);
        }
    }
    
    // Callbacks
    public interface ConversationCallback {
        void onConversationCreated(int conversationId);
    }
    
    public interface AIResponseCallback {
        void onResponse(String response);
        void onError(String error);
    }
    
    public interface SuggestionCallback {
        void onSuggestion(String suggestion);
        void onError(String error);
    }
}
