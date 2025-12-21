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
import vn.ltdidong.apphoctienganh.database.AppDatabase;
import vn.ltdidong.apphoctienganh.models.GeminiRequest;
import vn.ltdidong.apphoctienganh.models.GeminiResponse;
import vn.ltdidong.apphoctienganh.models.LearningPathStep;
import vn.ltdidong.apphoctienganh.models.SkillProgress;
import vn.ltdidong.apphoctienganh.models.SkillType;
import vn.ltdidong.apphoctienganh.models.StudyHabit;

/**
 * AI-Powered Personalized Learning Path Manager
 * Tạo lộ trình học tập cá nhân hóa dựa trên:
 * - Phân tích điểm mạnh/yếu của user
 * - Mục tiêu học tập
 * - Thời gian có sẵn
 * - AI recommendations
 */
public class AdaptiveLearningPathManager {
    
    private static final String TAG = "AdaptiveLearningPath";
    private static final String API_KEY = "AIzaSyDOJpBmNfXE6aWZGRrb8Dy9XlzED1_QQNY";
    
    private Context context;
    private AppDatabase database;
    private LearningAnalyzer analyzer;
    private GeminiApi geminiApi;
    private FirebaseFirestore firestore;
    
    public AdaptiveLearningPathManager(Context context) {
        this.context = context;
        this.database = AppDatabase.getDatabase(context);
        this.analyzer = new LearningAnalyzer(context);
        this.firestore = FirebaseFirestore.getInstance();
        
        // Initialize Gemini API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        geminiApi = retrofit.create(GeminiApi.class);
    }
    
    /**
     * Tạo lộ trình học cá nhân hóa bằng AI
     * @param userId User ID
     * @param daysCount Số ngày muốn lập kế hoạch
     * @param dailyMinutes Số phút học mỗi ngày
     * @param goal Mục tiêu học tập
     */
    public void generateAILearningPath(String userId, int daysCount, int dailyMinutes, 
                                       String goal, PathGenerationCallback callback) {
        new Thread(() -> {
            try {
                // 1. Phân tích tiến độ hiện tại
                List<SkillProgress> allSkills = database.skillProgressDao().getAllByUser(userId);
                List<SkillProgress> weakSkills = analyzer.getWeakSkills(userId);
                StudyHabit habit = database.studyHabitDao().getByUser(userId);
                
                // 2. Build AI prompt
                String prompt = buildLearningPathPrompt(allSkills, weakSkills, habit, 
                                                       daysCount, dailyMinutes, goal);
                
                // 3. Gọi AI để tạo path
                geminiApi.generateContent(API_KEY, new GeminiRequest(prompt))
                    .enqueue(new Callback<GeminiResponse>() {
                        @Override
                        public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String aiResponse = response.body().getOutputText().trim();
                                
                                // 4. Parse AI response và tạo learning steps
                                List<LearningPathStep> steps = parseAIResponseToSteps(
                                    userId, aiResponse, daysCount, dailyMinutes);
                                
                                // 5. Lưu vào Firebase
                                saveLearningPathToFirebase(userId, steps);
                                
                                if (callback != null) {
                                    callback.onPathGenerated(steps);
                                }
                            } else {
                                // Fallback: Tạo path dựa trên rule-based
                                List<LearningPathStep> fallbackSteps = 
                                    generateRuleBasedPath(userId, allSkills, weakSkills, 
                                                         daysCount, dailyMinutes);
                                if (callback != null) {
                                    callback.onPathGenerated(fallbackSteps);
                                }
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<GeminiResponse> call, Throwable t) {
                            Log.e(TAG, "AI call failed", t);
                            // Fallback
                            List<LearningPathStep> fallbackSteps = 
                                generateRuleBasedPath(userId, allSkills, weakSkills, 
                                                     daysCount, dailyMinutes);
                            if (callback != null) {
                                callback.onPathGenerated(fallbackSteps);
                            }
                        }
                    });
                    
            } catch (Exception e) {
                Log.e(TAG, "Error generating path", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        }).start();
    }
    
    /**
     * Build AI prompt cho việc tạo learning path
     */
    private String buildLearningPathPrompt(List<SkillProgress> allSkills, 
                                           List<SkillProgress> weakSkills,
                                           StudyHabit habit, int days, 
                                           int dailyMinutes, String goal) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Create a personalized ").append(days).append("-day English learning path. ");
        prompt.append("Daily study time: ").append(dailyMinutes).append(" minutes. ");
        prompt.append("Goal: ").append(goal != null ? goal : "Improve overall English").append("\n\n");
        
        // Current skills
        prompt.append("Current Skills:\n");
        for (SkillProgress skill : allSkills) {
            prompt.append("- ").append(skill.getSkillType())
                  .append(", Avg Score: ").append(String.format("%.1f", skill.getAverageScore()))
                  .append(", Accuracy: ").append(String.format("%.1f%%", skill.getAverageAccuracy())).append("\n");
        }
        
        // Weak areas
        if (!weakSkills.isEmpty()) {
            prompt.append("\nWeak Areas (need improvement):\n");
            for (SkillProgress skill : weakSkills) {
                prompt.append("- ").append(skill.getSkillType())
                      .append(" (").append(skill.getAverageScore()).append("%)\n");
            }
        }
        
        // Study habit
        if (habit != null) {
            prompt.append("\nStudy Habits:\n");
            prompt.append("- Preferred time: Hour ").append(habit.getPreferredHourOfDay()).append("\n");
            prompt.append("- Average session: ").append(habit.getAverageSessionMinutes()).append(" minutes\n");
            prompt.append("- Current streak: ").append(habit.getCurrentStreak()).append(" days\n");
        }
        
        prompt.append("\nGenerate a structured learning path in this format:\n");
        prompt.append("Day X | [SKILL] | [DIFFICULTY] | [TIME]min | [TITLE] | [REASON]\n\n");
        prompt.append("Example:\n");
        prompt.append("Day 1 | LISTENING | EASY | 20min | Practice Daily Conversations | Build listening foundation\n");
        prompt.append("Day 1 | READING | MEDIUM | 15min | Short News Articles | Improve weak reading skill\n\n");
        prompt.append("Skills: LISTENING, READING, WRITING, SPEAKING\n");
        prompt.append("Difficulty: EASY, MEDIUM, HARD\n");
        prompt.append("Focus on weak areas but balance with practice in all skills.\n");
        
        return prompt.toString();
    }
    
