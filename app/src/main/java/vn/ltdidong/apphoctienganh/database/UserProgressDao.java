package vn.ltdidong.apphoctienganh.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import vn.ltdidong.apphoctienganh.models.UserProgress;

import java.util.List;

/**
 * DAO cho UserProgress entity
 * Quản lý tiến độ học tập của người dùng theo từng userId
 */
@Dao
public interface UserProgressDao {
    
    /**
     * Lấy tất cả tiến độ của một user cụ thể
     * @param userId ID của user
     */
    @Query("SELECT * FROM user_progress WHERE userId = :userId ORDER BY completedAt DESC")
    LiveData<List<UserProgress>> getAllProgressByUser(String userId);
    
    /**
     * Lấy tiến độ của một bài học cụ thể cho user
     * @param userId ID của user
     * @param lessonId ID của bài học
     */
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND lessonId = :lessonId LIMIT 1")
    LiveData<UserProgress> getProgressByUserAndLesson(String userId, int lessonId);
    
    /**
     * Lấy tiến độ của một bài học (không dùng LiveData)
     */
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND lessonId = :lessonId LIMIT 1")
    UserProgress getProgressByUserAndLessonSync(String userId, int lessonId);
    
    /**
     * Lấy tất cả bài học đã hoàn thành của user
     */
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND status = 'COMPLETED' ORDER BY completedAt DESC")
    LiveData<List<UserProgress>> getCompletedLessonsByUser(String userId);
    
    /**
     * Lấy tất cả bài học đang làm dở của user
     */
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND status = 'IN_PROGRESS' ORDER BY completedAt DESC")
    LiveData<List<UserProgress>> getInProgressLessonsByUser(String userId);
    
    /**
     * Thêm hoặc cập nhật tiến độ
     * OnConflictStrategy.REPLACE: nếu đã tồn tại thì sẽ replace
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertProgress(UserProgress progress);
    
    /**
     * Cập nhật tiến độ
     */
    @Update
    void updateProgress(UserProgress progress);
    
    /**
     * Xóa tiến độ
     */
    @Delete
    void deleteProgress(UserProgress progress);
    
    /**
     * Xóa tiến độ của một user cho một bài học
     */
    @Query("DELETE FROM user_progress WHERE userId = :userId AND lessonId = :lessonId")
    void deleteProgressByUserAndLesson(String userId, int lessonId);
    
    /**
     * Xóa tất cả tiến độ của một user
     */
    @Query("DELETE FROM user_progress WHERE userId = :userId")
    void deleteAllProgressByUser(String userId);
    
    /**
     * Tính tổng điểm trung bình của user
     */
    @Query("SELECT AVG(score) FROM user_progress WHERE userId = :userId AND status = 'COMPLETED'")
    LiveData<Float> getAverageScoreByUser(String userId);
    
    /**
     * Đếm số bài đã hoàn thành của user
     */
    @Query("SELECT COUNT(*) FROM user_progress WHERE userId = :userId AND status = 'COMPLETED'")
    LiveData<Integer> getCompletedLessonCountByUser(String userId);
    
    /**
     * Lấy điểm cao nhất của user
     */
    @Query("SELECT MAX(bestScore) FROM user_progress WHERE userId = :userId")
    LiveData<Float> getHighestScoreByUser(String userId);
}
