package vn.ltdidong.apphoctienganh.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import vn.ltdidong.apphoctienganh.models.ListeningLesson;

import java.util.List;

/**
 * DAO (Data Access Object) cho ListeningLesson
 * Định nghĩa các phương thức truy vấn database
 */
@Dao
public interface ListeningLessonDao {
    
    /**
     * Lấy tất cả bài học, sắp xếp theo ID
     * Trả về LiveData để tự động cập nhật UI khi data thay đổi
     */
    @Query("SELECT * FROM listening_lessons ORDER BY id ASC")
    LiveData<List<ListeningLesson>> getAllLessons();
    
    /**
     * Lấy bài học theo độ khó
     * @param difficulty Độ khó: EASY, MEDIUM hoặc HARD
     */
    @Query("SELECT * FROM listening_lessons WHERE difficulty = :difficulty ORDER BY id ASC")
    LiveData<List<ListeningLesson>> getLessonsByDifficulty(String difficulty);
    
    /**
     * Lấy một bài học cụ thể theo ID
     * @param lessonId ID của bài học
     */
    @Query("SELECT * FROM listening_lessons WHERE id = :lessonId")
    LiveData<ListeningLesson> getLessonById(int lessonId);
    
    /**
     * Lấy một bài học cụ thể theo ID (không dùng LiveData)
     * Dùng cho các operation đồng bộ
     */
    @Query("SELECT * FROM listening_lessons WHERE id = :lessonId")
    ListeningLesson getLessonByIdSync(int lessonId);
    
    /**
     * Thêm một bài học mới
     * @return ID của bài học vừa được thêm
     */
    @Insert
    long insertLesson(ListeningLesson lesson);
    
    /**
     * Thêm nhiều bài học cùng lúc
     * @return Mảng IDs của các bài học vừa được thêm
     */
    @Insert
    long[] insertLessons(List<ListeningLesson> lessons);
    
    /**
     * Cập nhật thông tin bài học
     */
    @Update
    void updateLesson(ListeningLesson lesson);
    
    /**
     * Xóa bài học
     */
    @Delete
    void deleteLesson(ListeningLesson lesson);
    
    /**
     * Xóa tất cả bài học
     */
    @Query("DELETE FROM listening_lessons")
    void deleteAllLessons();
    
    /**
     * Đếm tổng số bài học
     */
    @Query("SELECT COUNT(*) FROM listening_lessons")
    LiveData<Integer> getLessonCount();
    
    /**
     * Đếm số bài học theo độ khó
     */
    @Query("SELECT COUNT(*) FROM listening_lessons WHERE difficulty = :difficulty")
    LiveData<Integer> getLessonCountByDifficulty(String difficulty);
}
