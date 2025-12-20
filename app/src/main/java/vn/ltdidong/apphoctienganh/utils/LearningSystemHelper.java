package vn.ltdidong.apphoctienganh.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import vn.ltdidong.apphoctienganh.managers.DataSyncManager;
import vn.ltdidong.apphoctienganh.managers.LearningAnalyzer;
import vn.ltdidong.apphoctienganh.managers.PersonalizedRecommendationEngine;
import vn.ltdidong.apphoctienganh.managers.StudyScheduleGenerator;

/**
 * Helper class để dễ dàng sử dụng hệ thống học tập thông minh
 * Singleton pattern để tránh tạo nhiều instances
 */
public class LearningSystemHelper {
    
    private static final String TAG = "LearningSystemHelper";
    
    private static LearningSystemHelper instance;
    
    private final Context context;
    private final DataSyncManager syncManager;
    private final LearningAnalyzer analyzer;
    private final StudyScheduleGenerator scheduleGenerator;
    private final PersonalizedRecommendationEngine recommendationEngine;
    
    private LearningSystemHelper(Context context) {
        this.context = context.getApplicationContext();
        this.syncManager = new DataSyncManager(this.context);
        this.analyzer = new LearningAnalyzer(this.context);
        this.scheduleGenerator = new StudyScheduleGenerator(this.context);
        this.recommendationEngine = new PersonalizedRecommendationEngine(this.context);
    }
    
    /**
     * Lấy instance của helper (Singleton)
     */
    public static synchronized LearningSystemHelper getInstance(Context context) {
        if (instance == null) {
            instance = new LearningSystemHelper(context);
        }
        return instance;
    }
    
