package vn.ltdidong.apphoctienganh.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import vn.ltdidong.apphoctienganh.models.GroqChatCompletionRequest;
import vn.ltdidong.apphoctienganh.models.GroqChatCompletionResponse;

public interface GroqApi {
    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    Call<GroqChatCompletionResponse> createChatCompletion(
            @Header("Authorization") String authorization,
            @Body GroqChatCompletionRequest request
    );
}

