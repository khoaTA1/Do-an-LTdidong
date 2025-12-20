package vn.ltdidong.apphoctienganh.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.ltdidong.apphoctienganh.models.StudySchedule;

/**
 * DAO cho StudySchedule - quản lý lịch học tự động
 */
@Dao
public interface StudyScheduleDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(StudySchedule schedule);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StudySchedule> schedules);
    
    @Update
    void update(StudySchedule schedule);
    
    @Delete
    void delete(StudySchedule schedule);
    
    // Lấy tất cả lịch của user
    @Query("SELECT * FROM study_schedules WHERE userId = :userId ORDER BY scheduledDate ASC")
    List<StudySchedule> getAllByUser(String userId);
    
    // Lấy lịch theo trạng thái
    @Query("SELECT * FROM study_schedules WHERE userId = :userId AND status = :status ORDER BY scheduledDate ASC")
    List<StudySchedule> getByStatus(String userId, String status);
    
    // Lấy lịch đang chờ (PENDING)
    @Query("SELECT * FROM study_schedules WHERE userId = :userId AND status = 'PENDING' ORDER BY priority DESC, scheduledDate ASC")
    List<StudySchedule> getPendingSchedules(String userId);
    
    // Lấy lịch trong khoảng thời gian
    @Query("SELECT * FROM study_schedules WHERE userId = :userId AND scheduledDate >= :startDate AND scheduledDate <= :endDate ORDER BY scheduledDate ASC")
    List<StudySchedule> getByDateRange(String userId, long startDate, long endDate);
    
    // Lấy lịch theo ngày cụ thể
    @Query("SELECT * FROM study_schedules WHERE userId = :userId AND DATE(scheduledDate/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch') ORDER BY priority DESC")
    List<StudySchedule> getByDate(String userId, long date);
    
    // Lấy lịch theo kỹ năng
    @Query("SELECT * FROM study_schedules WHERE userId = :userId AND skillType = :skillType AND status = 'PENDING' ORDER BY scheduledDate ASC")
    List<StudySchedule> getBySkillType(String userId, String skillType);
    
    // Lấy lịch ưu tiên cao
    @Query("SELECT * FROM study_schedules WHERE userId = :userId AND status = 'PENDING' AND priority >= :minPriority ORDER BY priority DESC, scheduledDate ASC")
    List<StudySchedule> getHighPrioritySchedules(String userId, int minPriority);
    
    // Lấy lịch chưa đồng bộ
    @Query("SELECT * FROM study_schedules WHERE userId = :userId AND synced = 0")
    List<StudySchedule> getUnsyncedSchedules(String userId);
    
    // Đánh dấu đã đồng bộ
    @Query("UPDATE study_schedules SET synced = 1, firestoreId = :firestoreId WHERE id = :id")
    void markAsSynced(long id, String firestoreId);
    
    // Cập nhật trạng thái
    @Query("UPDATE study_schedules SET status = :status, lastUpdated = :timestamp WHERE id = :id")
    void updateStatus(long id, String status, long timestamp);
    
    // Đánh dấu hoàn thành
    @Query("UPDATE study_schedules SET status = 'COMPLETED', actualCompletedAt = :completedAt, actualScore = :score, actualDurationMinutes = :duration, lastUpdated = :timestamp WHERE id = :id")
    void markAsCompleted(long id, long completedAt, float score, int duration, long timestamp);
    
    // Đánh dấu bỏ qua
    @Query("UPDATE study_schedules SET status = 'SKIPPED', lastUpdated = :timestamp WHERE id = :id")
    void markAsSkipped(long id, long timestamp);
    
    // Đánh dấu hết hạn các lịch cũ
    @Query("UPDATE study_schedules SET status = 'EXPIRED', lastUpdated = :timestamp WHERE userId = :userId AND status = 'PENDING' AND scheduledDate < :beforeDate")
    void expireOldSchedules(String userId, long beforeDate, long timestamp);
    
    // Xóa các lịch cũ đã hoàn thành/hết hạn
    @Query("DELETE FROM study_schedules WHERE userId = :userId AND (status = 'COMPLETED' OR status = 'EXPIRED') AND scheduledDate < :beforeDate")
    void deleteOldSchedules(String userId, long beforeDate);
    
    // Đếm số lịch theo trạng thái
    @Query("SELECT COUNT(*) FROM study_schedules WHERE userId = :userId AND status = :status")
    int countByStatus(String userId, String status);
    
    // Xóa tất cả lịch của user
    @Query("DELETE FROM study_schedules WHERE userId = :userId")
    void deleteAllByUser(String userId);
}
