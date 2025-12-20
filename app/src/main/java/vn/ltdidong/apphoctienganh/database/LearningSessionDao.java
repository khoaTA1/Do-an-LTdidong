package vn.ltdidong.apphoctienganh.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.ltdidong.apphoctienganh.models.LearningSession;

/**
 * DAO cho LearningSession - truy cập dữ liệu phiên học
 */
@Dao
public interface LearningSessionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LearningSession session);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LearningSession> sessions);
    
    @Update
    void update(LearningSession session);
    
    @Delete
    void delete(LearningSession session);
    
    // Lấy tất cả phiên học của user
    @Query("SELECT * FROM learning_sessions WHERE userId = :userId ORDER BY startTime DESC")
    List<LearningSession> getAllByUser(String userId);
    
    // Lấy phiên học theo kỹ năng
    @Query("SELECT * FROM learning_sessions WHERE userId = :userId AND skillType = :skillType ORDER BY startTime DESC")
    List<LearningSession> getBySkillType(String userId, String skillType);
    
    // Lấy phiên học trong khoảng thời gian
    @Query("SELECT * FROM learning_sessions WHERE userId = :userId AND startTime >= :startTime AND startTime <= :endTime ORDER BY startTime DESC")
    List<LearningSession> getByTimeRange(String userId, long startTime, long endTime);
    
    // Lấy phiên học theo kỹ năng và khoảng thời gian
    @Query("SELECT * FROM learning_sessions WHERE userId = :userId AND skillType = :skillType AND startTime >= :startTime AND startTime <= :endTime ORDER BY startTime DESC")
    List<LearningSession> getBySkillTypeAndTimeRange(String userId, String skillType, long startTime, long endTime);
    
    // Lấy các phiên học hoàn thành
    @Query("SELECT * FROM learning_sessions WHERE userId = :userId AND completed = 1 ORDER BY startTime DESC")
    List<LearningSession> getCompletedSessions(String userId);
    
    // Lấy các phiên học chưa đồng bộ
    @Query("SELECT * FROM learning_sessions WHERE userId = :userId AND synced = 0")
    List<LearningSession> getUnsyncedSessions(String userId);
    
    // Đếm số phiên học theo kỹ năng
    @Query("SELECT COUNT(*) FROM learning_sessions WHERE userId = :userId AND skillType = :skillType AND completed = 1")
    int countSessionsBySkill(String userId, String skillType);
    
    // Tính tổng thời gian học theo kỹ năng
    @Query("SELECT SUM(durationSeconds) FROM learning_sessions WHERE userId = :userId AND skillType = :skillType AND completed = 1")
    Long getTotalTimeBySkill(String userId, String skillType);
    
    // Tính điểm trung bình theo kỹ năng
    @Query("SELECT AVG(score) FROM learning_sessions WHERE userId = :userId AND skillType = :skillType AND completed = 1")
    Float getAverageScoreBySkill(String userId, String skillType);
    
    // Lấy điểm cao nhất theo kỹ năng
    @Query("SELECT MAX(score) FROM learning_sessions WHERE userId = :userId AND skillType = :skillType AND completed = 1")
    Float getHighestScoreBySkill(String userId, String skillType);
    
    // Lấy điểm thấp nhất theo kỹ năng
    @Query("SELECT MIN(score) FROM learning_sessions WHERE userId = :userId AND skillType = :skillType AND completed = 1")
    Float getLowestScoreBySkill(String userId, String skillType);
    
    // Đếm số phiên trong N ngày gần nhất
    @Query("SELECT COUNT(*) FROM learning_sessions WHERE userId = :userId AND skillType = :skillType AND completed = 1 AND startTime >= :sinceTime")
    int countSessionsSince(String userId, String skillType, long sinceTime);
    
    // Lấy phiên học gần nhất
    @Query("SELECT * FROM learning_sessions WHERE userId = :userId ORDER BY startTime DESC LIMIT 1")
    LearningSession getLatestSession(String userId);
    
    // Lấy N phiên học gần nhất
    @Query("SELECT * FROM learning_sessions WHERE userId = :userId ORDER BY startTime DESC LIMIT :limit")
    List<LearningSession> getRecentSessions(String userId, int limit);
    
    // Xóa các phiên cũ (data cleanup)
    @Query("DELETE FROM learning_sessions WHERE userId = :userId AND startTime < :beforeTime")
    void deleteOldSessions(String userId, long beforeTime);
    
    // Đánh dấu đã đồng bộ
    @Query("UPDATE learning_sessions SET synced = 1, firestoreId = :firestoreId WHERE id = :id")
    void markAsSynced(long id, String firestoreId);
    
    // Xóa tất cả dữ liệu của user
    @Query("DELETE FROM learning_sessions WHERE userId = :userId")
    void deleteAllByUser(String userId);
}
