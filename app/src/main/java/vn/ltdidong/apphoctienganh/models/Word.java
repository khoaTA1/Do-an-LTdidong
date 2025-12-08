package vn.ltdidong.apphoctienganh.models;

import java.util.List;

public class Word {
    private String word;
    private List<String> pos;
    private List<String> syn;

    // constructors
    public Word() {}

    public Word(String word, List<String> pos, List<String> syn) {
        this.word = word;
        this.pos = pos;
        this.syn = syn;
    }

    // getters, setters

    public List<String> getSyn() {
        return syn;
    }

    public void setSyn(List<String> syn) {
        this.syn = syn;
    }

    public List<String> getPos() {
        return pos;
    }

    public void setPos(List<String> pos) {
        this.pos = pos;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
