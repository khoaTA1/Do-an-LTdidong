package vn.ltdidong.apphoctienganh.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import vn.ltdidong.apphoctienganh.models.NewsResponse;

public interface NewsApi {
    @GET("v2/everything")
    Call<NewsResponse> getEnglishNews(
        @Query("q") String query,
        @Query("language") String language,
        @Query("sortBy") String sortBy,
        @Query("pageSize") int pageSize,
        @Query("apiKey") String apiKey
    );
}
