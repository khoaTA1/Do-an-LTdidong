package vn.ltdidong.apphoctienganh.models;

/**
 * Model đại diện cho một bài học Fill Blank
 */
public class FillBlankLesson {
    private String id;           // Document ID từ Firebase
    private String title;        // Tiêu đề bài học
    private String audioUrl;     // URL audio (optional)
    private int questionCount;   // Số lượng câu hỏi

    public FillBlankLesson() {
    }

    public FillBlankLesson(String id, String title, String audioUrl, int questionCount) {
        this.id = id;
        this.title = title;
        this.audioUrl = audioUrl;
        this.questionCount = questionCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public boolean hasAudio() {
        return audioUrl != null && !audioUrl.isEmpty();
    }
}
