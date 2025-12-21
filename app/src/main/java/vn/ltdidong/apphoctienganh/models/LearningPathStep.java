package vn.ltdidong.apphoctienganh.models;

/**
 * Model cho lộ trình học cá nhân hóa
 */
public class LearningPathStep {
    private String stepId;
    private String userId;
    private int stepNumber;
    private String skillType; // LISTENING, READING, WRITING, SPEAKING
    private String title;
    private String description;
    private String difficulty; // EASY, MEDIUM, HARD
    private int estimatedMinutes;
    private boolean isCompleted;
    private long completedAt;
    private int score;
    private String reason; // Lý do AI đề xuất step này
    
    public LearningPathStep() {}

    public LearningPathStep(String userId, int stepNumber, String skillType, 
                           String title, String difficulty, int estimatedMinutes, String reason) {
        this.userId = userId;
        this.stepNumber = stepNumber;
        this.skillType = skillType;
        this.title = title;
        this.difficulty = difficulty;
        this.estimatedMinutes = estimatedMinutes;
        this.reason = reason;
        this.isCompleted = false;
    }

    // Getters and Setters
    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getSkillType() {
        return skillType;
    }

    public void setSkillType(String skillType) {
        this.skillType = skillType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(int estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
