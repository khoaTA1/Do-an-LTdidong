package vn.ltdidong.apphoctienganh.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import vn.ltdidong.apphoctienganh.models.WordEntry;

public interface DictionaryApi {
    // Đường dẫn cơ sở: https://api.dictionaryapi.dev/api/v2/entries/en/

    @GET("entries/en/{word}")
    Call<List<WordEntry>> getDefinition(@Path("word") String word);
}