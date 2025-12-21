package vn.ltdidong.apphoctienganh.api;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.ltdidong.apphoctienganh.models.GeminiRequest;
import vn.ltdidong.apphoctienganh.models.GeminiResponse;
import vn.ltdidong.apphoctienganh.models.GroqChatCompletionRequest;
import vn.ltdidong.apphoctienganh.models.GroqChatCompletionResponse;

public final class AiService {
    private static final String GROQ_BASE_URL = "https://api.groq.com/openai/v1/";
    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/";
    
    private static AiService instance;

    private final GroqApi groqApi;
    private final GeminiApi geminiApi;

    private AiService() {
        Retrofit groqRetrofit = new Retrofit.Builder()
                .baseUrl(GROQ_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.groqApi = groqRetrofit.create(GroqApi.class);

        Retrofit geminiRetrofit = new Retrofit.Builder()
                .baseUrl(GEMINI_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.geminiApi = geminiRetrofit.create(GeminiApi.class);
    }

    public static synchronized AiService getInstance() {
        if (instance == null) {
            instance = new AiService();
        }
        return instance;
    }

    public Call<GroqChatCompletionResponse> generateText(String prompt) {
        GroqChatCompletionRequest request = GroqChatCompletionRequest.forTextPrompt(AiConfig.getChatModel(), prompt);
        return groqApi.createChatCompletion(AiConfig.getAuthorizationHeader(), request);
    }

    public Call<GroqChatCompletionResponse> generateVision(String prompt, String base64Jpeg) {
        GroqChatCompletionRequest request = GroqChatCompletionRequest.forVisionPrompt(AiConfig.getVisionModel(), prompt, base64Jpeg);
        return groqApi.createChatCompletion(AiConfig.getAuthorizationHeader(), request);
    }

    public Call<GeminiResponse> generateGeminiVision(String prompt, String base64Jpeg) {
        GeminiRequest request = new GeminiRequest(prompt, base64Jpeg);
        return geminiApi.generateContent(AiConfig.getGeminiApiKey(), request);
    }
}
