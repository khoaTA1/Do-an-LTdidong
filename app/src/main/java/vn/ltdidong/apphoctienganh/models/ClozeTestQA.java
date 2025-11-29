package vn.ltdidong.apphoctienganh.models;

public class ClozeTestQA {
    private long id;
    private String question;
    private String answer;

    // constructors
    public ClozeTestQA() {
    }

    public ClozeTestQA(String answer, String question, long id) {
        this.answer = answer;
        this.question = question;
        this.id = id;
    }

    // getters, setters

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
