package vn.ltdidong.apphoctienganh.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import vn.ltdidong.apphoctienganh.models.AITutorMessage;

/**
 * DAO cho AITutorMessage
 */
@Dao
public interface AITutorMessageDao {
    
    @Insert
    long insert(AITutorMessage message);
    
    @Delete
    void delete(AITutorMessage message);
    
    @Query("SELECT * FROM ai_tutor_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    LiveData<List<AITutorMessage>> getMessagesByConversation(int conversationId);
    
    @Query("SELECT * FROM ai_tutor_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    List<AITutorMessage> getMessagesByConversationSync(int conversationId);
    
    @Query("SELECT * FROM ai_tutor_messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 5")
    List<AITutorMessage> getRecentMessages(int conversationId);
    
    @Query("DELETE FROM ai_tutor_messages WHERE conversationId = :conversationId")
    void deleteByConversation(int conversationId);
    
    @Query("SELECT COUNT(*) FROM ai_tutor_messages WHERE conversationId = :conversationId")
    int getMessageCount(int conversationId);
}
