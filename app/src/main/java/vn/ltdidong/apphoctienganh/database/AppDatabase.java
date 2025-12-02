package vn.ltdidong.apphoctienganh.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import vn.ltdidong.apphoctienganh.models.UserProgress;
import vn.ltdidong.apphoctienganh.models.UserStreak;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room Database chính cho ứng dụng
 * Chỉ lưu UserProgress (tiến độ người dùng) và UserStreak (chuỗi ngày) - local database
 * Lessons và Questions load từ Firebase Firestore
 * Version 6: Thêm UserStreak để theo dõi chuỗi ngày học liên tục
 */
@Database(
    entities = {UserProgress.class, UserStreak.class},
    version = 6,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    
    // DAO
    public abstract UserProgressDao userProgressDao();
    public abstract UserStreakDao userStreakDao();
    
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

