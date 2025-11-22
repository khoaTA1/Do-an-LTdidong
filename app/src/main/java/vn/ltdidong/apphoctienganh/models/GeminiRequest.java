package vn.ltdidong.apphoctienganh.models;

import java.util.Collections;
import java.util.List;

public class GeminiRequest {
    private List<Content> contents;

    public GeminiRequest(String text) {
        this.contents = Collections.singletonList(new Content(new Part(text)));
    }

    private static class Content {
        private List<Part> parts;
        public Content(Part part) { this.parts = Collections.singletonList(part); }
    }

    private static class Part {
        private String text;
        public Part(String text) { this.text = text; }
    }
}