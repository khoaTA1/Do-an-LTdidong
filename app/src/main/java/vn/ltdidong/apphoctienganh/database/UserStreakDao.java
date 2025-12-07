package vn.ltdidong.apphoctienganh.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import vn.ltdidong.apphoctienganh.models.UserStreak;

/**
 * DAO cho UserStreak entity
 * Quản lý chuỗi ngày học liên tục của người dùng
 */
@Dao
public interface UserStreakDao {
    
    /**
     * Lấy streak của user
     * @param userId ID của user
     */
    @Query("SELECT * FROM user_streak WHERE userId = :userId LIMIT 1")
    LiveData<UserStreak> getStreakByUser(String userId);
    
    /**
     * Lấy streak của user (không dùng LiveData - dùng cho background thread)
     */
    @Query("SELECT * FROM user_streak WHERE userId = :userId LIMIT 1")
    UserStreak getStreakByUserSync(String userId);
    
    /**
     * Thêm hoặc cập nhật streak
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertStreak(UserStreak streak);
    
    /**
     * Cập nhật streak
     */
    @Update
    void updateStreak(UserStreak streak);
    
    /**
     * Xóa streak của user
     */
    @Query("DELETE FROM user_streak WHERE userId = :userId")
    void deleteStreakByUser(String userId);
}
