package vn.ltdidong.apphoctienganh.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import vn.ltdidong.apphoctienganh.models.ListeningLesson;
import vn.ltdidong.apphoctienganh.models.Question;
import vn.ltdidong.apphoctienganh.models.UserProgress;
import vn.ltdidong.apphoctienganh.repositories.FirebaseListeningRepo;
import vn.ltdidong.apphoctienganh.repositories.ListeningRepo;

import java.util.List;

/**
 * ViewModel cho Listening feature
 * Quản lý UI-related data và survive configuration changes (như xoay màn hình)
 * Giao tiếp giữa UI (Activity/Fragment) và Repository
 * 
 * **ĐANG DÙNG FIREBASE** - Load dữ liệu từ Firebase Firestore
 */
public class ListeningViewModel extends AndroidViewModel {
    
    // Repository - Chuyển sang Firebase
    private final FirebaseListeningRepo firebaseRepository;
    private final ListeningRepo localRepository; // Giữ lại cho UserProgress
    
    /**
     * Constructor
     * @param application Application context
     */
    public ListeningViewModel(@NonNull Application application) {
        super(application);
        // Khởi tạo Firebase repository để load lessons và questions
        this.firebaseRepository = new FirebaseListeningRepo();
        // Giữ lại local repository cho UserProgress
        this.localRepository = new ListeningRepo(application);
    }
    
    // ============= LESSON METHODS (Firebase) =============
    
    /**
     * Lấy tất cả bài học từ Firebase
     * @return LiveData chứa danh sách bài học
     */
    public LiveData<List<ListeningLesson>> getAllLessons() {
        return firebaseRepository.getAllLessons();
    }
    
    /**
     * Lấy bài học theo độ khó từ Firebase
     * @param difficulty Độ khó: EASY, MEDIUM, HARD
     * @return LiveData chứa danh sách bài học
     */
    public LiveData<List<ListeningLesson>> getLessonsByDifficulty(String difficulty) {
        return firebaseRepository.getLessonsByDifficulty(difficulty);
    }
    
    /**
     * Lấy một bài học cụ thể theo ID từ Firebase
     * @param lessonId ID của bài học
     * @return LiveData chứa bài học
     */
    public LiveData<ListeningLesson> getLessonById(int lessonId) {
        return firebaseRepository.getLessonById(lessonId);
    }
    
    /**
     * Thêm bài học mới (không dùng trong app)
     * @param lesson Bài học cần thêm
     */
    public void insertLesson(ListeningLesson lesson) {
        // Không implement - chỉ admin mới được thêm lesson lên Firebase
    }
    
    /**
     * Cập nhật bài học (không dùng trong app)
     * @param lesson Bài học cần cập nhật
     */
    public void updateLesson(ListeningLesson lesson) {
        // Không implement - chỉ admin mới được sửa lesson trên Firebase
    }
    
    /**
     * Xóa bài học (không dùng trong app)
     * @param lesson Bài học cần xóa
     */
    public void deleteLesson(ListeningLesson lesson) {
        // Không implement - chỉ admin mới được xóa lesson trên Firebase
    }
    
    // ============= QUESTION METHODS (Firebase) =============
    
    /**
     * Lấy tất cả câu hỏi của một bài học từ Firebase
     * @param lessonId ID của bài học
     * @return LiveData chứa danh sách câu hỏi
     */
    public LiveData<List<Question>> getQuestionsByLesson(int lessonId) {
        return firebaseRepository.getQuestionsByLesson(lessonId);
    }
    
    /**
     * Thêm câu hỏi mới (không dùng trong app)
     * @param question Câu hỏi cần thêm
     */
    public void insertQuestion(Question question) {
        // Không implement - chỉ admin mới được thêm question lên Firebase
    }
    
    /**
     * Thêm nhiều câu hỏi cùng lúc (không dùng trong app)
     * @param questions Danh sách câu hỏi cần thêm
     */
    public void insertQuestions(List<Question> questions) {
        // Không implement - chỉ admin mới được thêm question lên Firebase
    }
    
    /**
     * Cập nhật câu hỏi (không dùng trong app)
     * @param question Câu hỏi cần cập nhật
     */
    public void updateQuestion(Question question) {
        // Không implement - chỉ admin mới được sửa question trên Firebase
    }
    
    /**
     * Xóa câu hỏi (không dùng trong app)
     * @param question Câu hỏi cần xóa
     */
    public void deleteQuestion(Question question) {
        // Không implement - chỉ admin mới được xóa question trên Firebase
    }
    
    // ============= PROGRESS METHODS (Local Database) =============
    
    /**
     * Lấy tất cả tiến độ (vẫn dùng local database)
     * @return LiveData chứa danh sách tiến độ
     */
    public LiveData<List<UserProgress>> getAllProgress() {
        return localRepository.getAllProgress();
    }
    
    /**
     * Lấy tiến độ của một bài học (vẫn dùng local database)
     * @param lessonId ID của bài học
     * @return LiveData chứa tiến độ
     */
    public LiveData<UserProgress> getProgressByLesson(int lessonId) {
        return localRepository.getProgressByLesson(lessonId);
    }
    
    /**
     * Lấy các bài đã hoàn thành (vẫn dùng local database)
     * @return LiveData chứa danh sách tiến độ của các bài đã hoàn thành
     */
    public LiveData<List<UserProgress>> getCompletedLessons() {
        return localRepository.getCompletedLessons();
    }
    
    /**
     * Lấy các bài đang làm dở (vẫn dùng local database)
     * @return LiveData chứa danh sách tiến độ của các bài đang làm
     */
    public LiveData<List<UserProgress>> getInProgressLessons() {
        return localRepository.getInProgressLessons();
    }
    
    /**
     * Lưu hoặc cập nhật tiến độ (vẫn dùng local database)
     * @param progress Tiến độ cần lưu
     */
    public void saveProgress(UserProgress progress) {
        localRepository.saveProgress(progress);
    }
    
    /**
     * Xóa tiến độ (vẫn dùng local database)
     * @param progress Tiến độ cần xóa
     */
    public void deleteProgress(UserProgress progress) {
        localRepository.deleteProgress(progress);
    }
    
    /**
     * Xóa tiến độ của một bài học (vẫn dùng local database)
     * @param lessonId ID của bài học
     */
    public void deleteProgressByLesson(int lessonId) {
        localRepository.deleteProgressByLesson(lessonId);
    }
    
    // ============= STATISTICS METHODS (Local Database) =============
    
    /**
     * Lấy điểm trung bình của tất cả bài đã hoàn thành (vẫn dùng local database)
     * @return LiveData chứa điểm trung bình
     */
    public LiveData<Float> getAverageScore() {
        return localRepository.getAverageScore();
    }
    
    /**
     * Lấy số bài đã hoàn thành (vẫn dùng local database)
     * @return LiveData chứa số lượng bài đã hoàn thành
     */
    public LiveData<Integer> getCompletedLessonCount() {
        return localRepository.getCompletedLessonCount();
    }
    
    /**
     * Lấy điểm cao nhất (vẫn dùng local database)
     * @return LiveData chứa điểm cao nhất
     */
    public LiveData<Float> getHighestScore() {
        return localRepository.getHighestScore();
    }
}
