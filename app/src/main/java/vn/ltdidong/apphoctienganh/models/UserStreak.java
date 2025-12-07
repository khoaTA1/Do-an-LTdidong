package vn.ltdidong.apphoctienganh.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity lưu trữ thông tin streak (chuỗi ngày) của người dùng
 * Streak tăng khi user hoàn thành ít nhất 1 bài học mỗi ngày
 */
@Entity(tableName = "user_streak",
        indices = {@Index(value = "userId", unique = true)})
public class UserStreak {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    // ID của user
    private String userId;
    
    // Số ngày streak hiện tại
    private int currentStreak;
    
    // Streak dài nhất từng đạt được
    private int longestStreak;
    
    // Timestamp của lần hoàn thành bài học cuối cùng (để check ngày)
    private long lastCompletedDate;
    
    // Ngày bắt đầu streak hiện tại
    private long streakStartDate;
    
    public UserStreak() {
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.lastCompletedDate = 0;
        this.streakStartDate = 0;
    }
    
    @Ignore
    public UserStreak(String userId) {
        this.userId = userId;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.lastCompletedDate = 0;
        this.streakStartDate = 0;
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

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public long getLastCompletedDate() {
        return lastCompletedDate;
    }

    public void setLastCompletedDate(long lastCompletedDate) {
        this.lastCompletedDate = lastCompletedDate;
    }

    public long getStreakStartDate() {
        return streakStartDate;
    }

    public void setStreakStartDate(long streakStartDate) {
        this.streakStartDate = streakStartDate;
    }

    /**
     * Kiểm tra xem có cần cập nhật streak không dựa trên timestamp hiện tại
     * @param currentTime Timestamp hiện tại
     * @return true nếu cần cập nhật, false nếu đã cập nhật hôm nay rồi
     */
    public boolean shouldUpdateStreak(long currentTime) {
        if (lastCompletedDate == 0) {
            return true; // Lần đầu tiên
        }
        
        // Lấy ngày (bỏ qua giờ phút giây)
        long lastDay = lastCompletedDate / (24 * 60 * 60 * 1000);
        long currentDay = currentTime / (24 * 60 * 60 * 1000);
        
        // Nếu đã hoàn thành bài học hôm nay rồi, không cần cập nhật nữa
        return lastDay != currentDay;
    }

    /**
     * Cập nhật streak dựa trên thời gian hiện tại
     * @param currentTime Timestamp hiện tại
     */
    public void updateStreak(long currentTime) {
        if (!shouldUpdateStreak(currentTime)) {
            return; // Đã cập nhật hôm nay rồi
        }
        
        long lastDay = lastCompletedDate / (24 * 60 * 60 * 1000);
        long currentDay = currentTime / (24 * 60 * 60 * 1000);
        long daysDiff = currentDay - lastDay;
        
        if (lastCompletedDate == 0) {
            // Lần đầu tiên
            currentStreak = 1;
            streakStartDate = currentTime;
        } else if (daysDiff == 1) {
            // Ngày liên tiếp - tăng streak
            currentStreak++;
        } else if (daysDiff > 1) {
            // Bỏ lỡ ngày - reset streak
            currentStreak = 1;
            streakStartDate = currentTime;
        }
        // daysDiff == 0 không xảy ra vì đã check shouldUpdateStreak
        
        // Cập nhật longest streak
        if (currentStreak > longestStreak) {
            longestStreak = currentStreak;
        }
        
        lastCompletedDate = currentTime;
    }
}
