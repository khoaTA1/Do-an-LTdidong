package vn.ltdidong.apphoctienganh.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.ltdidong.apphoctienganh.models.SkillProgress;

/**
 * DAO cho SkillProgress - truy cập tiến độ theo kỹ năng
 */
@Dao
public interface SkillProgressDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SkillProgress skillProgress);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SkillProgress> skillProgressList);
    
    @Update
    void update(SkillProgress skillProgress);
    
    @Delete
    void delete(SkillProgress skillProgress);
    
    // Lấy tất cả tiến độ của user
    @Query("SELECT * FROM skill_progress WHERE userId = :userId")
    List<SkillProgress> getAllByUser(String userId);
    
    // Lấy tiến độ theo kỹ năng cụ thể
    @Query("SELECT * FROM skill_progress WHERE userId = :userId AND skillType = :skillType")
    SkillProgress getBySkillType(String userId, String skillType);
    
    // Lấy các kỹ năng yếu (averageScore < threshold)
    @Query("SELECT * FROM skill_progress WHERE userId = :userId AND averageScore < :threshold ORDER BY averageScore ASC")
    List<SkillProgress> getWeakSkills(String userId, float threshold);
    
    // Lấy các kỹ năng mạnh (averageScore >= threshold)
    @Query("SELECT * FROM skill_progress WHERE userId = :userId AND averageScore >= :threshold ORDER BY averageScore DESC")
    List<SkillProgress> getStrongSkills(String userId, float threshold);
    
    // Lấy kỹ năng được luyện nhiều nhất
    @Query("SELECT * FROM skill_progress WHERE userId = :userId ORDER BY totalSessions DESC LIMIT 1")
    SkillProgress getMostPracticedSkill(String userId);
    
    // Lấy kỹ năng được luyện ít nhất
    @Query("SELECT * FROM skill_progress WHERE userId = :userId AND totalSessions > 0 ORDER BY totalSessions ASC LIMIT 1")
    SkillProgress getLeastPracticedSkill(String userId);
    
    // Lấy các tiến độ chưa đồng bộ
    @Query("SELECT * FROM skill_progress WHERE userId = :userId AND synced = 0")
    List<SkillProgress> getUnsyncedProgress(String userId);
    
    // Đánh dấu đã đồng bộ
    @Query("UPDATE skill_progress SET synced = 1 WHERE id = :id")
    void markAsSynced(long id);
    
    // Lấy kỹ năng theo mức độ mạnh/yếu
    @Query("SELECT * FROM skill_progress WHERE userId = :userId AND strengthLevel = :strengthLevel")
    List<SkillProgress> getByStrengthLevel(String userId, String strengthLevel);
    
    // Lấy kỹ năng theo xu hướng
    @Query("SELECT * FROM skill_progress WHERE userId = :userId AND trend = :trend")
    List<SkillProgress> getByTrend(String userId, String trend);
    
    // Tính điểm trung bình tất cả kỹ năng
    @Query("SELECT AVG(averageScore) FROM skill_progress WHERE userId = :userId")
    Float getOverallAverageScore(String userId);
    
    // Đếm số kỹ năng đã luyện tập
    @Query("SELECT COUNT(*) FROM skill_progress WHERE userId = :userId AND totalSessions > 0")
    int countPracticedSkills(String userId);
    
    // Xóa tất cả dữ liệu của user
    @Query("DELETE FROM skill_progress WHERE userId = :userId")
    void deleteAllByUser(String userId);
}
