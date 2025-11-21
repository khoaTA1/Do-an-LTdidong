package vn.ltdidong.apphoctienganh.models;

import java.io.Serializable;
import java.util.List;

public class WordEntry implements Serializable {
    private String word;
    private String phonetic; // JSON mới thỉnh thoảng vẫn có trường này, giữ lại cho an toàn
    private List<Phonetic> phonetics;
    private List<Meaning> meanings;
    private License license;
    private List<String> sourceUrls;

    public String getWord() { return word; }
    public String getPhonetic() { return phonetic; }
    public List<Phonetic> getPhonetics() { return phonetics; }
    public List<Meaning> getMeanings() { return meanings; }
    public License getLicense() { return license; }
    public List<String> getSourceUrls() { return sourceUrls; }
}