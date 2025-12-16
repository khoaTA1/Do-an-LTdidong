package vn.ltdidong.apphoctienganh.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeminiRequest {
    private List<Content> contents;

    // Constructor for text-only requests
    public GeminiRequest(String text) {
        this.contents = Collections.singletonList(new Content(new Part(text)));
    }

    // Constructor for text + image requests
    public GeminiRequest(String text, String base64Image) {
        List<Part> parts = new ArrayList<>();
        parts.add(new Part(text));
        parts.add(new Part(new InlineData("image/jpeg", base64Image)));
        this.contents = Collections.singletonList(new Content(parts));
    }

    private static class Content {
        private List<Part> parts;
        public Content(List<Part> parts) { this.parts = parts; }
        public Content(Part part) { this.parts = Collections.singletonList(part); }
    }

    private static class Part {
        private String text;
        private InlineData inline_data;

        public Part(String text) { this.text = text; }
        public Part(InlineData inlineData) { this.inline_data = inlineData; }
    }

    private static class InlineData {
        private String mime_type;
        private String data;

        public InlineData(String mimeType, String data) {
            this.mime_type = mimeType;
            this.data = data;
        }
    }
}
