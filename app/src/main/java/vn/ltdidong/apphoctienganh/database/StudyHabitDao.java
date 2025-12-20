package vn.ltdidong.apphoctienganh.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import vn.ltdidong.apphoctienganh.models.StudyHabit;

/**
 * DAO cho StudyHabit - truy cập thói quen học tập
 */
@Dao
public interface StudyHabitDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(StudyHabit studyHabit);
    
    @Update
    void update(StudyHabit studyHabit);
    
    @Delete
    void delete(StudyHabit studyHabit);
    
    // Lấy thói quen của user (mỗi user chỉ có 1 record)
    @Query("SELECT * FROM study_habits WHERE userId = :userId LIMIT 1")
    StudyHabit getByUser(String userId);
    
    // Lấy thói quen chưa đồng bộ
    @Query("SELECT * FROM study_habits WHERE userId = :userId AND synced = 0 LIMIT 1")
    StudyHabit getUnsyncedHabit(String userId);
    
    // Đánh dấu đã đồng bộ
    @Query("UPDATE study_habits SET synced = 1 WHERE id = :id")
    void markAsSynced(long id);
    
    // Cập nhật streak hiện tại
    @Query("UPDATE study_habits SET currentStreak = :streak, lastUpdated = :timestamp WHERE userId = :userId")
    void updateStreak(String userId, int streak, long timestamp);
    
    // Cập nhật longest streak
    @Query("UPDATE study_habits SET longestStreak = :streak, lastUpdated = :timestamp WHERE userId = :userId")
    void updateLongestStreak(String userId, int streak, long timestamp);
    
    // Xóa dữ liệu của user
    @Query("DELETE FROM study_habits WHERE userId = :userId")
    void deleteByUser(String userId);
}
