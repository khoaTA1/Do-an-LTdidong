package vn.ltdidong.apphoctienganh.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import vn.ltdidong.apphoctienganh.models.DailyChallenge;

/**
 * DAO for Daily Challenge operations
 */
@Dao
public interface DailyChallengeDao {
    
    @Insert
    void insert(DailyChallenge challenge);
    
    @Update
    void update(DailyChallenge challenge);
    
    @Query("SELECT * FROM daily_challenges WHERE userId = :userId AND date = :date LIMIT 1")
    DailyChallenge getChallengeByDate(String userId, String date);
    
    @Query("SELECT * FROM daily_challenges WHERE userId = :userId ORDER BY date DESC LIMIT 30")
    java.util.List<DailyChallenge> getLastMonthChallenges(String userId);
    
    @Query("SELECT COUNT(*) FROM daily_challenges WHERE userId = :userId AND allCompleted = 1")
    int getTotalCompletedDays(String userId);
    
    @Query("DELETE FROM daily_challenges WHERE userId = :userId")
    void deleteAllForUser(String userId);
}
