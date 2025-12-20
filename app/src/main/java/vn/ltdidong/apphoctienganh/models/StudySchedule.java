package vn.ltdidong.apphoctienganh.models;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity lưu lịch học được đề xuất tự động
 * Dựa trên phân tích thói quen và điểm mạnh/yếu
 */
@Entity(tableName = "study_schedules",
        indices = {
            @Index(value = "userId"),
            @Index(value = "scheduledDate")
        })
public class StudySchedule {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    // ID người dùng
    private String userId;
    
    // Ngày dự kiến học (timestamp)
    private long scheduledDate;
    
    // Kỹ năng cần học
    private String skillType;
    
    // Thời lượng đề xuất (phút)
    private int recommendedDurationMinutes;
    
    // Độ khó đề xuất (EASY, MEDIUM, HARD)
    private String recommendedDifficulty;
    
    // Giờ đề xuất bắt đầu (0-23)
    private int recommendedStartHour;
    
    // Lý do đề xuất
    private String reason;
    
    // Mức độ ưu tiên (1-5, 5 là cao nhất)
    private int priority;
    
    // Trạng thái (PENDING, COMPLETED, SKIPPED, EXPIRED)
    private String status;
    
    // Thời gian hoàn thành thực tế (nếu đã hoàn thành)
    private Long actualCompletedAt;
    
    // Điểm đạt được (nếu đã hoàn thành)
    private Float actualScore;
    
    // Thời gian học thực tế (phút, nếu đã hoàn thành)
    private Integer actualDurationMinutes;
    
    // Thời gian tạo lịch
    private long createdAt;
    
    // Thời gian cập nhật
    private long lastUpdated;
    
    // Đã gửi thông báo chưa
    private boolean notificationSent;
    
    // Đã đồng bộ lên Firestore chưa
    private boolean synced;
    
    // ID trên Firestore (nếu đã sync)
    private String firestoreId;

    public StudySchedule() {
        this.status = "PENDING";
        this.priority = 3;
        this.createdAt = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
        this.notificationSent = false;
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

    public long getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(long scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getSkillType() {
        return skillType;
    }

    public void setSkillType(String skillType) {
        this.skillType = skillType;
    }

    public int getRecommendedDurationMinutes() {
        return recommendedDurationMinutes;
    }

    public void setRecommendedDurationMinutes(int recommendedDurationMinutes) {
        this.recommendedDurationMinutes = recommendedDurationMinutes;
    }

    public String getRecommendedDifficulty() {
        return recommendedDifficulty;
    }

    public void setRecommendedDifficulty(String recommendedDifficulty) {
        this.recommendedDifficulty = recommendedDifficulty;
    }

    public int getRecommendedStartHour() {
        return recommendedStartHour;
    }

    public void setRecommendedStartHour(int recommendedStartHour) {
        this.recommendedStartHour = recommendedStartHour;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getActualCompletedAt() {
        return actualCompletedAt;
    }

    public void setActualCompletedAt(Long actualCompletedAt) {
        this.actualCompletedAt = actualCompletedAt;
    }

    public Float getActualScore() {
        return actualScore;
    }

    public void setActualScore(Float actualScore) {
        this.actualScore = actualScore;
    }

    public Integer getActualDurationMinutes() {
        return actualDurationMinutes;
    }

    public void setActualDurationMinutes(Integer actualDurationMinutes) {
        this.actualDurationMinutes = actualDurationMinutes;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isNotificationSent() {
        return notificationSent;
    }

    public void setNotificationSent(boolean notificationSent) {
        this.notificationSent = notificationSent;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public String getFirestoreId() {
        return firestoreId;
    }

    public void setFirestoreId(String firestoreId) {
        this.firestoreId = firestoreId;
    }
}
