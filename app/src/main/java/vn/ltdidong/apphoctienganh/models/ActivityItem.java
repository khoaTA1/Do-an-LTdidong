package vn.ltdidong.apphoctienganh.models;

import com.google.firebase.Timestamp;

/**
 * Model cho Activity Feed Item
 */
public class ActivityItem {
    private String id;
    private String userId;
    private String userName;
    private String userAvatar;
    private String activityType; // "achievement", "completion", "challenge", "streak", "level_up"
    private String title;
    private String description;
    private long timestamp;
    private String icon;
    private int xpGained;
    private String extraData; // JSON string for additional data
    
    public ActivityItem() {
    }
    
    public ActivityItem(String userId, String userName, String activityType, String title, String description) {
        this.userId = userId;
        this.userName = userName;
        this.activityType = activityType;
        this.title = title;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getUserAvatar() {
        return userAvatar;
    }
    
    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }
    
    public String getActivityType() {
        return activityType;
    }
    
    public void setActivityType(String activityType) {
        this.activityType = activityType;
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
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public int getXpGained() {
        return xpGained;
    }
    
    public void setXpGained(int xpGained) {
        this.xpGained = xpGained;
    }
    
    public String getExtraData() {
        return extraData;
    }
    
    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }
    
    /**
     * Get formatted time ago string
     */
    public String getTimeAgo() {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            return "Just now";
        }
    }
    
    /**
     * Get icon emoji based on activity type
     */
    public String getActivityIcon() {
        if (icon != null && !icon.isEmpty()) {
            return icon;
        }
        
        switch (activityType) {
            case "achievement":
                return "🏆";
            case "completion":
                return "✅";
            case "challenge":
                return "⚔️";
            case "streak":
                return "🔥";
            case "level_up":
                return "⬆️";
            default:
                return "📢";
        }
    }
}
