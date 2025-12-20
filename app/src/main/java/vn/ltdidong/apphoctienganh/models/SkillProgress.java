package vn.ltdidong.apphoctienganh.models;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity lưu trữ tiến độ tổng hợp của từng kỹ năng
 * Tổng hợp từ các LearningSession theo kỹ năng
 */
@Entity(tableName = "skill_progress",
        indices = {
            @Index(value = {"userId", "skillType"}, unique = true)
        })
public class SkillProgress {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    // ID người dùng
    private String userId;
    
    // Loại kỹ năng
    private String skillType;
    
    // Tổng số phiên học
    private int totalSessions;
    
    // Tổng số phiên hoàn thành
    private int completedSessions;
    
    // Điểm trung bình
    private float averageScore;
    
    // Độ chính xác trung bình (%)
    private float averageAccuracy;
    
    // Tổng thời gian học (giây)
    private long totalTimeSeconds;
    
    // Điểm cao nhất
    private float highestScore;
    
    // Điểm thấp nhất
    private float lowestScore;
    
    // Số lần luyện tập trong 7 ngày gần nhất
    private int practicesLast7Days;
    
    // Số lần luyện tập trong 30 ngày gần nhất
    private int practicesLast30Days;
    
    // Xu hướng (IMPROVING, STABLE, DECLINING)
    private String trend;
    
    // Level hiện tại của kỹ năng (1-10)
    private int level;
    
    // % tiến độ đến level tiếp theo
    private float progressToNextLevel;
    
    // Đánh giá điểm mạnh/yếu (STRONG, MEDIUM, WEAK)
    private String strengthLevel;
    
    // Thời gian cập nhật cuối
    private long lastUpdated;
    
    // Đã đồng bộ lên Firestore chưa
    private boolean synced;

    public SkillProgress() {
        this.totalSessions = 0;
        this.completedSessions = 0;
        this.averageScore = 0;
        this.averageAccuracy = 0;
        this.totalTimeSeconds = 0;
        this.highestScore = 0;
        this.lowestScore = 100;
        this.practicesLast7Days = 0;
        this.practicesLast30Days = 0;
        this.level = 1;
        this.progressToNextLevel = 0;
        this.trend = "STABLE";
        this.strengthLevel = "MEDIUM";
        this.lastUpdated = System.currentTimeMillis();
        this.synced = false;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSkillType() {
        return skillType;
    }

    public void setSkillType(String skillType) {
        this.skillType = skillType;
    }

    public int getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(int totalSessions) {
        this.totalSessions = totalSessions;
    }

    public int getCompletedSessions() {
        return completedSessions;
    }

    public void setCompletedSessions(int completedSessions) {
        this.completedSessions = completedSessions;
    }

    public float getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(float averageScore) {
        this.averageScore = averageScore;
    }

    public float getAverageAccuracy() {
        return averageAccuracy;
    }

    public void setAverageAccuracy(float averageAccuracy) {
        this.averageAccuracy = averageAccuracy;
    }

    public long getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    public void setTotalTimeSeconds(long totalTimeSeconds) {
        this.totalTimeSeconds = totalTimeSeconds;
    }

    public float getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(float highestScore) {
        this.highestScore = highestScore;
    }

    public float getLowestScore() {
        return lowestScore;
    }

    public void setLowestScore(float lowestScore) {
        this.lowestScore = lowestScore;
    }

    public int getPracticesLast7Days() {
        return practicesLast7Days;
    }

    public void setPracticesLast7Days(int practicesLast7Days) {
        this.practicesLast7Days = practicesLast7Days;
    }

    public int getPracticesLast30Days() {
        return practicesLast30Days;
    }

    public void setPracticesLast30Days(int practicesLast30Days) {
        this.practicesLast30Days = practicesLast30Days;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public float getProgressToNextLevel() {
        return progressToNextLevel;
    }

    public void setProgressToNextLevel(float progressToNextLevel) {
        this.progressToNextLevel = progressToNextLevel;
    }

    public String getStrengthLevel() {
        return strengthLevel;
    }

    public void setStrengthLevel(String strengthLevel) {
        this.strengthLevel = strengthLevel;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }
}
