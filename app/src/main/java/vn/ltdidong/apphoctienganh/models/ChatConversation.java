package vn.ltdidong.apphoctienganh.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Model cho cuộc hội thoại AI Tutor
 */
@Entity(tableName = "chat_conversations")
public class ChatConversation {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String userId;
    private String title;
    private String topic; // Chủ đề hội thoại
    private long createdAt;
    private long lastMessageAt;
    private int messageCount;
    private boolean isArchived;
    
    public ChatConversation() {
        this.createdAt = System.currentTimeMillis();
        this.lastMessageAt = this.createdAt;
        this.messageCount = 0;
        this.isArchived = false;
    }

    public ChatConversation(String userId, String title, String topic) {
        this();
        this.userId = userId;
        this.title = title;
        this.topic = topic;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(long lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }
}
