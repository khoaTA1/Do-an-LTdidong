package vn.ltdidong.apphoctienganh.models;

public class MMCard {

    private int id;
    private int pairId;
    private String text;
    private boolean isEnglish;
    private boolean isMatched = false;
    private boolean isFaceUp = false;

    public MMCard() {}
    public MMCard(int id, int pairId, String text, boolean isEnglish) {
        this.id = id;
        this.pairId = pairId;
        this.text = text;
        this.isEnglish = isEnglish;
    }

    public int getPairId() {
        return pairId;
    }

    public int getId() { return id; }
    public String getText() { return text; }
    public boolean isEnglish() { return isEnglish; }

    public boolean isMatched() { return isMatched; }
    public void setMatched(boolean matched) { isMatched = matched; }

    public boolean isFaceUp() { return isFaceUp; }
    public void setFaceUp(boolean faceUp) { isFaceUp = faceUp; }
}

