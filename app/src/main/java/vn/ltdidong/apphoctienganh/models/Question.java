package vn.ltdidong.apphoctienganh.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import vn.ltdidong.apphoctienganh.models.ListeningLesson;

/**
 * Entity đại diện cho câu hỏi trắc nghiệm
 * Mỗi câu hỏi thuộc về một bài học listening
 */
@Entity(tableName = "questions",
        foreignKeys = @ForeignKey(
                entity = ListeningLesson.class,
                parentColumns = "id",
                childColumns = "lessonId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index(value = "lessonId")})
public class Question {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    // ID của bài học mà câu hỏi này thuộc về
    private int lessonId;
    
    // Nội dung câu hỏi
    private String questionText;
    
    // Các lựa chọn (4 đáp án)
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    
    // Đáp án đúng (A, B, C hoặc D)
    private String correctAnswer;
    
    // Giải thích cho đáp án (optional)
    private String explanation;
    
    // Thứ tự câu hỏi trong bài
    private int orderIndex;

    // Constructor
    public Question() {
    }

    // @Ignore annotation để Room không sử dụng constructor này
    @Ignore
    public Question(int lessonId, String questionText, String optionA, 
                   String optionB, String optionC, String optionD, 
                   String correctAnswer, String explanation, int orderIndex) {
        this.lessonId = lessonId;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.orderIndex = orderIndex;
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

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
    
    /**
     * Helper method để lấy text của option theo key
     * @param optionKey A, B, C hoặc D
     * @return Text của option tương ứng
     */
    public String getOptionByKey(String optionKey) {
        switch (optionKey.toUpperCase()) {
            case "A": return optionA;
            case "B": return optionB;
            case "C": return optionC;
            case "D": return optionD;
            default: return "";
        }
    }
}
