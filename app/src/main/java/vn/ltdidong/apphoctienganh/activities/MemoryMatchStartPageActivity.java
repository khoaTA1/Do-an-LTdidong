package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.MMStartPageAdapter;
import vn.ltdidong.apphoctienganh.api.TranslateAPI;
import vn.ltdidong.apphoctienganh.functions.DBHelper;
import vn.ltdidong.apphoctienganh.models.TranslateRequest;
import vn.ltdidong.apphoctienganh.models.TranslateResponse;
import vn.ltdidong.apphoctienganh.models.TranslateResponseWrapper;
import vn.ltdidong.apphoctienganh.models.Word;

public class MemoryMatchStartPageActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MMStartPageAdapter modeAdapter;
    private LinearLayout dotsLayout;
    private Button btnStart;
    private ImageView[] dots;
    private DBHelper sqlite;
    private TranslateAPI translateApi;
    private ArrayList<String> enList = new ArrayList<>();
    private ArrayList<String> viList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_match_start_page);

        viewPager = findViewById(R.id.viewPagerMode);
        dotsLayout = findViewById(R.id.dots);
        btnStart = findViewById(R.id.btnStart);

        modeAdapter = new MMStartPageAdapter(this);
        viewPager.setAdapter(modeAdapter);

        setupDots(2);
        selectDot(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                selectDot(position);
            }
        });

        sqlite = new DBHelper(this);

        // API translate bằng retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        translateApi = retrofit.create(TranslateAPI.class);

        // tắt nút bắt đầu khi chưa load dữ liệu xong
        btnStart.setAlpha(0.3f);
        btnStart.setEnabled(false);

        loadData(list -> {
            for (String[] pair : list) {
                Log.d(">>> Memory Match", "Tách: " + pair[0] + " -> " + pair[1]);
                enList.add(pair[0]);
                viList.add(pair[1]);
            }

            btnStart.setOnClickListener(v -> openGame());
            btnStart.setAlpha(1f);
            btnStart.setEnabled(true);
        });

    }
    private void loadData(Consumer<List<String[]>> onResult) {
        List<String> WordEnDefault = Arrays.asList("apple", "dog", "book", "car", "house",
                "cat", "water", "tree", "tank", "phone", "chair", "sun", "moon", "river",
                "mountain", "flower");

        List<String> tempWordEn = new ArrayList<>();

        for (Word w : sqlite.getRecentWords(16)) {
            tempWordEn.add(w.getWord());
            if (tempWordEn.size() > 20) break;

            for (String syn : w.getSyn()) {
                tempWordEn.add(syn);
                if (tempWordEn.size() > 20) break;
            }
        }

        List<String> temp = new ArrayList<>();
        Log.d(">>> Memory Match", "SQLite hiện tại: " + tempWordEn.size());
        if (tempWordEn.size() >= 16) {
            Log.d(">>> Memory Match", "Sử dụng nguồn từ vựng từ SQLite");
            temp = tempWordEn;
        } else {
            Log.d(">>> Memory Match", "Sử dụng nguồn mặc định");
            temp = WordEnDefault;
        }

        Log.d(">>> Memory Match", "list En: " + temp);
        List<String> allWordEn = temp;

        // Nếu API null hoặc list rỗng, trả về cặp EN-EN tạm thời
        if (allWordEn.isEmpty()) {
            List<String[]> fallback = new ArrayList<>();
            for (String w : tempWordEn) {
                fallback.add(new String[]{w, w});
            }
            onResult.accept(fallback);
            return;
        }

        TranslateRequest request = new TranslateRequest(allWordEn, "en", "vi");

        translateApi.translateBatch(request)
                .enqueue(new Callback<TranslateResponse>() {
                    @Override
                    public void onResponse(Call<TranslateResponse> call,
                                           Response<TranslateResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<String[]> allVocab = new ArrayList<>();
                            List<String> viList = response.body().translatedText;

                            int count = Math.min(allWordEn.size(), viList.size());

                            for (int i = 0; i < count; i++) {
                                String en = allWordEn.get(i);
                                String vi = viList.get(i);
                                String[] pair = new String[]{en, vi};

                                allVocab.add(pair);
                                Log.d(">>> Memory Match", "Add pair: " + pair);
                            }

                            // callback trả về danh sách cặp EN-VI
                            Log.d(">>> Memory Match", "All: " + allVocab);
                            onResult.accept(allVocab);
                        } else {
                            Log.e("TranslateAPI", "Response not successful or body null");

                            // fallback nếu API lỗi
                            List<String[]> fallback = new ArrayList<>();
                            for (String w : allWordEn) {
                                fallback.add(new String[]{w, w});
                            }
                            onResult.accept(fallback);
                        }
                    }

                    @Override
                    public void onFailure(Call<TranslateResponse> call, Throwable t) {
                        t.printStackTrace();
                        Log.e("TranslateAPI", "API call failed");
                    }
                });
    }

    private void openGame() {

        int page = viewPager.getCurrentItem();
        Intent intent = new Intent(this, MemoryMatchActivity.class);
        intent.putStringArrayListExtra("enData", enList);
        intent.putStringArrayListExtra("viData", viList);

        if (page == 0) {
            // 1 người
            intent.putExtra("mode", 1);
            intent.putExtra("difficulty", modeAdapter.spDifficulty.getSelectedItemPosition());
        } else {
            // 2 người
            intent.putExtra("mode", 2);
            intent.putExtra("firstPlayer",
                    modeAdapter.spFirstPlayer.getSelectedItemPosition() == 0 ? 0 : 1);
        }

        startActivity(intent);
    }

    private void setupDots(int count) {
        dots = new ImageView[count];
        dotsLayout.removeAllViews();

        for (int i = 0; i < count; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageResource(R.drawable.dot_inactive);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    20, 20);
            params.setMargins(8, 0, 8, 0);
            dotsLayout.addView(dots[i], params);
        }
    }

    private void selectDot(int index) {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setImageResource(i == index ?
                    R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }
}
