package vn.ltdidong.apphoctienganh.models;

import java.util.List;

public class GroqChatCompletionResponse {
    private List<Choice> choices;

    public String getOutputText() {
        if (choices == null || choices.isEmpty()) return "";
        Choice first = choices.get(0);
        if (first == null || first.message == null || first.message.content == null) return "";
        return first.message.content;
    }

    public static class Choice {
        private Message message;
    }

    public static class Message {
        private String content;
    }
}

