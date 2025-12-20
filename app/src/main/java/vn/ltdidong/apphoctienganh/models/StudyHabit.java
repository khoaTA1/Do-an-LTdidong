package vn.ltdidong.apphoctienganh.models;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity lưu thông tin thói quen học tập của user
 * Phân tích theo ngày, giờ, thời lượng, tần suất
 */
@Entity(tableName = "study_habits",
        indices = {
            @Index(value = "userId")
        })
public class StudyHabit {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    // ID người dùng
    private String userId;
    
    // Ngày ưa thích học nhất (0=CN, 1=T2, ..., 6=T7)
    private int preferredDayOfWeek;
    
    // Giờ ưa thích học nhất (0-23)
    private int preferredHourOfDay;
    
    // Thời lượng học trung bình mỗi phiên (phút)
    private int averageSessionMinutes;
    
    // Tần suất học hàng tuần (số phiên/tuần)
    private float weeklyFrequency;
    
    // Tần suất học hàng tháng (số phiên/tháng)
    private float monthlyFrequency;
    
    // Chuỗi ngày học liên tiếp hiện tại
    private int currentStreak;
    
    // Chuỗi ngày học dài nhất
    private int longestStreak;
    
    // Tổng số ngày đã học
    private int totalStudyDays;
    
    // Kỹ năng được luyện nhiều nhất
    private String mostPracticedSkill;
    
    // Kỹ năng được luyện ít nhất
    private String leastPracticedSkill;
    
    // Thời gian tốt nhất trong ngày (MORNING, AFTERNOON, EVENING, NIGHT)
    private String bestTimeOfDay;
    
    // Ngày cuối cùng học
    private long lastStudyDate;
    
    // Thời gian cập nhật
    private long lastUpdated;
    
    // Đã đồng bộ lên Firestore chưa
    private boolean synced;

    public StudyHabit() {
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.totalStudyDays = 0;
        this.averageSessionMinutes = 0;
        this.weeklyFrequency = 0;
        this.monthlyFrequency = 0;
        this.preferredDayOfWeek = -1;
        this.preferredHourOfDay = -1;
        this.bestTimeOfDay = "UNKNOWN";
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

    public int getPreferredDayOfWeek() {
        return preferredDayOfWeek;
    }

    public void setPreferredDayOfWeek(int preferredDayOfWeek) {
        this.preferredDayOfWeek = preferredDayOfWeek;
    }

    public int getPreferredHourOfDay() {
        return preferredHourOfDay;
    }

    public void setPreferredHourOfDay(int preferredHourOfDay) {
        this.preferredHourOfDay = preferredHourOfDay;
    }

    public int getAverageSessionMinutes() {
        return averageSessionMinutes;
    }

    public void setAverageSessionMinutes(int averageSessionMinutes) {
        this.averageSessionMinutes = averageSessionMinutes;
    }

    public float getWeeklyFrequency() {
        return weeklyFrequency;
    }

    public void setWeeklyFrequency(float weeklyFrequency) {
        this.weeklyFrequency = weeklyFrequency;
    }

    public float getMonthlyFrequency() {
        return monthlyFrequency;
    }

    public void setMonthlyFrequency(float monthlyFrequency) {
        this.monthlyFrequency = monthlyFrequency;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public int getTotalStudyDays() {
        return totalStudyDays;
    }

    public void setTotalStudyDays(int totalStudyDays) {
        this.totalStudyDays = totalStudyDays;
    }

    public String getMostPracticedSkill() {
        return mostPracticedSkill;
    }

    public void setMostPracticedSkill(String mostPracticedSkill) {
        this.mostPracticedSkill = mostPracticedSkill;
    }

    public String getLeastPracticedSkill() {
        return leastPracticedSkill;
    }

    public void setLeastPracticedSkill(String leastPracticedSkill) {
        this.leastPracticedSkill = leastPracticedSkill;
    }

    public String getBestTimeOfDay() {
        return bestTimeOfDay;
    }

    public void setBestTimeOfDay(String bestTimeOfDay) {
        this.bestTimeOfDay = bestTimeOfDay;
    }

    public long getLastStudyDate() {
        return lastStudyDate;
    }

    public void setLastStudyDate(long lastStudyDate) {
        this.lastStudyDate = lastStudyDate;
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
