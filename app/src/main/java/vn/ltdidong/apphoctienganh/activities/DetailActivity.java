package vn.ltdidong.apphoctienganh.activities;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.Definition;
import vn.ltdidong.apphoctienganh.models.Meaning;
import vn.ltdidong.apphoctienganh.models.Phonetic;
import vn.ltdidong.apphoctienganh.models.WordEntry;

public class DetailActivity extends AppCompatActivity {

    // Khai báo MediaPlayer để phát âm thanh
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 1. Ánh xạ View
        TextView tvWord = findViewById(R.id.tvDetailWord);
        TextView tvPhonetic = findViewById(R.id.tvDetailPhonetic);
        TextView tvContent = findViewById(R.id.tvDetailContent);
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnPlayAudio = findViewById(R.id.btnPlayAudio);

        // Sự kiện nút Back
        btnBack.setOnClickListener(v -> finish());

        // 2. Nhận dữ liệu từ màn hình trước
        WordEntry wordEntry = (WordEntry) getIntent().getSerializableExtra("WORD_DATA");

        if (wordEntry != null) {
            // Hiển thị từ vựng
            tvWord.setText(wordEntry.getWord());

            // --- XỬ LÝ LOGIC LẤY AUDIO VÀ PHIÊN ÂM ---
            String textPhonetic = "";
            String audioUrl = "";

            // Duyệt qua danh sách phonetics để tìm dữ liệu tốt nhất
            if (wordEntry.getPhonetics() != null) {
                for (Phonetic p : wordEntry.getPhonetics()) {
                    // Ưu tiên lấy text phiên âm nếu chưa có
                    if (TextUtils.isEmpty(textPhonetic) && !TextUtils.isEmpty(p.getText())) {
                        textPhonetic = p.getText();
                    }
                    // Ưu tiên lấy link audio nếu chưa có và link không rỗng
                    if (TextUtils.isEmpty(audioUrl) && !TextUtils.isEmpty(p.getAudio())) {
                        audioUrl = p.getAudio();
                    }
                }
            }

            // Hiển thị phiên âm
            tvPhonetic.setText(TextUtils.isEmpty(textPhonetic) ? "" : textPhonetic);

            // Cấu hình nút Play Audio
            if (!TextUtils.isEmpty(audioUrl)) {
                btnPlayAudio.setVisibility(View.VISIBLE);

                // Fix lỗi link API thiếu "https:" (thường trả về "//ssl.gstatic...")
                if (audioUrl.startsWith("//")) {
                    audioUrl = "https:" + audioUrl;
                }

                // Gán sự kiện click
                String finalAudioUrl = audioUrl;
                btnPlayAudio.setOnClickListener(v -> playAudio(finalAudioUrl));
            } else {
                btnPlayAudio.setVisibility(View.GONE);
            }
            // ------------------------------------------

            // --- XỬ LÝ HIỂN THỊ NGHĨA ---
            StringBuilder content = new StringBuilder();
            if (wordEntry.getMeanings() != null) {
                for (Meaning meaning : wordEntry.getMeanings()) {
                    // Loại từ (Noun/Verb)
                    content.append("★ ").append(meaning.getPartOfSpeech().toUpperCase()).append("\n");

                    // Danh sách định nghĩa
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
    }

    // Hàm phát âm thanh từ URL
    private void playAudio(String url) {
        // Nếu đang phát cái cũ thì tắt đi
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        try {
            mediaPlayer = new MediaPlayer();
            // Cấu hình cho Android stream nhạc
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());

            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync(); // Chuẩn bị bất đồng bộ (tránh đơ app)

            mediaPlayer.setOnPreparedListener(mp -> {
                Toast.makeText(DetailActivity.this, "Playing...", Toast.LENGTH_SHORT).show();
                mp.start();
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(DetailActivity.this, "Lỗi phát âm thanh!", Toast.LENGTH_SHORT).show();
                return false;
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Không thể tải file âm thanh", Toast.LENGTH_SHORT).show();
        }
    }

    // Giải phóng bộ nhớ khi thoát màn hình
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}