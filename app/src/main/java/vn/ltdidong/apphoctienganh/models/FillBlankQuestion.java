package vn.ltdidong.apphoctienganh.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho câu hỏi Fill-in-the-blank
 * Người dùng nghe audio và điền từ còn thiếu vào chỗ trống
 */
@Entity(tableName = "fill_blank_questions",
        indices = {@Index(value = "lessonId")})
public class FillBlankQuestion {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    // ID của bài học
    private int lessonId;
    
    // Câu văn có chỗ trống (dùng {blank} để đánh dấu chỗ trống)
    // Ví dụ: "I wake up at {blank} every day."
    private String sentenceWithBlanks;
    
    // Danh sách từ đúng cho các chỗ trống (cách nhau bởi dấu |)
    // Ví dụ: "7 AM|brush my teeth|breakfast"
    private String correctAnswers;
    
    // Gợi ý cho người dùng (optional)
    private String hint;
    
    // Thứ tự câu hỏi trong bài
    private int orderIndex;
    
    // URL audio riêng cho câu hỏi này (optional - nếu mỗi câu có audio riêng)
    private String audioUrl;

    // Constructor
    public FillBlankQuestion() {
    }

    @Ignore
    public FillBlankQuestion(int lessonId, String sentenceWithBlanks, 
                            String correctAnswers, String hint, 
                            int orderIndex, String audioUrl) {
        this.lessonId = lessonId;
        this.sentenceWithBlanks = sentenceWithBlanks;
        this.correctAnswers = correctAnswers;
        this.hint = hint;
        this.orderIndex = orderIndex;
        this.audioUrl = audioUrl;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLessonId() {
        return lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }

    public String getSentenceWithBlanks() {
        return sentenceWithBlanks;
    }

    public void setSentenceWithBlanks(String sentenceWithBlanks) {
        this.sentenceWithBlanks = sentenceWithBlanks;
    }

    public String getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(String correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
    
    public String getAudioUrl() {
        return audioUrl;
    }
    
    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
    
    /**
     * Lấy số lượng chỗ trống trong câu
     */
    public int getBlankCount() {
        if (sentenceWithBlanks == null) return 0;
        int count = 0;
        int index = 0;
        while ((index = sentenceWithBlanks.indexOf("{blank}", index)) != -1) {
            count++;
            index += 7; // Length of "{blank}"
        }
        return count;
    }
    
    /**
     * Lấy danh sách đáp án đúng dưới dạng List
     */
    public List<String> getCorrectAnswersList() {
        List<String> answers = new ArrayList<>();
        if (correctAnswers != null && !correctAnswers.isEmpty()) {
            String[] parts = correctAnswers.split("\\|");
            for (String part : parts) {
                answers.add(part.trim().toLowerCase());
            }
        }
        return answers;
    }
    
    /**
     * Kiểm tra câu trả lời của người dùng
     * @param userAnswers Danh sách câu trả lời của user
     * @return Số câu trả lời đúng
     */
    public int checkAnswers(List<String> userAnswers) {
        List<String> correctList = getCorrectAnswersList();
        if (userAnswers.size() != correctList.size()) {
            return 0;
        }
        
        int correctCount = 0;
        for (int i = 0; i < correctList.size(); i++) {
            String userAns = userAnswers.get(i).trim().toLowerCase();
            String correctAns = correctList.get(i);
            
            // So sánh với một số biến thể
            if (userAns.equals(correctAns) || 
                userAns.replace("'", "").equals(correctAns.replace("'", "")) ||
                userAns.replace(" ", "").equals(correctAns.replace(" ", ""))) {
                correctCount++;
            }
        }
        return correctCount;
    }
    
    /**
     * Tạo câu hoàn chỉnh từ câu trả lời của user
     * @param userAnswers Danh sách câu trả lời
     * @return Câu đã điền
     */
    public String getFilledSentence(List<String> userAnswers) {
        String result = sentenceWithBlanks;
        for (String answer : userAnswers) {
            result = result.replaceFirst("\\{blank\\}", answer);
        }
        return result;
    }
    
    /**
     * Tạo câu hoàn chỉnh với đáp án đúng
     */
    public String getCorrectSentence() {
        return getFilledSentence(getCorrectAnswersList());
    }
}
