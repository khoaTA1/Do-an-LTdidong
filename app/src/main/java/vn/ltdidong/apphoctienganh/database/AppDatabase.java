package vn.ltdidong.apphoctienganh.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import vn.ltdidong.apphoctienganh.models.UserProgress;
import vn.ltdidong.apphoctienganh.models.UserStreak;
import vn.ltdidong.apphoctienganh.models.DailyChallenge;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room Database chính cho ứng dụng
 * Chỉ lưu UserProgress (tiến độ người dùng) và UserStreak (chuỗi ngày) - local database
 * Lessons và Questions load từ Firebase Firestore
 * Version 7: Thêm DailyChallenge để theo dõi thử thách hàng ngày
 */
@Database(
    entities = {UserProgress.class, UserStreak.class, DailyChallenge.class},
    version = 7,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    
    // DAO
    public abstract UserProgressDao userProgressDao();
    public abstract UserStreakDao userStreakDao();
    public abstract DailyChallengeDao dailyChallengeDao();
    
    // Singleton instance
    private static volatile AppDatabase INSTANCE;
    
    // Thread pool để chạy các database operations trên background thread
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = 
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    
    /**
     * Lấy database instance (Singleton pattern)
     * @param context Application context
     * @return Database instance
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "listening_database"
                    )
                    // Xóa và tạo lại database khi version thay đổi
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}