    /**
     * Parse AI response thành list of steps
     */
    private List<LearningPathStep> parseAIResponseToSteps(String userId, String aiResponse, 
                                                          int days, int dailyMinutes) {
        List<LearningPathStep> steps = new ArrayList<>();
        String[] lines = aiResponse.split("\n");
        int stepNumber = 1;
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || !line.contains("|")) continue;
            
            try {
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    String day = parts[0].trim();
                    String skill = parts[1].trim();
                    String difficulty = parts[2].trim();
                    String time = parts[3].trim().replace("min", "").trim();
                    String title = parts[4].trim();
                    String reason = parts.length > 5 ? parts[5].trim() : "";
                    
                    // Validate skill type
                    if (!isValidSkill(skill)) continue;
                    
                    int minutes = Integer.parseInt(time);
                    
                    LearningPathStep step = new LearningPathStep(
                        userId, stepNumber++, skill, title, difficulty, minutes, reason
                    );
                    steps.add(step);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing line: " + line, e);
            }
        }
        
        // If parsing failed, generate rule-based
        if (steps.isEmpty()) {
            return generateRuleBasedPath(userId, 
                database.skillProgressDao().getAllByUser(userId),
                analyzer.getWeakSkills(userId), days, dailyMinutes);
        }
        
        return steps;
    }
    
    /**
     * Generate rule-based learning path (fallback)
     */
    private List<LearningPathStep> generateRuleBasedPath(String userId, 
                                                         List<SkillProgress> allSkills,
                                                         List<SkillProgress> weakSkills,
                                                         int days, int dailyMinutes) {
        List<LearningPathStep> steps = new ArrayList<>();
        int stepNumber = 1;
        
        for (int day = 1; day <= days; day++) {
            int remainingMinutes = dailyMinutes;
            
            // Focus on weak skill first (50% time)
            if (!weakSkills.isEmpty()) {
                SkillProgress weakest = weakSkills.get(0);
                int weakTime = dailyMinutes / 2;
                
                String difficulty = weakest.getAverageScore() < 40 ? "EASY" : 
                                  weakest.getAverageScore() < 60 ? "MEDIUM" : "HARD";
                
                steps.add(new LearningPathStep(
                    userId, stepNumber++, weakest.getSkillType(),
                    "Improve " + weakest.getSkillType(),
                    difficulty, weakTime,
                    "Focus on weak area (avg: " + weakest.getAverageScore() + "%)"
                ));
                
                remainingMinutes -= weakTime;
            }
            
            // Practice other skills (remaining time)
            if (remainingMinutes > 0 && !allSkills.isEmpty()) {
                // Rotate through skills
                int skillIndex = (day - 1) % allSkills.size();
                SkillProgress skill = allSkills.get(skillIndex);
                
                if (!weakSkills.contains(skill)) {
                    String difficulty = skill.getAverageScore() >= 70 ? "HARD" : "MEDIUM";
                    
                    steps.add(new LearningPathStep(
                        userId, stepNumber++, skill.getSkillType(),
                        "Practice " + skill.getSkillType(),
                        difficulty, remainingMinutes,
                        "Maintain strength"
                    ));
                }
            }
        }
        
        return steps;
    }
    
    /**
     * Lưu learning path vào Firebase
     */
    private void saveLearningPathToFirebase(String userId, List<LearningPathStep> steps) {
        try {
            Map<String, Object> pathData = new HashMap<>();
            pathData.put("userId", userId);
            pathData.put("createdAt", System.currentTimeMillis());
            pathData.put("totalSteps", steps.size());
            
            List<Map<String, Object>> stepsData = new ArrayList<>();
            for (LearningPathStep step : steps) {
                Map<String, Object> stepMap = new HashMap<>();
                stepMap.put("stepNumber", step.getStepNumber());
                stepMap.put("skillType", step.getSkillType());
                stepMap.put("title", step.getTitle());
                stepMap.put("difficulty", step.getDifficulty());
                stepMap.put("estimatedMinutes", step.getEstimatedMinutes());
                stepMap.put("reason", step.getReason());
                stepMap.put("isCompleted", step.isCompleted());
                stepsData.add(stepMap);
            }
            pathData.put("steps", stepsData);
            
            firestore.collection("users")
                .document(userId)
                .collection("learning_paths")
                .add(pathData)
                .addOnSuccessListener(doc -> 
                    Log.d(TAG, "Learning path saved to Firebase"))
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Failed to save path", e));
                    
        } catch (Exception e) {
            Log.e(TAG, "Error saving to Firebase", e);
        }
    }
    
    /**
     * Load learning path từ Firebase
     */
    public void loadLearningPath(String userId, PathLoadCallback callback) {
        firestore.collection("users")
            .document(userId)
            .collection("learning_paths")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    Map<String, Object> pathData = querySnapshot.getDocuments().get(0).getData();
                    List<Map<String, Object>> stepsData = 
                        (List<Map<String, Object>>) pathData.get("steps");
                    
                    List<LearningPathStep> steps = new ArrayList<>();
                    if (stepsData != null) {
                        for (Map<String, Object> stepMap : stepsData) {
                            LearningPathStep step = new LearningPathStep();
                            step.setUserId(userId);
                            step.setStepNumber(((Long) stepMap.get("stepNumber")).intValue());
                            step.setSkillType((String) stepMap.get("skillType"));
                            step.setTitle((String) stepMap.get("title"));
                            step.setDifficulty((String) stepMap.get("difficulty"));
                            step.setEstimatedMinutes(((Long) stepMap.get("estimatedMinutes")).intValue());
                            step.setReason((String) stepMap.get("reason"));
                            step.setCompleted((Boolean) stepMap.get("isCompleted"));
                            steps.add(step);
                        }
                    }
                    
                    if (callback != null) {
                        callback.onPathLoaded(steps);
                    }
                } else {
                    if (callback != null) {
                        callback.onPathLoaded(new ArrayList<>());
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to load path", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            });
    }
    
    private boolean isValidSkill(String skill) {
        try {
            SkillType.valueOf(skill);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Callbacks
    public interface PathGenerationCallback {
        void onPathGenerated(List<LearningPathStep> steps);
        void onError(String error);
    }
    
    public interface PathLoadCallback {
        void onPathLoaded(List<LearningPathStep> steps);
        void onError(String error);
    }
}
