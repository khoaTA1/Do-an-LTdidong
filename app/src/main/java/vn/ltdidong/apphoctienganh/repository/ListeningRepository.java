package vn.ltdidong.apphoctienganh.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import vn.ltdidong.apphoctienganh.database.AppDatabase;
import vn.ltdidong.apphoctienganh.database.ListeningLessonDao;
import vn.ltdidong.apphoctienganh.database.QuestionDao;
import vn.ltdidong.apphoctienganh.database.UserProgressDao;
import vn.ltdidong.apphoctienganh.models.ListeningLesson;
import vn.ltdidong.apphoctienganh.models.Question;
import vn.ltdidong.apphoctienganh.models.UserProgress;

import java.util.List;

/**
 * Repository class - Lớp trung gian giữa ViewModel và Database
 * Cung cấp API sạch sẽ để truy cập data
 * Xử lý logic data từ nhiều nguồn (database, network, cache...)
 */
public class ListeningRepository {
    
    // DAOs
    private ListeningLessonDao lessonDao;
    private QuestionDao questionDao;
    private UserProgressDao progressDao;
    
    /**
     * Constructor - khởi tạo database và các DAOs
     * @param application Application context
     */
    public ListeningRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        lessonDao = db.listeningLessonDao();
        questionDao = db.questionDao();
        progressDao = db.userProgressDao();
    }
    
    // ============= LISTENING LESSON METHODS =============
    
    /**
     * Lấy tất cả bài học
     */
    public LiveData<List<ListeningLesson>> getAllLessons() {
        return lessonDao.getAllLessons();
    }
    
    /**
     * Lấy bài học theo độ khó
     */
    public LiveData<List<ListeningLesson>> getLessonsByDifficulty(String difficulty) {
        return lessonDao.getLessonsByDifficulty(difficulty);
    }
    
    /**
     * Lấy một bài học cụ thể
     */
    public LiveData<ListeningLesson> getLessonById(int lessonId) {
        return lessonDao.getLessonById(lessonId);
    }
    
    /**
     * Thêm bài học mới (chạy trên background thread)
     */
    public void insertLesson(ListeningLesson lesson) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            lessonDao.insertLesson(lesson);
        });
    }
    
    /**
     * Cập nhật bài học (chạy trên background thread)
     */
    public void updateLesson(ListeningLesson lesson) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            lessonDao.updateLesson(lesson);
        });
    }
    
    /**
     * Xóa bài học (chạy trên background thread)
     */
    public void deleteLesson(ListeningLesson lesson) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            lessonDao.deleteLesson(lesson);
        });
    }
    
    // ============= QUESTION METHODS =============
    
    /**
     * Lấy tất cả câu hỏi của một bài học
     */
    public LiveData<List<Question>> getQuestionsByLesson(int lessonId) {
        return questionDao.getQuestionsByLesson(lessonId);
    }
    
    /**
     * Thêm câu hỏi mới (chạy trên background thread)
     */
    public void insertQuestion(Question question) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            questionDao.insertQuestion(question);
        });
    }
    
    /**
     * Thêm nhiều câu hỏi cùng lúc (chạy trên background thread)
     */
    public void insertQuestions(List<Question> questions) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            questionDao.insertQuestions(questions);
        });
    }
    
    /**
     * Cập nhật câu hỏi (chạy trên background thread)
     */
    public void updateQuestion(Question question) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            questionDao.updateQuestion(question);
        });
    }
    
    /**
     * Xóa câu hỏi (chạy trên background thread)
     */
    public void deleteQuestion(Question question) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            questionDao.deleteQuestion(question);
        });
    }
    
    // ============= USER PROGRESS METHODS =============
    
    /**
     * Lấy tất cả tiến độ
     */
    public LiveData<List<UserProgress>> getAllProgress() {
        return progressDao.getAllProgress();
    }
    
    /**
     * Lấy tiến độ của một bài học
     */
    public LiveData<UserProgress> getProgressByLesson(int lessonId) {
        return progressDao.getProgressByLesson(lessonId);
    }
    
    /**
     * Lấy các bài đã hoàn thành
     */
    public LiveData<List<UserProgress>> getCompletedLessons() {
        return progressDao.getCompletedLessons();
    }
    
    /**
     * Lấy các bài đang làm dở
     */
    public LiveData<List<UserProgress>> getInProgressLessons() {
        return progressDao.getInProgressLessons();
    }
    
    /**
     * Lưu hoặc cập nhật tiến độ (chạy trên background thread)
     */
    public void saveProgress(UserProgress progress) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Kiểm tra xem đã có progress chưa
            UserProgress existingProgress = progressDao.getProgressByLessonSync(progress.getLessonId());
            
            if (existingProgress != null) {
                // Nếu đã có, cập nhật
                progress.setId(existingProgress.getId());
                progress.setAttempts(existingProgress.getAttempts() + 1);
                
                // Cập nhật best score nếu điểm mới cao hơn
                if (progress.getScore() > existingProgress.getBestScore()) {
                    progress.setBestScore(progress.getScore());
                } else {
                    progress.setBestScore(existingProgress.getBestScore());
                }
                
                progressDao.updateProgress(progress);
            } else {
                // Nếu chưa có, thêm mới
                progress.setAttempts(1);
                progress.setBestScore(progress.getScore());
                progressDao.insertProgress(progress);
            }
        });
    }
    
    /**
     * Xóa tiến độ (chạy trên background thread)
     */
    public void deleteProgress(UserProgress progress) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            progressDao.deleteProgress(progress);
        });
    }
    
    /**
     * Xóa tiến độ của một bài học (chạy trên background thread)
     */
    public void deleteProgressByLesson(int lessonId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            progressDao.deleteProgressByLesson(lessonId);
        });
    }
    
    /**
     * Lấy điểm trung bình
     */
    public LiveData<Float> getAverageScore() {
        return progressDao.getAverageScore();
    }
    
    /**
     * Lấy số bài đã hoàn thành
     */
    public LiveData<Integer> getCompletedLessonCount() {
        return progressDao.getCompletedLessonCount();
    }
    
    /**
     * Lấy điểm cao nhất
     */
    public LiveData<Float> getHighestScore() {
        return progressDao.getHighestScore();
    }
}
