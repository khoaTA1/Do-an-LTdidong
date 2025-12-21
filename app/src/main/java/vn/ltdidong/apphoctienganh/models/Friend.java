package vn.ltdidong.apphoctienganh.models;

/**
 * Model cho bạn bè
 */
public class Friend {
    private String userId;
    private String friendId;
    private String friendName;
    private String friendEmail;
    private String friendAvatarUrl;
    private int friendLevel;
    private long friendTotalXP;
    private long friendedAt;
    private String status; // PENDING, ACCEPTED, BLOCKED
    
    public Friend() {
        this.friendedAt = System.currentTimeMillis();
        this.status = "PENDING";
    }

    public Friend(String userId, String friendId, String friendName, String friendEmail) {
        this();
        this.userId = userId;
        this.friendId = friendId;
        this.friendName = friendName;
        this.friendEmail = friendEmail;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getFriendEmail() {
        return friendEmail;
    }

    public void setFriendEmail(String friendEmail) {
        this.friendEmail = friendEmail;
    }

    public String getFriendAvatarUrl() {
        return friendAvatarUrl;
    }

    public void setFriendAvatarUrl(String friendAvatarUrl) {
        this.friendAvatarUrl = friendAvatarUrl;
    }

    public int getFriendLevel() {
        return friendLevel;
    }

    public void setFriendLevel(int friendLevel) {
        this.friendLevel = friendLevel;
    }

    public long getFriendTotalXP() {
        return friendTotalXP;
    }

    public void setFriendTotalXP(long friendTotalXP) {
        this.friendTotalXP = friendTotalXP;
    }

    public long getFriendedAt() {
        return friendedAt;
    }

    public void setFriendedAt(long friendedAt) {
        this.friendedAt = friendedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
