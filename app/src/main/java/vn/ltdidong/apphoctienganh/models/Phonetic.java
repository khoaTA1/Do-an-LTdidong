package vn.ltdidong.apphoctienganh.models;

import java.io.Serializable;

public class Phonetic implements Serializable {
    private String text;
    private String audio;
    private String sourceUrl;
    private License license; // Object License lồng bên trong

    public String getText() { return text; }
    public String getAudio() { return audio; }
    public String getSourceUrl() { return sourceUrl; }
    public License getLicense() { return license; }
}