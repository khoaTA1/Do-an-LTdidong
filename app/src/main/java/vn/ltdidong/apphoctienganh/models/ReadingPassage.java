package vn.ltdidong.apphoctienganh.models;

import java.util.List;

public class ReadingPassage {
    private long id;
    private String passage;
    private List<QuestionAnswer> QAList;

    // constructors
    public ReadingPassage() {
    }

    public ReadingPassage(String passage, List<QuestionAnswer> QAList) {
        this.passage = passage;
        this.QAList = QAList;
    }

    // getters, setters
    public long getId() {
        return id;
    }

    public List<QuestionAnswer> getQAList() {
        return QAList;
    }

    public void setQAList(List<QuestionAnswer> QAList) {
        this.QAList = QAList;
    }

    public String getPassage() {
        return passage;
    }

    public void setPassage(String passage) {
        this.passage = passage;
    }
}
