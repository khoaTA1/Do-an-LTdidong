package vn.ltdidong.apphoctienganh.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.WordEntry;

public class FlashcardActivity extends AppCompatActivity {

    private List<WordEntry> flashcardList;
    private int currentIndex = 0;
    private boolean isFront = true;
    private MediaPlayer mediaPlayer;

    // UI Components
    private View cardFront, cardBack;
    private TextView tvWord, tvMeaning, tvIndex;
    private View cardContainer;
    private FloatingActionButton btnAudio;
    private ImageButton btnBack;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        initViews();
        getDataFromIntent();
    }

    private void initViews() {
        cardContainer = findViewById(R.id.card_container);
        cardFront = findViewById(R.id.card_front);
        cardBack = findViewById(R.id.card_back);
        tvWord = findViewById(R.id.tv_word);
        tvMeaning = findViewById(R.id.tv_meaning);
        tvIndex = findViewById(R.id.tv_index);

        btnAudio = findViewById(R.id.btn_audio);

        btnNext = findViewById(R.id.btn_next);
        btnBack = findViewById(R.id.btn_back_flashcard);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                stopAudio();
                finish();
            });
        }

        cardContainer.setOnClickListener(v -> flipCard());

        // Sự kiện click nút loa mặt trước
        btnAudio.setOnClickListener(v -> playCurrentAudio());

        // Chỉ 1 nút âm thanh (btn_audio)

        btnNext.setOnClickListener(v -> {
            if (flashcardList == null || flashcardList.isEmpty())
                return;
            stopAudio();

            int size = flashcardList.size();
            int newIndex = currentIndex;
            if (size > 1) {
                Random r = new Random();
                do {
                    newIndex = r.nextInt(size);
                } while (newIndex == currentIndex);
            }
            currentIndex = newIndex;

            if (!isFront)
                resetCardToFront();
            showCard(flashcardList.get(currentIndex));
        });
    }

    // Hàm chung để phát âm thanh (cho gọn code)
    private void playCurrentAudio() {
        if (flashcardList != null && !flashcardList.isEmpty()) {
            playAudio(flashcardList.get(currentIndex).getAudioUrl());
        }
    }

    private void getDataFromIntent() {
        ArrayList<WordEntry> receivedList = (ArrayList<WordEntry>) getIntent().getSerializableExtra("FLASHCARD_LIST");

        if (receivedList != null && !receivedList.isEmpty()) {
            flashcardList = receivedList;
            Collections.shuffle(flashcardList);
            currentIndex = 0;
            resetCardToFront();
            showCard(flashcardList.get(0));
        } else {
            Toast.makeText(this, "Không có dữ liệu từ vựng!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showCard(WordEntry word) {
        tvWord.setText(word.getWord());

        // 1. Lấy định nghĩa từ Model (Model đã xử lý logic lấy từ List rồi)
        String meaning = word.getFirstDefinition();

        // 2. LOGIC KIỂM TRA MỚI (Đồng bộ với API tiếng Anh)
        // Kiểm tra null, rỗng, hoặc thông báo lỗi từ Model
        if (meaning == null || meaning.isEmpty() || meaning.equals("No definition found.")) {

            // Nếu không có định nghĩa -> Hiển thị thông báo
            tvMeaning.setText("(Chưa có định nghĩa)");

            // Log để debug xem tại sao (Do list rỗng hay do lỗi parse)
            Log.e("Flashcard", "Word: " + word.getWord() + " - Dữ liệu nghĩa bị thiếu.");

        } else {
            // Nếu có dữ liệu -> Hiển thị bình thường
            tvMeaning.setText(meaning);
        }

        tvIndex.setText((currentIndex + 1) + "/" + flashcardList.size());
    }

    private void flipCard() {
        stopAudio();
        View visibleView = isFront ? cardFront : cardBack;
        View invisibleView = isFront ? cardBack : cardFront;

        ObjectAnimator flipOut = ObjectAnimator.ofFloat(visibleView, "rotationY", 0f, 90f);
        flipOut.setDuration(250);
        ObjectAnimator flipIn = ObjectAnimator.ofFloat(invisibleView, "rotationY", -90f, 0f);
        flipIn.setDuration(250);

        flipOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                visibleView.setVisibility(View.GONE);
                invisibleView.setVisibility(View.VISIBLE);
                flipIn.start();
                isFront = !isFront;
            }
        });
        flipOut.start();
    }

    private void resetCardToFront() {
        cardBack.setVisibility(View.GONE);
        cardFront.setVisibility(View.VISIBLE);
        cardFront.setRotationY(0f);
        cardBack.setRotationY(0f);
        isFront = true;
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
            e.printStackTrace();
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
