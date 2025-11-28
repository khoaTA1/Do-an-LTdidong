package vn.ltdidong.apphoctienganh.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Entity đại diện cho một bài học Listening
 * Lưu trữ thông tin về bài học, audio URL và các câu hỏi
 */
@Entity(tableName = "listening_lessons")
public class ListeningLesson {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    // Tiêu đề bài học
    private String title;
    
    // Mô tả ngắn về bài học
    private String description;
    
    // Độ khó: EASY, MEDIUM, HARD
    private String difficulty;
    
    // Đường dẫn hoặc URL tới file audio
    // Có thể là đường dẫn local (raw resource) hoặc URL từ server
    private String audioUrl;
    
    // Thời lượng audio (tính bằng giây)
    private int duration;
    
    // Transcript của audio (hiển thị sau khi hoàn thành)
    private String transcript;
    
    // Ảnh thumbnail cho bài học
    private String imageUrl;
    
    // Số lượng câu hỏi trong bài
    private int questionCount;

    // Constructor
    public ListeningLesson() {
    }

    // @Ignore annotation để Room không sử dụng constructor này
    @Ignore
    public ListeningLesson(String title, String description, String difficulty, 
                          String audioUrl, int duration, String transcript, 
                          String imageUrl, int questionCount) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.audioUrl = audioUrl;
        this.duration = duration;
        this.transcript = transcript;
        this.imageUrl = imageUrl;
        this.questionCount = questionCount;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }
    
    /**
     * Helper method để format thời lượng audio
     * @return String theo format "MM:SS"
     */
    public String getFormattedDuration() {
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
