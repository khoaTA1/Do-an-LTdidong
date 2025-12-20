package vn.ltdidong.apphoctienganh.models;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity lưu thông tin về mỗi phiên học của user
 * Bao gồm điểm số, độ chính xác, thời gian học, độ khó,...
 */
@Entity(tableName = "learning_sessions",
        indices = {
            @Index(value = "userId"),
            @Index(value = "skillType"),
            @Index(value = "startTime")
        })
public class LearningSession {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    // ID người dùng
    private String userId;
    
    // Kỹ năng học (LISTENING, SPEAKING, READING, WRITING)
    private String skillType;
    
    // ID bài học (nếu có)
    private Integer lessonId;
    
    // Tên bài học
    private String lessonName;
    
    // Điểm số đạt được (0-100)
    private float score;
    
    // Số câu đúng
    private int correctAnswers;
    
    // Tổng số câu
    private int totalQuestions;
    
    // Độ chính xác (%) = correctAnswers / totalQuestions * 100
    private float accuracy;
    
    // Thời gian bắt đầu (timestamp milliseconds)
    private long startTime;
    
    // Thời gian kết thúc (timestamp milliseconds)
    private long endTime;
    
    // Thời gian học (giây)
    private int durationSeconds;
    
    // Độ khó bài học (EASY, MEDIUM, HARD)
    private String difficulty;
    
    // Có hoàn thành không
    private boolean completed;
    
    // Đã đồng bộ lên Firestore chưa
    private boolean synced;
    
    // ID trên Firestore (nếu đã sync)
    private String firestoreId;
    
    // Thời gian tạo
    private long createdAt;

    public LearningSession() {
        this.createdAt = System.currentTimeMillis();
        this.synced = false;
        this.completed = false;
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

    public Integer getLessonId() {
        return lessonId;
    }

    public void setLessonId(Integer lessonId) {
        this.lessonId = lessonId;
    }

    public String getLessonName() {
        return lessonName;
    }

    public void setLessonName(String lessonName) {
        this.lessonName = lessonName;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
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

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Tính toán accuracy từ correctAnswers và totalQuestions
     */
    public void calculateAccuracy() {
        if (totalQuestions > 0) {
            this.accuracy = (correctAnswers * 100.0f) / totalQuestions;
        } else {
            this.accuracy = 0;
        }
    }

    /**
     * Tính toán duration từ startTime và endTime
     */
    public void calculateDuration() {
        if (endTime > startTime) {
            this.durationSeconds = (int) ((endTime - startTime) / 1000);
        } else {
            this.durationSeconds = 0;
        }
    }
}
