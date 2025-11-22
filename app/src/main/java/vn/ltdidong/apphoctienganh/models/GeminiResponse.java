package vn.ltdidong.apphoctienganh.models;

import java.util.List;

public class GeminiResponse {
    private List<Candidate> candidates;

    public String getOutputText() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate candidate = candidates.get(0);
            if (candidate.content != null && candidate.content.parts != null && !candidate.content.parts.isEmpty()) {
                return candidate.content.parts.get(0).text;
            }
        }
        return "No response";
    }

    public static class Candidate {
        private Content content;
    }

    public static class Content {
        private List<Part> parts;
    }

    public static class Part {
        private String text;
    }
}