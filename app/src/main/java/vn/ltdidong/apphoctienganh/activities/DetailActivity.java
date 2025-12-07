package vn.ltdidong.apphoctienganh.activities;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;
import vn.ltdidong.apphoctienganh.models.User;
import vn.ltdidong.apphoctienganh.models.WordEntry;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.ltdidong.apphoctienganh.api.DictionaryApi;

public class DetailActivity extends AppCompatActivity {

    private TextView tvWord, tvPhonetic;
    private LinearLayout layoutMeanings;
    private ImageButton btnBack;
    private ImageButton btnAudio, btnFavorite;
    private WordEntry wordEntry;
    private MediaPlayer mediaPlayer;
    private FirebaseFirestore db;
    private String userID;
    private boolean isFavorite = false;
    private DictionaryApi dictionaryApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Ánh xạ View (Bây giờ XML đã có ID này nên sẽ không lỗi nữa)
        tvWord = findViewById(R.id.tv_detail_word);
        tvPhonetic = findViewById(R.id.tv_detail_phonetic);
        layoutMeanings = findViewById(R.id.layout_meanings);
        btnBack = findViewById(R.id.btn_back_detail);
        btnAudio = findViewById(R.id.btn_detail_audio);
        btnFavorite = findViewById(R.id.btn_detail_favorite);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnAudio != null) {
            btnAudio.setOnClickListener(v -> {
                if (wordEntry != null) {
                    String audioUrl = wordEntry.getAudioUrl();
                    playAudio(audioUrl);
                }
            });
        }

        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> {
                if (userID == null) {
                    Toast.makeText(this, "Vui lòng đăng nhập để thêm yêu thích", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (wordEntry == null || wordEntry.getWord() == null)
                    return;

                if (isFavorite) {
                    db.collection("users").document(userID)
                            .collection("favorites").document(wordEntry.getWord())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                isFavorite = false;
                                updateFavoriteIcon();
                                Toast.makeText(this, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(
                                    e -> Toast.makeText(this, "Lỗi khi bỏ yêu thích", Toast.LENGTH_SHORT).show());
                } else {
                    db.collection("users").document(userID)
                            .collection("favorites").document(wordEntry.getWord())
                            .set(wordEntry)
                            .addOnSuccessListener(aVoid -> {
                                isFavorite = true;
                                updateFavoriteIcon();
                                Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(
                                    e -> Toast.makeText(this, "Thêm thất bại", Toast.LENGTH_SHORT).show());
                }
            });
        }

        db = FirebaseFirestore.getInstance();
        User user = SharedPreferencesManager.getInstance(this).getUser();
        if (user != null)
            userID = String.valueOf(user.getId());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.dictionaryapi.dev/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        dictionaryApi = retrofit.create(DictionaryApi.class);

        wordEntry = (WordEntry) getIntent().getSerializableExtra("WORD_DATA");

        if (wordEntry != null) {
            if (userID != null)
                checkFavoriteInitial();
            ensureFullDataThenDisplay();
        } else {
            Toast.makeText(this, "Không có dữ liệu", Toast.LENGTH_SHORT).show();
        }
    }

    private void ensureFullDataThenDisplay() {
        boolean missing = (wordEntry.getMeanings() == null || wordEntry.getMeanings().isEmpty());
        if (!missing) {
            displayData();
            return;
        }
        dictionaryApi.getDefinition(wordEntry.getWord()).enqueue(new Callback<List<WordEntry>>() {
            @Override
            public void onResponse(Call<List<WordEntry>> call, Response<List<WordEntry>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    wordEntry = response.body().get(0);
                    displayData();
                } else {
                    displayData();
                }
            }

            @Override
            public void onFailure(Call<List<WordEntry>> call, Throwable t) {
                displayData();
            }
        });
    }

    private void checkFavoriteInitial() {
        db.collection("users").document(userID)
                .collection("favorites").document(wordEntry.getWord())
                .get()
                .addOnSuccessListener(doc -> {
                    isFavorite = doc != null && doc.exists();
                    updateFavoriteIcon();
                });
    }

    private void updateFavoriteIcon() {
        if (btnFavorite == null)
            return;
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_favorite_red);
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    private void displayData() {
        tvWord.setText(wordEntry.getWord());

        // Xử lý Phonetic
        String phoneticText = "";
        if (wordEntry.getPhonetics() != null) {
            for (WordEntry.Phonetic p : wordEntry.getPhonetics()) {
                if (p.getText() != null && !p.getText().isEmpty()) {
                    phoneticText = p.getText();
                    break;
                }
            }
        }
        if (phoneticText.isEmpty() && wordEntry.getPhonetic() != null) {
            phoneticText = wordEntry.getPhonetic();
        }
        tvPhonetic.setText(phoneticText);

        // Xử lý Meaning
        if (layoutMeanings != null) {
            layoutMeanings.removeAllViews();
            if (wordEntry.getMeanings() != null) {
                for (WordEntry.Meaning m : wordEntry.getMeanings()) {
                    // Loại từ
                    TextView tvPart = new TextView(this);
                    tvPart.setText("- " + m.getPartOfSpeech());
                    tvPart.setTextSize(18);
                    tvPart.setTypeface(null, android.graphics.Typeface.BOLD);
                    tvPart.setPadding(0, 16, 0, 8);
                    layoutMeanings.addView(tvPart);

                    // Định nghĩa
                    if (m.getDefinitions() != null) {
                        for (WordEntry.Definition d : m.getDefinitions()) {
                            TextView tvDef = new TextView(this);
                            tvDef.setText(" • " + d.getDefinition());
                            tvDef.setTextSize(16);
                            tvDef.setPadding(16, 0, 0, 8);
                            layoutMeanings.addView(tvDef);
                        }
                    }
                }
            }
        }
    }

    private void playAudio(String audioUrl) {
        stopAudio();
        if (audioUrl == null || audioUrl.isEmpty()) {
            Toast.makeText(this, "Không có file âm thanh", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA).build());
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
        } catch (Exception e) {
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception e) {
            }
            mediaPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAudio();
    }
}
