package vn.ltdidong.apphoctienganh.api;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.ltdidong.apphoctienganh.models.GroqChatCompletionRequest;
import vn.ltdidong.apphoctienganh.models.GroqChatCompletionResponse;

public final class AiService {
    private static final String BASE_URL = "https://api.groq.com/openai/v1/";
    private static AiService instance;

    private final GroqApi groqApi;

    private AiService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.groqApi = retrofit.create(GroqApi.class);
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
}

