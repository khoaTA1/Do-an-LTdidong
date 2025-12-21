package vn.ltdidong.apphoctienganh.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.ltdidong.apphoctienganh.models.ChatConversation;

/**
 * DAO cho ChatConversation
 */
@Dao
public interface ChatConversationDao {
    
    @Insert
    long insert(ChatConversation conversation);
    
    @Update
    void update(ChatConversation conversation);
    
    @Delete
    void delete(ChatConversation conversation);
    
    @Query("SELECT * FROM chat_conversations WHERE userId = :userId AND isArchived = 0 ORDER BY lastMessageAt DESC")
    LiveData<List<ChatConversation>> getAllByUser(String userId);
    
    @Query("SELECT * FROM chat_conversations WHERE id = :conversationId")
    LiveData<ChatConversation> getById(int conversationId);
    
    @Query("SELECT * FROM chat_conversations WHERE id = :conversationId")
    ChatConversation getByIdSync(int conversationId);
    
    @Query("SELECT * FROM chat_conversations WHERE userId = :userId AND isArchived = 1 ORDER BY lastMessageAt DESC")
    LiveData<List<ChatConversation>> getArchivedByUser(String userId);
    
    @Query("UPDATE chat_conversations SET lastMessageAt = :timestamp, messageCount = messageCount + 1 WHERE id = :conversationId")
    void updateLastMessage(int conversationId, long timestamp);
    
    @Query("DELETE FROM chat_conversations WHERE userId = :userId")
    void deleteAllByUser(String userId);
}
