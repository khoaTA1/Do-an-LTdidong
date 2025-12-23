package vn.ltdidong.apphoctienganh.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Model cho tin nhắn trong AI Tutor chat
 */
@Entity(tableName = "ai_tutor_messages",
        foreignKeys = @ForeignKey(
            entity = ChatConversation.class,
            parentColumns = "id",
            childColumns = "conversationId",
            onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("conversationId")})
public class AITutorMessage {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private int conversationId;
    private String message;
    private boolean isUser; // true = user, false = AI
    private long timestamp;
    private String context; // Ngữ cảnh của tin nhắn (skill type, lesson info, etc.)
    
    public AITutorMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public AITutorMessage(int conversationId, String message, boolean isUser) {
        this();
        this.conversationId = conversationId;
        this.message = message;
        this.isUser = isUser;
    }

    public AITutorMessage(int conversationId, String message, boolean isUser, String context) {
        this(conversationId, message, isUser);
        this.context = context;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
