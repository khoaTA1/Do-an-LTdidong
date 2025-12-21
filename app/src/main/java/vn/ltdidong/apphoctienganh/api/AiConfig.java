package vn.ltdidong.apphoctienganh.api;

import vn.ltdidong.apphoctienganh.BuildConfig;

public final class AiConfig {
    private static final String DEFAULT_CHAT_MODEL = "llama-3.3-70b-versatile";
    private static final String DEFAULT_VISION_MODEL = "llama-3.2-11b-vision-preview";

    private AiConfig() {
    }

    public static String getAuthorizationHeader() {
        String apiKey = BuildConfig.GROQ_API_KEY;
        if (apiKey == null) apiKey = "";
        apiKey = apiKey.trim();
        return "Bearer " + apiKey;
    }

    public static String getChatModel() {
        String model = BuildConfig.GROQ_CHAT_MODEL;
        if (model == null) model = "";
        model = model.trim();
        return model.isEmpty() ? DEFAULT_CHAT_MODEL : model;
    }

    public static String getVisionModel() {
        String model = BuildConfig.GROQ_VISION_MODEL;
        if (model == null) model = "";
        model = model.trim();
        return model.isEmpty() ? DEFAULT_VISION_MODEL : model;
    }
}

