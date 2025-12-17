package vn.ltdidong.apphoctienganh.models;

public class MMCard {

    private int id;
    private String text;
    private boolean isEnglish;
    private boolean isMatched = false;
    private boolean isFaceUp = false;

    public MMCard() {}
    public MMCard(int id, String text, boolean isEnglish) {
        this.id = id;
        this.text = text;
        this.isEnglish = isEnglish;
    }

    public int getId() { return id; }
    public String getText() { return text; }
    public boolean isEnglish() { return isEnglish; }

    public boolean isMatched() { return isMatched; }
    public void setMatched(boolean matched) { isMatched = matched; }

    public boolean isFaceUp() { return isFaceUp; }
    public void setFaceUp(boolean faceUp) { isFaceUp = faceUp; }
}

