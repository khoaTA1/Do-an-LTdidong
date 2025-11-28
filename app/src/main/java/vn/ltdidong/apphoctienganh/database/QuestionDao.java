package vn.ltdidong.apphoctienganh.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import vn.ltdidong.apphoctienganh.models.Question;

import java.util.List;

/**
 * DAO cho Question entity
 * Quản lý các câu hỏi của bài học
 */
@Dao
public interface QuestionDao {
    
    /**
     * Lấy tất cả câu hỏi của một bài học
     * Sắp xếp theo orderIndex
     * @param lessonId ID của bài học
     */
    @Query("SELECT * FROM questions WHERE lessonId = :lessonId ORDER BY orderIndex ASC")
    LiveData<List<Question>> getQuestionsByLesson(int lessonId);
    
    /**
     * Lấy tất cả câu hỏi của một bài học (không dùng LiveData)
     * Dùng cho các operation đồng bộ
     */
    @Query("SELECT * FROM questions WHERE lessonId = :lessonId ORDER BY orderIndex ASC")
    List<Question> getQuestionsByLessonSync(int lessonId);
    
    /**
     * Lấy một câu hỏi cụ thể theo ID
     */
    @Query("SELECT * FROM questions WHERE id = :questionId")
    LiveData<Question> getQuestionById(int questionId);
    
    /**
     * Thêm một câu hỏi mới
     * @return ID của câu hỏi vừa được thêm
     */
    @Insert
    long insertQuestion(Question question);
    
    /**
     * Thêm nhiều câu hỏi cùng lúc
     * @return Mảng IDs của các câu hỏi vừa được thêm
     */
    @Insert
    long[] insertQuestions(List<Question> questions);
    
    /**
     * Cập nhật câu hỏi
     */
    @Update
    void updateQuestion(Question question);
    
    /**
     * Xóa câu hỏi
     */
    @Delete
    void deleteQuestion(Question question);
    
    /**
     * Xóa tất cả câu hỏi của một bài học
     */
    @Query("DELETE FROM questions WHERE lessonId = :lessonId")
    void deleteQuestionsByLesson(int lessonId);
    
    /**
     * Đếm số câu hỏi của một bài học
     */
    @Query("SELECT COUNT(*) FROM questions WHERE lessonId = :lessonId")
    int getQuestionCountByLesson(int lessonId);
}
