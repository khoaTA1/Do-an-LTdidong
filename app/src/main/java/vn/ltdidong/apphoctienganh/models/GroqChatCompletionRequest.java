package vn.ltdidong.apphoctienganh.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroqChatCompletionRequest {
    private String model;
    private List<Message> messages;
    private Double temperature;
    private Integer max_tokens;

    public GroqChatCompletionRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }

    public GroqChatCompletionRequest(String model, List<Message> messages, Double temperature, Integer maxTokens) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
        this.max_tokens = maxTokens;
    }

    public static GroqChatCompletionRequest forTextPrompt(String model, String prompt) {
        List<Message> messages = Collections.singletonList(Message.userText(prompt));
        return new GroqChatCompletionRequest(model, messages);
    }

    public static GroqChatCompletionRequest forVisionPrompt(String model, String prompt, String base64Jpeg) {
        List<ContentPart> parts = new ArrayList<>();
        parts.add(ContentPart.text(prompt));
        parts.add(ContentPart.imageBase64Jpeg(base64Jpeg));
        List<Message> messages = Collections.singletonList(Message.userParts(parts));
        return new GroqChatCompletionRequest(model, messages);
    }

    public static class Message {
        private String role;
        private Object content;

        public Message(String role, Object content) {
            this.role = role;
            this.content = content;
        }

        public static Message userText(String text) {
            return new Message("user", text);
        }

        public static Message userParts(List<ContentPart> parts) {
            return new Message("user", parts);
        }
    }

    public static class ContentPart {
        private String type;
        private String text;
        private ImageUrl image_url;

        public static ContentPart text(String text) {
            ContentPart part = new ContentPart();
            part.type = "text";
            part.text = text;
            return part;
        }

        public static ContentPart imageBase64Jpeg(String base64Jpeg) {
            ContentPart part = new ContentPart();
            part.type = "image_url";
            part.image_url = new ImageUrl("data:image/jpeg;base64," + base64Jpeg);
            return part;
        }
    }

    public static class ImageUrl {
        private String url;

        public ImageUrl(String url) {
            this.url = url;
        }
    }
}

