package vn.ltdidong.apphoctienganh.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import vn.ltdidong.apphoctienganh.models.ListeningLesson;

/**
 * Entity theo dõi tiến độ học tập của người dùng
 * Lưu điểm số, thời gian hoàn thành và trạng thái của từng bài học
 */
@Entity(tableName = "user_progress",
        foreignKeys = @ForeignKey(
                entity = ListeningLesson.class,
                parentColumns = "id",
                childColumns = "lessonId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index(value = "lessonId")})
public class UserProgress {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    // ID của bài học
    private int lessonId;
    
    // Số câu trả lời đúng
    private int correctAnswers;
    
    // Tổng số câu hỏi
    private int totalQuestions;
    
    // Điểm số (tính theo %)
    private float score;
    
    // Trạng thái: NOT_STARTED, IN_PROGRESS, COMPLETED
    private String status;
    
    // Thời gian hoàn thành (timestamp)
    private long completedAt;
    
    // Số lần đã làm bài này
    private int attempts;
    
    // Điểm cao nhất đạt được
    private float bestScore;

    // Constructor
    public UserProgress() {
        this.status = "NOT_STARTED";
        this.attempts = 0;
        this.bestScore = 0;
    }

    // @Ignore annotation để Room không sử dụng constructor này
    @Ignore
    public UserProgress(int lessonId, int correctAnswers, int totalQuestions, 
                       float score, String status, long completedAt) {
        this.lessonId = lessonId;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
        this.score = score;
        this.status = status;
        this.completedAt = completedAt;
        this.attempts = 1;
        this.bestScore = score;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLessonId() {
        return lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
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

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public float getBestScore() {
        return bestScore;
    }

    public void setBestScore(float bestScore) {
        this.bestScore = bestScore;
    }
    
    /**
     * Helper method để format điểm số
     * @return String điểm số theo format "X/Y (Z%)"
     */
    public String getFormattedScore() {
        return String.format("%d/%d (%.0f%%)", correctAnswers, totalQuestions, score);
    }
    
    /**
     * Cập nhật điểm số tốt nhất nếu điểm hiện tại cao hơn
     */
    public void updateBestScore() {
        if (score > bestScore) {
            bestScore = score;
        }
    }
}
