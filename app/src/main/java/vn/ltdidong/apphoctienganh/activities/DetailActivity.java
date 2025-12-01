package vn.ltdidong.apphoctienganh.activities;

import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.api.DictionaryApi;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;
import vn.ltdidong.apphoctienganh.models.Definition;
import vn.ltdidong.apphoctienganh.models.Meaning;
import vn.ltdidong.apphoctienganh.models.Phonetic;
import vn.ltdidong.apphoctienganh.models.User;
import vn.ltdidong.apphoctienganh.models.WordEntry;

public class DetailActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private DictionaryApi dictionaryApi;
    private FirebaseFirestore db; // Thêm Firestore

    // UI
    private TextView tvWord, tvPhonetic, tvContent;
    private ImageButton btnBack, btnPlayAudio, btnFavorite;

    // Biến quản lý trạng thái
    private boolean isFavorite = false;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyFavoriteDictionary";
    private String userID; // ID người dùng để lưu lên mây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 1. Khởi tạo các thành phần
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Lấy User ID từ Manager của bạn
        User user = SharedPreferencesManager.getInstance(this).getUser();
        if (user != null) {
            userID = String.valueOf(user.getId());
        }

        // Cấu hình Retrofit
        Retrofit dictionaryRetrofit = new Retrofit.Builder()
                .baseUrl("https://api.dictionaryapi.dev/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        dictionaryApi = dictionaryRetrofit.create(DictionaryApi.class);

        // 2. Ánh xạ View
        tvWord = findViewById(R.id.tvDetailWord);
        tvPhonetic = findViewById(R.id.tvDetailPhonetic);
        tvContent = findViewById(R.id.tvDetailContent);
        btnBack = findViewById(R.id.btnBack);
        btnPlayAudio = findViewById(R.id.btnPlayAudio);
        btnFavorite = findViewById(R.id.btnFavorite);

        btnBack.setOnClickListener(v -> finish());

        // 3. Nhận dữ liệu
        WordEntry wordEntry = (WordEntry) getIntent().getSerializableExtra("WORD_DATA");

        if (wordEntry != null) {
            String currentWord = wordEntry.getWord();
            tvWord.setText(currentWord);

            // Kiểm tra xem đã thích chưa (Ưu tiên check SharedPreferences cho nhanh)
            isFavorite = sharedPreferences.getBoolean(currentWord, false);
            updateFavoriteIcon();

            // Setup sự kiện click Tim
            setupFavoriteClick(currentWord);

            // --- LOGIC GỌI API ---
            if (wordEntry.getMeanings() == null || wordEntry.getMeanings().isEmpty()) {
                // Nếu thiếu dữ liệu (vào từ Wishlist) -> Gọi API
                fetchWordDetails(currentWord);
            } else {
                // Nếu đủ dữ liệu (vào từ Search) -> Hiển thị luôn
                displayWordData(wordEntry);
            }
        }
    }

    private void setupFavoriteClick(String currentWord) {
        btnFavorite.setOnClickListener(v -> {
            if (userID == null) {
                Toast.makeText(DetailActivity.this, "Bạn cần đăng nhập để lưu từ!", Toast.LENGTH_SHORT).show();
                return;
            }

            isFavorite = !isFavorite; // Đảo trạng thái

            // 1. Lưu vào SharedPreferences (để lần sau vào app thấy ngay tim đỏ)
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (isFavorite) {
                editor.putBoolean(currentWord, true);
            } else {
                editor.remove(currentWord);
            }
            editor.apply();

            // 2. Cập nhật giao diện ngay lập tức
            updateFavoriteIcon();

            // 3. Lưu lên FIRESTORE (Quan trọng: Để Wishlist thấy được)
            if (isFavorite) {
                // Tạo dữ liệu trống hoặc có timestamp tùy ý
                Map<String, Object> data = new HashMap<>();
                data.put("timestamp", System.currentTimeMillis());

                db.collection("users").document(userID)
                        .collection("favorites").document(currentWord)
                        .set(data)
                        .addOnSuccessListener(aVoid -> Toast.makeText(DetailActivity.this, "Đã lưu vào Wishlist", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(DetailActivity.this, "Lỗi lưu mạng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                // Xóa khỏi Firestore
                db.collection("users").document(userID)
                        .collection("favorites").document(currentWord)
                        .delete()
                        .addOnSuccessListener(aVoid -> Toast.makeText(DetailActivity.this, "Đã xóa khỏi Wishlist", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void fetchWordDetails(String word) {
        // Toast.makeText(this, "Đang tải dữ liệu...", Toast.LENGTH_SHORT).show();
        dictionaryApi.getDefinition(word).enqueue(new Callback<List<WordEntry>>() {
            @Override
            public void onResponse(Call<List<WordEntry>> call, Response<List<WordEntry>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    displayWordData(response.body().get(0));
                } else {
                    tvContent.setText("Không tìm thấy thông tin chi tiết.");
                }
            }

            @Override
            public void onFailure(Call<List<WordEntry>> call, Throwable t) {
                tvContent.setText("Lỗi kết nối mạng.");
            }
        });
    }

    private void displayWordData(WordEntry wordEntry) {
        // Logic hiển thị cũ (Phonetic, Audio, Meaning)
        String textPhonetic = "";
        String audioUrl = "";

        if (wordEntry.getPhonetics() != null) {
            for (Phonetic p : wordEntry.getPhonetics()) {
                if (TextUtils.isEmpty(textPhonetic) && !TextUtils.isEmpty(p.getText())) {
                    textPhonetic = p.getText();
                }
                if (TextUtils.isEmpty(audioUrl) && !TextUtils.isEmpty(p.getAudio())) {
                    audioUrl = p.getAudio();
                }
            }
        }
        tvPhonetic.setText(TextUtils.isEmpty(textPhonetic) ? "" : textPhonetic);

        if (!TextUtils.isEmpty(audioUrl)) {
            btnPlayAudio.setVisibility(View.VISIBLE);
            if (audioUrl.startsWith("//")) {
                audioUrl = "https:" + audioUrl;
            }
            String finalAudioUrl = audioUrl;
            btnPlayAudio.setOnClickListener(v -> playAudio(finalAudioUrl));
        } else {
            btnPlayAudio.setVisibility(View.GONE);
        }

        StringBuilder content = new StringBuilder();
        if (wordEntry.getMeanings() != null) {
            for (Meaning meaning : wordEntry.getMeanings()) {
                content.append("★ ").append(meaning.getPartOfSpeech().toUpperCase()).append("\n");
                if (meaning.getDefinitions() != null) {
                    int count = 1;
                    for (Definition def : meaning.getDefinitions()) {
                        content.append("   ").append(count).append(". ").append(def.getDefinition()).append("\n");
                        if (!TextUtils.isEmpty(def.getExample())) {
                            content.append("      Ex: \"").append(def.getExample()).append("\"\n");
                        }
                        count++;
                    }
                }
                content.append("\n────────────────────\n\n");
            }
        }
        tvContent.setText(content.toString());
    }

    private void updateFavoriteIcon() {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_favorite_red);
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    private void playAudio(String url) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> mp.start());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}