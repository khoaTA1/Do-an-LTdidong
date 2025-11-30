package vn.ltdidong.apphoctienganh.models;

public class ChatMessage {
    private String message;
    private boolean isUser; // true if user sent, false if AI sent

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }
}