    /**
     * Lấy user ID hiện tại
     */
    private String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }
    
    /**
     * Đồng bộ toàn bộ dữ liệu
     */
    public void syncAll(DataSyncManager.SyncCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onSyncError("User not logged in");
            return;
        }
        
        syncManager.syncAll(callback);
    }
    
    /**
     * Phân tích toàn bộ dữ liệu học tập
     */
    public void analyzeAll(LearningAnalyzer.AnalysisCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onAnalysisError("User not logged in");
            return;
        }
        
        analyzer.analyzeAllData(userId, callback);
    }
    
    /**
     * Tạo lịch học cho N ngày tới
     */
    public void generateSchedule(int daysAhead, StudyScheduleGenerator.ScheduleCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onScheduleError("User not logged in");
            return;
        }
        
        scheduleGenerator.generateSchedule(userId, daysAhead, callback);
    }
    
    /**
     * Lấy gợi ý nhanh cho hôm nay
     */
    public PersonalizedRecommendationEngine.Recommendation getQuickRecommendation() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return null;
        }
        
        return recommendationEngine.getQuickRecommendation(userId);
    }
    
    /**
     * Lấy tóm tắt hàng ngày
     */
    public String getDailySummary() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return "Vui lòng đăng nhập để xem thống kê";
        }
        
        return recommendationEngine.getDailySummary(userId);
    }
    
    /**
     * Đánh dấu lịch đã hoàn thành
     */
    public void markScheduleCompleted(long scheduleId, float score, int durationMinutes) {
        scheduleGenerator.markScheduleCompleted(scheduleId, score, durationMinutes);
    }
    
    /**
     * Đánh dấu lịch bị bỏ qua
     */
    public void markScheduleSkipped(long scheduleId) {
        scheduleGenerator.markScheduleSkipped(scheduleId);
    }
    
    /**
     * Làm hết hạn các lịch cũ
     */
    public void expireOldSchedules() {
        String userId = getCurrentUserId();
        if (userId != null) {
            scheduleGenerator.expireOldSchedules(userId);
        }
    }
    
    /**
     * Xóa dữ liệu cũ (cleanup)
     * @param daysToKeep Số ngày muốn giữ lại
     */
    public void cleanupOldData(int daysToKeep) {
        String userId = getCurrentUserId();
        if (userId != null) {
            scheduleGenerator.cleanupOldSchedules(userId, daysToKeep);
        }
    }
    
    /**
     * Xóa các lịch học cũ (wrapper method)
     */
    public void deleteOldSchedules(int daysOld) {
        String userId = getCurrentUserId();
        if (userId != null) {
            scheduleGenerator.deleteOldSchedules(userId, daysOld);
        }
    }
    
    /**
     * Quy trình hoàn chỉnh khi user vừa đăng nhập
     * 1. Sync dữ liệu từ cloud
     * 2. Phân tích dữ liệu
     * 3. Tạo lịch học nếu chưa có
     */
    public void onUserLogin(final OnLoginCompleteCallback callback) {
        Log.d(TAG, "Starting login workflow...");
        
        // Bước 1: Sync dữ liệu
        syncAll(new DataSyncManager.SyncCallback() {
            @Override
            public void onSyncSuccess(String message) {
                Log.d(TAG, "✅ Sync completed");
                
                // Bước 2: Phân tích dữ liệu
                analyzeAll(new LearningAnalyzer.AnalysisCallback() {
                    @Override
                    public void onAnalysisComplete(String message) {
                        Log.d(TAG, "✅ Analysis completed");
                        
                        // Bước 3: Tạo lịch nếu cần
                        generateSchedule(7, new StudyScheduleGenerator.ScheduleCallback() {
                            @Override
                            public void onScheduleGenerated(java.util.List<vn.ltdidong.apphoctienganh.models.StudySchedule> schedules) {
                                Log.d(TAG, "✅ Schedule generated: " + schedules.size() + " items");
                                callback.onComplete(true, "Setup completed successfully");
                            }
                            
                            @Override
                            public void onScheduleError(String error) {
                                Log.e(TAG, "Schedule error: " + error);
                                // Vẫn thành công nếu chỉ lỗi tạo lịch
                                callback.onComplete(true, "Setup completed with minor issues");
                            }
                        });
                    }
                    
                    @Override
                    public void onAnalysisError(String error) {
                        Log.e(TAG, "Analysis error: " + error);
                        callback.onComplete(false, "Analysis failed: " + error);
                    }
                });
            }
            
            @Override
            public void onSyncError(String error) {
                Log.e(TAG, "Sync error: " + error);
                // Có thể vẫn tiếp tục nếu có dữ liệu local
                callback.onComplete(true, "Working offline: " + error);
            }
            
            @Override
            public void onSyncProgress(int current, int total) {
                callback.onProgress("Đang đồng bộ " + current + "/" + total);
            }
        });
    }
    
    /**
     * Quy trình hoàn chỉnh sau mỗi bài học
     * 1. Lưu learning session (được gọi từ Activity)
     * 2. Phân tích lại dữ liệu
     * 3. Cập nhật lịch học
     */
    public void onLessonComplete(final OnLessonCompleteCallback callback) {
        Log.d(TAG, "Starting post-lesson workflow...");
        
        // Phân tích lại
        analyzeAll(new LearningAnalyzer.AnalysisCallback() {
            @Override
            public void onAnalysisComplete(String message) {
                Log.d(TAG, "✅ Re-analysis completed");
                callback.onComplete(true, "Progress updated successfully");
            }
            
            @Override
            public void onAnalysisError(String error) {
                Log.e(TAG, "Re-analysis error: " + error);
                callback.onComplete(false, "Failed to update progress");
            }
        });
    }
    
    /**
     * Callback cho login workflow
     */
    public interface OnLoginCompleteCallback {
        void onComplete(boolean success, String message);
        void onProgress(String progressMessage);
    }
    
    /**
     * Callback cho lesson complete workflow
     */
    public interface OnLessonCompleteCallback {
        void onComplete(boolean success, String message);
    }
    
    // Getters cho các managers (nếu cần truy cập trực tiếp)
    public DataSyncManager getSyncManager() {
        return syncManager;
    }
    
    public LearningAnalyzer getAnalyzer() {
        return analyzer;
    }
    
    public StudyScheduleGenerator getScheduleGenerator() {
        return scheduleGenerator;
    }
    
    public PersonalizedRecommendationEngine getRecommendationEngine() {
        return recommendationEngine;
    }
}
