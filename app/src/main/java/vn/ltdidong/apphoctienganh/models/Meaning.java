package vn.ltdidong.apphoctienganh.models;

import java.io.Serializable;
import java.util.List;

public class Meaning implements Serializable {
    private String partOfSpeech;
    private List<Definition> definitions;
    private List<String> synonyms;
    private List<String> antonyms;

    public String getPartOfSpeech() { return partOfSpeech; }
    public List<Definition> getDefinitions() { return definitions; }
    public List<String> getSynonyms() { return synonyms; }
    public List<String> getAntonyms() { return antonyms; }
}