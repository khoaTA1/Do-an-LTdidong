package vn.ltdidong.apphoctienganh.models;

import java.io.Serializable;
import java.util.List;

public class WordEntry implements Serializable {
    // Các trường khớp với JSON API Dictionary
    private String word;
    private String phonetic;
    private List<Phonetic> phonetics;
    private List<Meaning> meanings;
    private License license;
    private List<String> sourceUrls;
    private String origin; // JSON có trường origin

    public WordEntry() { }

    // --- GETTER & SETTER ---
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public List<Phonetic> getPhonetics() { return phonetics; }
    public void setPhonetics(List<Phonetic> phonetics) { this.phonetics = phonetics; }

    public List<Meaning> getMeanings() { return meanings; }
    public void setMeanings(List<Meaning> meanings) { this.meanings = meanings; }

    public License getLicense() { return license; }
    public void setLicense(License license) { this.license = license; }

    public List<String> getSourceUrls() { return sourceUrls; }
    public void setSourceUrls(List<String> sourceUrls) { this.sourceUrls = sourceUrls; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    // --- HELPER METHODS (Xử lý logic hiển thị) ---

    // 1. Lấy định nghĩa đầu tiên (Sẽ trả về chuỗi Tiếng Anh từ API)
    public String getFirstDefinition() {
        if (meanings != null && !meanings.isEmpty()) {
            for (Meaning m : meanings) {
                if (m.getDefinitions() != null && !m.getDefinitions().isEmpty()) {
                    // Lấy định nghĩa đầu tiên tìm thấy
                    return m.getDefinitions().get(0).getDefinition();
                }
            }
        }
        return "No definition found."; // Không tìm thấy nghĩa
    }

    // 2. Lấy link audio chuẩn (Ưu tiên link không rỗng)
    public String getAudioUrl() {
        if (phonetics != null) {
            for (Phonetic p : phonetics) {
                if (p.getAudio() != null && !p.getAudio().isEmpty()) {
                    String url = p.getAudio();
                    // Fix trường hợp API trả về thiếu "https:" (dù JSON này có full link nhưng cứ đề phòng)
                    if (url.startsWith("//")) {
                        return "https:" + url;
                    }
                    return url;
                }
            }
        }
        return null; // Không có audio
    }

    // ==========================================
    // CÁC CLASS CON (INNER CLASSES) - Mapping theo JSON
    // ==========================================

    public static class Phonetic implements Serializable {
        private String text;
        private String audio;
        private String sourceUrl;

        public Phonetic() {}
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getAudio() { return audio; }
        public void setAudio(String audio) { this.audio = audio; }
        public String getSourceUrl() { return sourceUrl; }
        public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    }

    public static class Meaning implements Serializable {
        private String partOfSpeech; // noun, verb, interjection...
        private List<Definition> definitions;
        private List<String> synonyms;
        private List<String> antonyms;

        public Meaning() {}
        public String getPartOfSpeech() { return partOfSpeech; }
        public void setPartOfSpeech(String partOfSpeech) { this.partOfSpeech = partOfSpeech; }
        public List<Definition> getDefinitions() { return definitions; }
        public void setDefinitions(List<Definition> definitions) { this.definitions = definitions; }
        public List<String> getSynonyms() { return synonyms; }
        public void setSynonyms(List<String> synonyms) { this.synonyms = synonyms; }
        public List<String> getAntonyms() { return antonyms; }
        public void setAntonyms(List<String> antonyms) { this.antonyms = antonyms; }
    }

    public static class Definition implements Serializable {
        private String definition;
        private String example;
        private List<String> synonyms;
        private List<String> antonyms;

        public Definition() {}
        public String getDefinition() { return definition; }
        public void setDefinition(String definition) { this.definition = definition; }
        public String getExample() { return example; }
        public void setExample(String example) { this.example = example; }
        public List<String> getSynonyms() { return synonyms; }
        public void setSynonyms(List<String> synonyms) { this.synonyms = synonyms; }
        public List<String> getAntonyms() { return antonyms; }
        public void setAntonyms(List<String> antonyms) { this.antonyms = antonyms; }
    }

    public static class License implements Serializable {
        private String name;
        private String url;
        public License() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}