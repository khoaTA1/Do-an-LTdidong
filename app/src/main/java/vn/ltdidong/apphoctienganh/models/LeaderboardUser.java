package vn.ltdidong.apphoctienganh.models;

/**
 * Model cho người dùng trong leaderboard
 */
public class LeaderboardUser {
    private String userId;
    private String username;
    private String avatarUrl;
    private int level;
    private long totalXP;
    private int currentStreak;
    private int rank;
    private boolean isFriend;
    private boolean isCurrentUser;
    
    public LeaderboardUser() {}

    public LeaderboardUser(String userId, String username, long totalXP, int level) {
        this.userId = userId;
        this.username = username;
        this.totalXP = totalXP;
        this.level = level;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getTotalXP() {
        return totalXP;
    }

    public void setTotalXP(long totalXP) {
        this.totalXP = totalXP;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    public void setCurrentUser(boolean currentUser) {
        isCurrentUser = currentUser;
    }
}
