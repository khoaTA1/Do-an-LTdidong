package vn.ltdidong.apphoctienganh.models;

/**
 * Model cho Friend Challenge - Thách đấu giữa bạn bè
 */
public class FriendChallenge {
    private String id;
    private String challengerId;        // Người thách đấu
    private String challengerName;
    private String challengerAvatar;
    private String opponentId;          // Người bị thách đấu
    private String opponentName;
    private String opponentAvatar;
    
    private String challengeType;       // "vocabulary", "grammar", "speaking", "listening", "mixed"
    private String challengeTitle;
    private String challengeDescription;
    private int totalQuestions;
    
    private String status;              // "pending", "accepted", "declined", "in_progress", "completed", "expired"
    private long createdAt;
    private long expiresAt;
    private long acceptedAt;
    private long completedAt;
    
    // Scores
    private int challengerScore;
    private int opponentScore;
    private boolean challengerCompleted;
    private boolean opponentCompleted;
    
    // Results
    private String winnerId;
    private String winnerName;
    private int xpReward;
    
    public FriendChallenge() {
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = createdAt + (24 * 60 * 60 * 1000); // 24 hours
        this.status = "pending";
        this.challengerScore = 0;
        this.opponentScore = 0;
        this.challengerCompleted = false;
        this.opponentCompleted = false;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getChallengerId() {
        return challengerId;
    }
    
    public void setChallengerId(String challengerId) {
        this.challengerId = challengerId;
    }
    
    public String getChallengerName() {
        return challengerName;
    }
    
    public void setChallengerName(String challengerName) {
        this.challengerName = challengerName;
    }
    
    public String getChallengerAvatar() {
        return challengerAvatar;
    }
    
    public void setChallengerAvatar(String challengerAvatar) {
        this.challengerAvatar = challengerAvatar;
    }
    
    public String getOpponentId() {
        return opponentId;
    }
    
    public void setOpponentId(String opponentId) {
        this.opponentId = opponentId;
    }
    
    public String getOpponentName() {
        return opponentName;
    }
    
    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }
    
    public String getOpponentAvatar() {
        return opponentAvatar;
    }
    
    public void setOpponentAvatar(String opponentAvatar) {
        this.opponentAvatar = opponentAvatar;
    }
    
    public String getChallengeType() {
        return challengeType;
    }
    
    public void setChallengeType(String challengeType) {
        this.challengeType = challengeType;
    }
    
    public String getChallengeTitle() {
        return challengeTitle;
    }
    
    public void setChallengeTitle(String challengeTitle) {
        this.challengeTitle = challengeTitle;
    }
    
    public String getChallengeDescription() {
        return challengeDescription;
    }
    
    public void setChallengeDescription(String challengeDescription) {
        this.challengeDescription = challengeDescription;
    }
    
    public int getTotalQuestions() {
        return totalQuestions;
    }
    
    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public long getAcceptedAt() {
        return acceptedAt;
    }
    
    public void setAcceptedAt(long acceptedAt) {
        this.acceptedAt = acceptedAt;
    }
    
    public long getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }
    
    public int getChallengerScore() {
        return challengerScore;
    }
    
    public void setChallengerScore(int challengerScore) {
        this.challengerScore = challengerScore;
    }
    
    public int getOpponentScore() {
        return opponentScore;
    }
    
    public void setOpponentScore(int opponentScore) {
        this.opponentScore = opponentScore;
    }
    
    public boolean isChallengerCompleted() {
        return challengerCompleted;
    }
    
    public void setChallengerCompleted(boolean challengerCompleted) {
        this.challengerCompleted = challengerCompleted;
    }
    
    public boolean isOpponentCompleted() {
        return opponentCompleted;
    }
    
    public void setOpponentCompleted(boolean opponentCompleted) {
        this.opponentCompleted = opponentCompleted;
    }
    
    public String getWinnerId() {
        return winnerId;
    }
    
    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }
    
    public String getWinnerName() {
        return winnerName;
    }
    
    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }
    
    public int getXpReward() {
        return xpReward;
    }
    
    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }
    
    /**
     * Check if challenge is expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt && !status.equals("completed");
    }
    
    /**
     * Check if both players completed
     */
    public boolean isBothCompleted() {
        return challengerCompleted && opponentCompleted;
    }
    
    /**
     * Get time remaining in hours
     */
    public int getHoursRemaining() {
        long remaining = expiresAt - System.currentTimeMillis();
        return (int) (remaining / (1000 * 60 * 60));
    }
    
    /**
     * Get challenge type icon
     */
    public String getTypeIcon() {
        switch (challengeType) {
            case "vocabulary":
                return "📚";
            case "grammar":
                return "✏️";
            case "speaking":
                return "🗣️";
            case "listening":
                return "👂";
            case "mixed":
                return "🎯";
            default:
                return "⚔️";
        }
    }
    
    /**
     * Determine winner after both completed
     */
    public void determineWinner() {
        if (!isBothCompleted()) {
            return;
        }
        
        if (challengerScore > opponentScore) {
            winnerId = challengerId;
            winnerName = challengerName;
            xpReward = 100;
        } else if (opponentScore > challengerScore) {
            winnerId = opponentId;
            winnerName = opponentName;
            xpReward = 100;
        } else {
            // Draw
            winnerId = "draw";
            winnerName = "Draw";
            xpReward = 50; // Both get 50 XP
        }
        
        status = "completed";
        completedAt = System.currentTimeMillis();
    }
}
