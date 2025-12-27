package vn.ltdidong.apphoctienganh.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import vn.ltdidong.apphoctienganh.models.UserProgress;
import vn.ltdidong.apphoctienganh.models.UserStreak;
import vn.ltdidong.apphoctienganh.models.DailyChallenge;
import vn.ltdidong.apphoctienganh.models.LearningSession;
import vn.ltdidong.apphoctienganh.models.SkillProgress;
import vn.ltdidong.apphoctienganh.models.StudyHabit;
import vn.ltdidong.apphoctienganh.models.StudySchedule;
import vn.ltdidong.apphoctienganh.models.ChatConversation;
import vn.ltdidong.apphoctienganh.models.AITutorMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room Database chính cho ứng dụng
 * Chỉ lưu UserProgress (tiến độ người dùng) và UserStreak (chuỗi ngày) - local database
 * Lessons và Questions load từ Firebase Firestore
 * Version 10: Thêm ChatConversation, AITutorMessage cho AI Tutor feature
 * Version 11: Thêm tracking chi tiết cho listening challenges (easy/medium/hard count, fill blank, exp earned)
 */
@Database(
    entities = {
        UserProgress.class, 
        UserStreak.class, 
        DailyChallenge.class,
        LearningSession.class,
        SkillProgress.class,
        StudyHabit.class,
        StudySchedule.class,
        ChatConversation.class,
        AITutorMessage.class
    },
    version = 11,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    
    // DAO
    public abstract UserProgressDao userProgressDao();
    public abstract UserStreakDao userStreakDao();
    public abstract DailyChallengeDao dailyChallengeDao();
    public abstract LearningSessionDao learningSessionDao();
    public abstract SkillProgressDao skillProgressDao();
    public abstract StudyHabitDao studyHabitDao();
    public abstract StudyScheduleDao studyScheduleDao();
    public abstract ChatConversationDao chatConversationDao();
    public abstract AITutorMessageDao aiTutorMessageDao();
    
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

