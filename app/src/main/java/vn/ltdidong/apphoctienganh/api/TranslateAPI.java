package vn.ltdidong.apphoctienganh.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import vn.ltdidong.apphoctienganh.models.TranslateRequest;
import vn.ltdidong.apphoctienganh.models.TranslateResponse;
import vn.ltdidong.apphoctienganh.models.TranslateResponseWrapper;

public interface TranslateAPI {

    @POST("translate")
    Call<TranslateResponse> translateBatch(
            @Body TranslateRequest request
    );
}