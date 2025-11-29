package vn.ltdidong.apphoctienganh.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import vn.ltdidong.apphoctienganh.models.ListeningLesson;
import vn.ltdidong.apphoctienganh.models.Question;
import vn.ltdidong.apphoctienganh.models.UserProgress;
import vn.ltdidong.apphoctienganh.repositories.ListeningRepository;

import java.util.List;

/**
 * ViewModel cho Listening feature
 * Quản lý UI-related data và survive configuration changes (như xoay màn hình)
 * Giao tiếp giữa UI (Activity/Fragment) và Repository
 */
public class ListeningViewModel extends AndroidViewModel {
    
    private ListeningRepository repository;
    
    // LiveData cho lessons
    private LiveData<List<ListeningLesson>> allLessons;
    private LiveData<Integer> completedLessonCount;
    private LiveData<Float> averageScore;
    private LiveData<Float> highestScore;
    
    /**
     * Constructor
     * @param application Application context
     */
    public ListeningViewModel(@NonNull Application application) {
        super(application);
        // Tạo repository từ application context
        this.repository = new ListeningRepository(application);
        
        // Initialize LiveData
        allLessons = repository.getAllLessons();
        completedLessonCount = repository.getCompletedLessonCount();
        averageScore = repository.getAverageScore();
        highestScore = repository.getHighestScore();
    }
    
    // ============= LESSON METHODS =============
    
    /**
     * Lấy tất cả bài học
     * @return LiveData chứa danh sách bài học
     */
    public LiveData<List<ListeningLesson>> getAllLessons() {
        return allLessons;
    }
    
    /**
     * Lấy bài học theo độ khó
     * @param difficulty Độ khó: EASY, MEDIUM, HARD
     * @return LiveData chứa danh sách bài học
     */
    public LiveData<List<ListeningLesson>> getLessonsByDifficulty(String difficulty) {
        return repository.getLessonsByDifficulty(difficulty);
    }
    
    /**
     * Lấy một bài học cụ thể theo ID
     * @param lessonId ID của bài học
     * @return LiveData chứa bài học
     */
    public LiveData<ListeningLesson> getLessonById(int lessonId) {
        return repository.getLessonById(lessonId);
    }
    
    /**
     * Thêm bài học mới
     * @param lesson Bài học cần thêm
     */
    public void insertLesson(ListeningLesson lesson) {
        repository.insertLesson(lesson);
    }
    
    /**
     * Cập nhật bài học
     * @param lesson Bài học cần cập nhật
     */
    public void updateLesson(ListeningLesson lesson) {
        repository.updateLesson(lesson);
    }
    
    /**
     * Xóa bài học
     * @param lesson Bài học cần xóa
     */
    public void deleteLesson(ListeningLesson lesson) {
        repository.deleteLesson(lesson);
    }
    
    // ============= QUESTION METHODS =============
    
    /**
     * Lấy tất cả câu hỏi của một bài học
     * @param lessonId ID của bài học
     * @return LiveData chứa danh sách câu hỏi
     */
    public LiveData<List<Question>> getQuestionsByLesson(int lessonId) {
        return repository.getQuestionsByLesson(lessonId);
    }
    
    /**
     * Thêm câu hỏi mới
     * @param question Câu hỏi cần thêm
     */
    public void insertQuestion(Question question) {
        repository.insertQuestion(question);
    }
    
    /**
     * Thêm nhiều câu hỏi cùng lúc
     * @param questions Danh sách câu hỏi cần thêm
     */
    public void insertQuestions(List<Question> questions) {
        repository.insertQuestions(questions);
    }
    
    /**
     * Cập nhật câu hỏi
     * @param question Câu hỏi cần cập nhật
     */
    public void updateQuestion(Question question) {
        repository.updateQuestion(question);
    }
    
    /**
     * Xóa câu hỏi
     * @param question Câu hỏi cần xóa
     */
    public void deleteQuestion(Question question) {
        repository.deleteQuestion(question);
    }
    
    // ============= PROGRESS METHODS =============
    
    /**
     * Lấy tất cả tiến độ
     * @return LiveData chứa danh sách tiến độ
     */
    public LiveData<List<UserProgress>> getAllProgress() {
        return repository.getAllProgress();
    }
    
    /**
     * Lấy tiến độ của một bài học
     * @param lessonId ID của bài học
     * @return LiveData chứa tiến độ
     */
    public LiveData<UserProgress> getProgressByLesson(int lessonId) {
        return repository.getProgressByLesson(lessonId);
    }
    
    /**
     * Lấy các bài đã hoàn thành
     * @return LiveData chứa danh sách tiến độ của các bài đã hoàn thành
     */
    public LiveData<List<UserProgress>> getCompletedLessons() {
        return repository.getCompletedLessons();
    }
    
    /**
     * Lấy các bài đang làm dở
     * @return LiveData chứa danh sách tiến độ của các bài đang làm
     */
    public LiveData<List<UserProgress>> getInProgressLessons() {
        return repository.getInProgressLessons();
    }
    
    /**
     * Lưu hoặc cập nhật tiến độ
     * @param progress Tiến độ cần lưu
     */
    public void saveProgress(UserProgress progress) {
        repository.saveProgress(progress);
    }
    
    /**
     * Xóa tiến độ
     * @param progress Tiến độ cần xóa
     */
    public void deleteProgress(UserProgress progress) {
        repository.deleteProgress(progress);
    }
    
    /**
     * Xóa tiến độ của một bài học
     * @param lessonId ID của bài học
     */
    public void deleteProgressByLesson(int lessonId) {
        repository.deleteProgressByLesson(lessonId);
    }
    
    // ============= STATISTICS METHODS =============
    
    /**
     * Lấy điểm trung bình của tất cả bài đã hoàn thành
     * @return LiveData chứa điểm trung bình
     */
    public LiveData<Float> getAverageScore() {
        return averageScore;
    }
    
    /**
     * Lấy số bài đã hoàn thành
     * @return LiveData chứa số lượng bài đã hoàn thành
     */
    public LiveData<Integer> getCompletedLessonCount() {
        return completedLessonCount;
    }
    
    /**
     * Lấy điểm cao nhất
     * @return LiveData chứa điểm cao nhất
     */
    public LiveData<Float> getHighestScore() {
        return highestScore;
    }
}
