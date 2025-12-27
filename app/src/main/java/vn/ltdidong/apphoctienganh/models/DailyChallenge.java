package vn.ltdidong.apphoctienganh.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Model cho Daily Challenge
 */
@Entity(tableName = "daily_challenges")
public class DailyChallenge {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String date;  // Format: yyyy-MM-dd
    private String userId;
    
    // Challenge types
    private boolean readingCompleted;
    private boolean writingCompleted;
    private boolean listeningCompleted;
    private boolean speakingCompleted;
    
    // Listening detailed tracking
    private int listeningEasyCount;  // Number of easy lessons completed
    private int listeningMediumCount;  // Number of medium lessons completed
    private int listeningHardCount;  // Number of hard lessons completed
    private int listeningFillBlankCount;  // Number of fill blank exercises completed
    private int listeningExpEarned;  // Total exp earned from listening
    
    // Additional challenges
    private boolean learnNewWordsCompleted;  // Learn 5 new words
    private int newWordsCount;
    
    private boolean practiceFlashcardCompleted;  // Practice 10 flashcards
    private int flashcardCount;
    
    private boolean completeQuizCompleted;  // Complete 1 quiz
    
    // Stats
    private int totalChallenges;
    private int completedChallenges;
    private int xpEarned;
    private boolean allCompleted;

    public DailyChallenge() {
        this.totalChallenges = 7;  // 4 skills + 3 additional
        this.completedChallenges = 0;
        this.xpEarned = 0;
        this.allCompleted = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isReadingCompleted() {
        return readingCompleted;
    }

    public void setReadingCompleted(boolean readingCompleted) {
        this.readingCompleted = readingCompleted;
    }

    public boolean isWritingCompleted() {
        return writingCompleted;
    }

    public void setWritingCompleted(boolean writingCompleted) {
        this.writingCompleted = writingCompleted;
    }

    public boolean isListeningCompleted() {
        return listeningCompleted;
    }

    public void setListeningCompleted(boolean listeningCompleted) {
        this.listeningCompleted = listeningCompleted;
    }

    public int getListeningEasyCount() {
        return listeningEasyCount;
    }

    public void setListeningEasyCount(int listeningEasyCount) {
        this.listeningEasyCount = listeningEasyCount;
    }

    public int getListeningMediumCount() {
        return listeningMediumCount;
    }

    public void setListeningMediumCount(int listeningMediumCount) {
        this.listeningMediumCount = listeningMediumCount;
    }

    public int getListeningHardCount() {
        return listeningHardCount;
    }

    public void setListeningHardCount(int listeningHardCount) {
        this.listeningHardCount = listeningHardCount;
    }

    public int getListeningFillBlankCount() {
        return listeningFillBlankCount;
    }

    public void setListeningFillBlankCount(int listeningFillBlankCount) {
        this.listeningFillBlankCount = listeningFillBlankCount;
    }

    public int getListeningExpEarned() {
        return listeningExpEarned;
    }

    public void setListeningExpEarned(int listeningExpEarned) {
        this.listeningExpEarned = listeningExpEarned;
    }

    public boolean isSpeakingCompleted() {
        return speakingCompleted;
    }

    public void setSpeakingCompleted(boolean speakingCompleted) {
        this.speakingCompleted = speakingCompleted;
    }

    public boolean isLearnNewWordsCompleted() {
        return learnNewWordsCompleted;
    }

    public void setLearnNewWordsCompleted(boolean learnNewWordsCompleted) {
        this.learnNewWordsCompleted = learnNewWordsCompleted;
    }

    public int getNewWordsCount() {
        return newWordsCount;
    }

    public void setNewWordsCount(int newWordsCount) {
        this.newWordsCount = newWordsCount;
    }

    public boolean isPracticeFlashcardCompleted() {
        return practiceFlashcardCompleted;
    }

    public void setPracticeFlashcardCompleted(boolean practiceFlashcardCompleted) {
        this.practiceFlashcardCompleted = practiceFlashcardCompleted;
    }

    public int getFlashcardCount() {
        return flashcardCount;
    }

    public void setFlashcardCount(int flashcardCount) {
        this.flashcardCount = flashcardCount;
    }

    public boolean isCompleteQuizCompleted() {
        return completeQuizCompleted;
    }

    public void setCompleteQuizCompleted(boolean completeQuizCompleted) {
        this.completeQuizCompleted = completeQuizCompleted;
    }

    public int getTotalChallenges() {
        return totalChallenges;
    }

    public void setTotalChallenges(int totalChallenges) {
        this.totalChallenges = totalChallenges;
    }

    public int getCompletedChallenges() {
        return completedChallenges;
    }

    public void setCompletedChallenges(int completedChallenges) {
        this.completedChallenges = completedChallenges;
    }

    public int getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(int xpEarned) {
        this.xpEarned = xpEarned;
    }

    public boolean isAllCompleted() {
        return allCompleted;
    }

    public void setAllCompleted(boolean allCompleted) {
        this.allCompleted = allCompleted;
    }

    /**
     * Tính toán số challenge đã hoàn thành
     */
    public void calculateProgress() {
        int completed = 0;
        if (readingCompleted) completed++;
        if (writingCompleted) completed++;
        if (listeningCompleted) completed++;
        if (speakingCompleted) completed++;
        if (learnNewWordsCompleted) completed++;
        if (practiceFlashcardCompleted) completed++;
        if (completeQuizCompleted) completed++;
        
        this.completedChallenges = completed;
        this.allCompleted = (completed == totalChallenges);
    }

    /**
     * Tính XP dựa trên số challenge hoàn thành
     */
    public void calculateXP() {
        int xp = completedChallenges * 10;  // 10 XP per challenge
        if (allCompleted) {
            xp += 50;  // Bonus 50 XP for completing all
        }
        this.xpEarned = xp;
    }

    public int getProgressPercentage() {
        if (totalChallenges == 0) return 0;
        return (completedChallenges * 100) / totalChallenges;
    }
}
