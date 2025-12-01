package vn.ltdidong.apphoctienganh.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import vn.ltdidong.apphoctienganh.models.ListeningLesson;
import vn.ltdidong.apphoctienganh.models.Question;
import vn.ltdidong.apphoctienganh.models.UserProgress;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room Database chính cho ứng dụng Listening
 * Quản lý tất cả entities và DAOs
 */
@Database(
    entities = {ListeningLesson.class, Question.class, UserProgress.class},
    version = 2,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    
    // Các DAOs
    public abstract ListeningLessonDao listeningLessonDao();
    public abstract QuestionDao questionDao();
    public abstract UserProgressDao userProgressDao();
    
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
                    // Callback để thêm sample data khi database được tạo lần đầu
                    .addCallback(sRoomDatabaseCallback)
                    // Xóa và tạo lại database khi version thay đổi
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Callback được gọi khi database được tạo lần đầu hoặc sau khi migrate
     * Thêm sample data để demo
     */
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            populateDatabase();
        }
        
        @Override
        public void onDestructiveMigration(@NonNull SupportSQLiteDatabase db) {
            super.onDestructiveMigration(db);
            // Sau khi xóa database cũ, thêm lại dữ liệu mẫu
            populateDatabase();
        }
    };
    
    /**
     * Thêm dữ liệu mẫu vào database
     */
    private static void populateDatabase() {
        if (INSTANCE != null) {
            
            // Thêm sample data trên background thread
            databaseWriteExecutor.execute(() -> {
                // Lấy DAOs
                ListeningLessonDao lessonDao = INSTANCE.listeningLessonDao();
                QuestionDao questionDao = INSTANCE.questionDao();
                
                // Xóa data cũ nếu có
                lessonDao.deleteAllLessons();
                
                // ========== BÀI 1: EASY - GREETINGS ==========
                ListeningLesson lesson1 = new ListeningLesson(
                    "Basic Greetings",
                    "Learn how to greet people in English",
                    "EASY",
                    "raw://smalltalk0101", // Audio resource name in res/raw
                    23, // 23 seconds
                    "Hello! My name is Sarah. Nice to meet you. How are you today? I'm doing great, thank you!",
                    "ic_lesson_1",
                    3
                );
                long lesson1Id = lessonDao.insertLesson(lesson1);
                
                // Thêm câu hỏi cho bài 1
                questionDao.insertQuestion(new Question(
                    (int)lesson1Id,
                    "What is the man doing right now?",
                    "Working full-time",
                    "Staying at home",
                    "Going to school",
                    "Traveling",
                    "C",
                    "The speaker clearly says 'I've been good. I'm in school right now'",
                    1
                ));
                
                questionDao.insertQuestion(new Question(
                    (int)lesson1Id,
                    "Which school does the men go to?",
                    "UCLA",
                    "PCC",
                    "Harvard",
                    "USC",
                    "B",
                    "The speaker says 'I go to PCC'",
                    2
                ));
                
                questionDao.insertQuestion(new Question(
                    (int)lesson1Id,
                    "What does the woman say to the man at the end of the conversation?",
                    "Good luck with your new job",
                    "Good luck on your exam",
                    "Good luck with school",
                    "Have a nice day",
                    "C",
                    "Good luck with school",
                    3
                ));
                
                // ========== BÀI 2: EASY - DAILY ROUTINE ==========
                ListeningLesson lesson2 = new ListeningLesson(
                    "Daily Routine",
                    "Listen about someone's daily activities",
                    "EASY",
                    "sample_audio_2", // Audio resource name in res/raw
                    60,
                    "I wake up at 7 AM every day. First, I brush my teeth and take a shower. Then I have breakfast with my family. After that, I go to school at 8 AM.",
                    "ic_lesson_2",
                    3
                );
                long lesson2Id = lessonDao.insertLesson(lesson2);
                
                questionDao.insertQuestion(new Question(
                    (int)lesson2Id,
                    "What time does the speaker wake up?",
                    "6 AM",
                    "7 AM",
                    "8 AM",
                    "9 AM",
                    "B",
                    "The speaker says 'I wake up at 7 AM'",
                    1
                ));
                
                questionDao.insertQuestion(new Question(
                    (int)lesson2Id,
                    "What does the speaker do first after waking up?",
                    "Have breakfast",
                    "Go to school",
                    "Brush teeth",
                    "Watch TV",
                    "C",
                    "First activity is 'brush my teeth'",
                    2
                ));
                
                questionDao.insertQuestion(new Question(
                    (int)lesson2Id,
                    "What time does the speaker go to school?",
                    "7 AM",
                    "8 AM",
                    "9 AM",
                    "10 AM",
                    "B",
                    "The speaker goes to school at 8 AM",
                    3
                ));
                
                // ========== BÀI 3: MEDIUM - SHOPPING ==========
                ListeningLesson lesson3 = new ListeningLesson(
                    "Shopping at the Mall",
                    "A conversation about shopping",
                    "MEDIUM",
                    "sample_audio_3", // Audio resource name in res/raw
                    90,
                    "Customer: Excuse me, how much is this blue jacket?\nSalesperson: It's $45, but we have a 20% discount today.\nCustomer: That's great! I'll take it. Do you accept credit cards?\nSalesperson: Yes, we do. Cash and credit cards are both fine.",
                    "ic_lesson_3",
                    4
                );
                long lesson3Id = lessonDao.insertLesson(lesson3);
                
                questionDao.insertQuestion(new Question(
                    (int)lesson3Id,
                    "How much is the jacket originally?",
                    "$35",
                    "$40",
                    "$45",
                    "$50",
                    "C",
                    "The salesperson says it's $45",
                    1
                ));
                
                questionDao.insertQuestion(new Question(
                    (int)lesson3Id,
                    "What discount is available?",
                    "10%",
                    "15%",
                    "20%",
                    "25%",
                    "C",
                    "There is a 20% discount today",
                    2
                ));
                
                questionDao.insertQuestion(new Question(
                    (int)lesson3Id,
                    "What color is the jacket?",
                    "Red",
                    "Blue",
                    "Green",
                    "Black",
                    "B",
                    "The customer asks about 'this blue jacket'",
                    3
                ));
                
                questionDao.insertQuestion(new Question(
                    (int)lesson3Id,
                    "What payment methods are accepted?",
                    "Only cash",
                    "Only credit cards",
                    "Cash and credit cards",
                    "Only mobile payment",
                    "C",
                    "Both cash and credit cards are accepted",
                    4
                ));
                
                // ========== BÀI 4: MEDIUM - WEATHER FORECAST ==========
                ListeningLesson lesson4 = new ListeningLesson(
                    "Weather Forecast",
                    "Listen to a weather report",
                    "MEDIUM",
                    "sample_audio_4", // Audio resource name in res/raw
                    75,
                    "Good morning! Here's today's weather forecast. It will be sunny in the morning with temperatures around 25 degrees Celsius. In the afternoon, we expect some clouds and the temperature will rise to 30 degrees. There's a 40% chance of rain in the evening, so don't forget your umbrella!",
                    "ic_lesson_4",
                    4
                );
                long lesson4Id = lessonDao.insertLesson(lesson4);
                
                questionDao.insertQuestion(new Question(
                    (int)lesson4Id,
                    "How will the morning weather be?",
                    "Rainy",
                    "Cloudy",
                    "Sunny",
                    "Snowy",
                    "C",
                    "It will be sunny in the morning",
                    1
                ));
                
                questionDao.insertQuestion(new Question(
                    (int)lesson4Id,
                    "What's the morning temperature?",
                    "20°C",
                    "25°C",
                    "30°C",
                    "35°C",
                    "B",
                    "Morning temperature is around 25 degrees",
                    2
                ));
                
                questionDao.insertQuestion(new Question(
                    (int)lesson4Id,
                    "When might it rain?",
                    "Morning",
                    "Afternoon",
                    "Evening",
                    "Night",
                    "C",
                    "40% chance of rain in the evening",
                    3
                ));
                
                questionDao.insertQuestion(new Question(
                    (int)lesson4Id,
                    "What should you bring?",
                    "Sunglasses",
                    "Umbrella",
                    "Hat",
                    "Coat",
                    "B",
                    "Don't forget your umbrella!",
                    4
                ));
                
                // ========== BÀI 5: HARD - JOB INTERVIEW ==========
                ListeningLesson lesson5 = new ListeningLesson(
                    "Job Interview",
                    "A conversation during a job interview",
                    "HARD",
                    "sample_audio_5", // Audio resource name in res/raw
                    120,
                    "Interviewer: Thank you for coming, Mr. Johnson. Can you tell me about your previous work experience?\nCandidate: Certainly. I worked as a marketing manager for five years at Tech Solutions Inc. I was responsible for developing marketing strategies and managing a team of ten people. We successfully increased sales by 35% during my tenure.\ninterviewer: That's impressive. Why are you interested in joining our company?\nCandidate: I've been following your company's growth for the past two years. I'm particularly interested in your innovative approach to digital marketing, and I believe my experience would be valuable to your team.",
                    "ic_lesson_5",
                    5
                );
                long lesson5Id = lessonDao.insertLesson(lesson5);
                
                questionDao.insertQuestion(new Question(
                    (int)lesson5Id,
                    "What was the candidate's previous position?",
                    "Sales Manager",
                    "Marketing Manager",
                    "Project Manager",
                    "Product Manager",
                    "B",
                    "He worked as a marketing manager",
                    1
                ));
                
                questionDao.insertQuestion(new Question(
                    (int)lesson5Id,
                    "How long did he work at his previous company?",
                    "3 years",
                    "4 years",
                    "5 years",
                    "6 years",
                    "C",
                    "He worked for five years",
                    2
                ));
                
                questionDao.insertQuestion(new Question(
                    (int)lesson5Id,
                    "How many people did he manage?",
                    "5 people",
                    "10 people",
                    "15 people",
                    "20 people",
                    "B",
                    "He managed a team of ten people",
                    3
                ));
                
                questionDao.insertQuestion(new Question(
                    (int)lesson5Id,
                    "By how much did sales increase?",
                    "25%",
                    "30%",
                    "35%",
                    "40%",
                    "C",
                    "Sales increased by 35%",
                    4
                ));
                
                questionDao.insertQuestion(new Question(
                    (int)lesson5Id,
                    "How long has he been following the company?",
                    "1 year",
                    "2 years",
                    "3 years",
                    "4 years",
                    "B",
                    "He's been following for the past two years",
                    5
                ));
            });
        }
    }
}
