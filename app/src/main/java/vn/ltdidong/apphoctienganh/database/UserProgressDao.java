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
 * Quản lý tiến độ học tập của người dùng
 */
@Dao
public interface UserProgressDao {
    
    /**
     * Lấy tất cả tiến độ của người dùng
     */
    @Query("SELECT * FROM user_progress ORDER BY completedAt DESC")
    LiveData<List<UserProgress>> getAllProgress();
    
    /**
     * Lấy tiến độ của một bài học cụ thể
     * @param lessonId ID của bài học
     */
    @Query("SELECT * FROM user_progress WHERE lessonId = :lessonId LIMIT 1")
    LiveData<UserProgress> getProgressByLesson(int lessonId);
    
    /**
     * Lấy tiến độ của một bài học (không dùng LiveData)
     */
    @Query("SELECT * FROM user_progress WHERE lessonId = :lessonId LIMIT 1")
    UserProgress getProgressByLessonSync(int lessonId);
    
    /**
     * Lấy tất cả bài học đã hoàn thành
     */
    @Query("SELECT * FROM user_progress WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    LiveData<List<UserProgress>> getCompletedLessons();
    
    /**
     * Lấy tất cả bài học đang làm dở
     */
    @Query("SELECT * FROM user_progress WHERE status = 'IN_PROGRESS' ORDER BY completedAt DESC")
    LiveData<List<UserProgress>> getInProgressLessons();
    
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
     * Xóa tiến độ của một bài học
     */
    @Query("DELETE FROM user_progress WHERE lessonId = :lessonId")
    void deleteProgressByLesson(int lessonId);
    
    /**
     * Xóa tất cả tiến độ
     */
    @Query("DELETE FROM user_progress")
    void deleteAllProgress();
    
    /**
     * Tính tổng điểm trung bình của tất cả bài đã hoàn thành
     */
    @Query("SELECT AVG(score) FROM user_progress WHERE status = 'COMPLETED'")
    LiveData<Float> getAverageScore();
    
    /**
     * Đếm số bài đã hoàn thành
     */
    @Query("SELECT COUNT(*) FROM user_progress WHERE status = 'COMPLETED'")
    LiveData<Integer> getCompletedLessonCount();
    
    /**
     * Lấy điểm cao nhất
     */
    @Query("SELECT MAX(bestScore) FROM user_progress")
    LiveData<Float> getHighestScore();
}
