package vn.ltdidong.apphoctienganh.models;

import java.io.Serializable;
import java.util.List;

public class Definition implements Serializable {
    private String definition;
    private String example;
    private List<String> synonyms;
    private List<String> antonyms;

    public String getDefinition() { return definition; }
    public String getExample() { return example; }
    public List<String> getSynonyms() { return synonyms; }
    public List<String> getAntonyms() { return antonyms; }
}