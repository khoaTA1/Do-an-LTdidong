package vn.ltdidong.apphoctienganh.models;

import java.util.Map;

public class QuestionAnswer {
    private long id;
    private long passageId;
    private String question;
    private Map<Integer, String> answers;
    private int correctAnswer;
    private int userAnswer;

    // constructors
    public long getId() {
        return id;
    }

    public int getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(int userAnswer) {
        this.userAnswer = userAnswer;
    }

    public QuestionAnswer() {
    }

    public QuestionAnswer(long passageId, String question, Map<Integer, String> answers, int correctAnswer) {
        this.passageId = passageId;
        this.question = question;
        this.answers = answers;
        this.correctAnswer = correctAnswer;
    }

    // getters, setters
    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(int correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public Map<Integer, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Integer, String> answers) {
        this.answers = answers;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public long getPassageId() {
        return passageId;
    }

    public void setPassageId(long passageId) {
        this.passageId = passageId;
    }
}
