package vn.ltdidong.apphoctienganh.models;

public class CrosswordRow {
    private int index;
    private String word; // The correct answer (e.g., "HELLO")
    private String question; // The clue/question
    private String currentInput; // What the user has currently filled (or revealed)
    private boolean isSolved;
    private int keyIndex = -1; // Index of the character that is part of the vertical keyword

    public CrosswordRow(int index, String word, String question) {
        this(index, word, question, -1);
    }

    public CrosswordRow(int index, String word, String question, int keyIndex) {
        this.index = index;
        this.word = word != null ? word.toUpperCase().trim() : "";
        this.question = question;
        this.currentInput = "";
        this.isSolved = false;
        this.keyIndex = keyIndex;
    }

    public int getIndex() {
        return index;
    }

    public String getWord() {
        return word;
    }

    public String getQuestion() {
        return question;
    }

    public String getCurrentInput() {
        return currentInput;
    }

    public void setCurrentInput(String currentInput) {
        this.currentInput = currentInput != null ? currentInput.toUpperCase().trim() : "";
    }

    public boolean isSolved() {
        return isSolved;
    }

    public void setSolved(boolean solved) {
        isSolved = solved;
    }

    public int getKeyIndex() {
        return keyIndex;
    }

    public void setKeyIndex(int keyIndex) {
        this.keyIndex = keyIndex;
    }
}