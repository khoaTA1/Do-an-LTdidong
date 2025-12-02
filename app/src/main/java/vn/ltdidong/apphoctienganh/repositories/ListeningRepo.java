package vn.ltdidong.apphoctienganh.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import vn.ltdidong.apphoctienganh.database.AppDatabase;
import vn.ltdidong.apphoctienganh.database.UserProgressDao;
import vn.ltdidong.apphoctienganh.models.UserProgress;

import java.util.List;

/**
 * Repository class cho UserProgress
 * Quản lý tiến độ học tập của người dùng (local database)
 * Lessons và Questions load từ Firebase qua FirebaseListeningRepo
 */
public class ListeningRepo {
    
    // DAO
    private UserProgressDao progressDao;
    private String currentUserId; // User hiện tại đang đăng nhập
    
    /**
     * Constructor - khởi tạo database và DAO
     * @param application Application context
     */
    public ListeningRepo(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        progressDao = db.userProgressDao();
        // Mặc định dùng "guest" nếu chưa đăng nhập
        this.currentUserId = "guest";
    }
    
    /**
     * Set user ID hiện tại (gọi sau khi đăng nhập)
     * @param userId ID của user (từ Firebase Auth hoặc local login)
     */
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId != null ? userId : "guest";
    }
    
    /**
     * Lấy user ID hiện tại
     */
    public String getCurrentUserId() {
        return currentUserId;
    }
    
    // ============= USER PROGRESS METHODS =============
    
    /**
     * Lấy tất cả tiến độ của user hiện tại
     */
    public LiveData<List<UserProgress>> getAllProgress() {
        return progressDao.getAllProgressByUser(currentUserId);
    }
    
    /**
     * Lấy tiến độ của một bài học cho user hiện tại
     */
    public LiveData<UserProgress> getProgressByLesson(int lessonId) {
        return progressDao.getProgressByUserAndLesson(currentUserId, lessonId);
    }
    
    /**
     * Lấy các bài đã hoàn thành của user hiện tại
     */
    public LiveData<List<UserProgress>> getCompletedLessons() {
        return progressDao.getCompletedLessonsByUser(currentUserId);
    }
    
    /**
     * Lấy các bài đang làm dở của user hiện tại
     */
    public LiveData<List<UserProgress>> getInProgressLessons() {
        return progressDao.getInProgressLessonsByUser(currentUserId);
    }
    
    /**
     * Lưu hoặc cập nhật tiến độ (chạy trên background thread)
     */
    public void saveProgress(UserProgress progress) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Set userId cho progress
            progress.setUserId(currentUserId);
            
            // Kiểm tra xem đã có progress chưa
            UserProgress existingProgress = progressDao.getProgressByUserAndLessonSync(
                currentUserId, progress.getLessonId());
            
            if (existingProgress != null) {
                // Nếu đã có, cập nhật
                progress.setId(existingProgress.getId());
                progress.setAttempts(existingProgress.getAttempts() + 1);
                
                // Cập nhật best score nếu điểm mới cao hơn
                if (progress.getScore() > existingProgress.getBestScore()) {
                    progress.setBestScore(progress.getScore());
                } else {
                    progress.setBestScore(existingProgress.getBestScore());
                }
                
                progressDao.updateProgress(progress);
            } else {
                // Nếu chưa có, thêm mới
                progress.setAttempts(1);
                progress.setBestScore(progress.getScore());
                progressDao.insertProgress(progress);
            }
        });
    }
    
    /**
     * Xóa tiến độ (chạy trên background thread)
     */
    public void deleteProgress(UserProgress progress) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            progressDao.deleteProgress(progress);
        });
    }
    
    /**
     * Xóa tiến độ của một bài học cho user hiện tại (chạy trên background thread)
     */
    public void deleteProgressByLesson(int lessonId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            progressDao.deleteProgressByUserAndLesson(currentUserId, lessonId);
        });
    }
    
    /**
     * Lấy điểm trung bình của user hiện tại
     */
    public LiveData<Float> getAverageScore() {
        return progressDao.getAverageScoreByUser(currentUserId);
    }
    
    /**
     * Lấy số bài đã hoàn thành của user hiện tại
     */
    public LiveData<Integer> getCompletedLessonCount() {
        return progressDao.getCompletedLessonCountByUser(currentUserId);
    }
    
    /**
     * Lấy điểm cao nhất của user hiện tại
     */
    public LiveData<Float> getHighestScore() {
        return progressDao.getHighestScoreByUser(currentUserId);
    }
}
