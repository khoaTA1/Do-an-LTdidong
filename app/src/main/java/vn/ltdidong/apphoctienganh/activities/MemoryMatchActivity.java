package vn.ltdidong.apphoctienganh.activities;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.MemoryMatchCardAdapter;
import vn.ltdidong.apphoctienganh.models.MMCard;

public class MemoryMatchActivity extends AppCompatActivity {
    private RecyclerView rvCards;
    private MemoryMatchCardAdapter adapter;
    private List<MMCard> cards = new ArrayList<>();

    private TextView tvScore, tvTimer;
    private Button btnRestart;

    // Logic
    private MMCard firstCard = null;
    private int firstPos = -1;
    private boolean isProcessing = false;
    private int score = 0;

    // Timer
    private Handler timerHandler = new Handler();
    private int seconds = 0;
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            seconds++;
            tvTimer.setText("Thời gian: " + formatTime(seconds));
            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int mode = getIntent().getIntExtra("mode", 1);

        if (mode == 1) {
            int diff = getIntent().getIntExtra("difficulty", 0);
            // 0 = dễ, 1 = vừa, 2 = khó
        } else {
            int first = getIntent().getIntExtra("firstPlayer", 1);
            // xử lý lượt đi đầu tiên

            singlePLayerMode();
        }

    }

    private void singlePLayerMode() {
        setContentView(R.layout.activity_memory_match);

        rvCards = findViewById(R.id.rvCards);
        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);
        btnRestart = findViewById(R.id.btnRestart);

        rvCards.setLayoutManager(new GridLayoutManager(this, 4)); // 4 cột

        initGame();
        startTimer();

        btnRestart.setOnClickListener(v -> restartGame());
    }

    private void twoPlayerMode() {

    }

    // ---------------------- GAME INIT ------------------------------
    private void initGame() {
        cards.clear();

        // Danh sách từ vựng mẫu (bạn có thể load từ DB hoặc API)
        List<String[]> vocab = Arrays.asList(
                new String[]{"apple", "quả táo"},
                new String[]{"dog", "con chó"},
                new String[]{"book", "quyển sách"},
                new String[]{"car", "xe hơi"},
                new String[]{"house", "ngôi nhà"}
        );

        int idCounter = 0;

        // Tạo card EN - VI
        for (String[] pair : vocab) {
            cards.add(new MMCard(idCounter++, pair[0], true));  // english
            cards.add(new MMCard(idCounter++, pair[1], false)); // vietnamese
        }

        // Shuffle
        Collections.shuffle(cards);

        adapter = new MemoryMatchCardAdapter(cards, (card, position) -> {
            onCardClicked(card, position);
        });

        rvCards.setAdapter(adapter);
        score = 0;
        tvScore.setText("Điểm: 0");
    }

    // -------------------- HANDLE CARD CLICK ------------------------
    private void onCardClicked(MMCard card, int position) {
        if (isProcessing) return;

        card.setFaceUp(true);
        adapter.notifyItemChanged(position);

        if (firstCard == null) {
            // Lật thẻ đầu tiên
            firstCard = card;
            firstPos = position;
        } else {
            // Lật thẻ thứ hai → kiểm tra ghép
            isProcessing = true;

            if (isMatch(firstCard, card)) {
                // Ghép đúng
                card.setMatched(true);
                firstCard.setMatched(true);

                score++;
                tvScore.setText("Điểm: " + score);

                firstCard = null;
                isProcessing = false;

            } else {
                // Sai → đợi 800ms rồi úp lại
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    card.setFaceUp(false);
                    firstCard.setFaceUp(false);

                    adapter.notifyItemChanged(position);
                    adapter.notifyItemChanged(firstPos);

                    firstCard = null;
                    isProcessing = false;
                }, 800);
            }
        }
    }

    // -------------------- CHECK MATCH -----------------------------
    private boolean isMatch(MMCard c1, MMCard c2) {
        if (c1.isEnglish() == c2.isEnglish()) return false;

        // Ghép EN - VI
        return c1.getText().equalsIgnoreCase(c2.getText()) ||
                isEnglishVietnamPair(c1.getText(), c2.getText());
    }

    // So sánh EN-VI theo dữ liệu mẫu của bạn
    private boolean isEnglishVietnamPair(String a, String b) {
        return (a.equals("apple") && b.equals("quả táo")) ||
                (a.equals("quả táo") && b.equals("apple")) ||
                (a.equals("dog") && b.equals("con chó")) ||
                (a.equals("con chó") && b.equals("dog")) ||
                (a.equals("book") && b.equals("quyển sách")) ||
                (a.equals("quyển sách") && b.equals("book")) ||
                (a.equals("car") && b.equals("xe hơi")) ||
                (a.equals("xe hơi") && b.equals("car")) ||
                (a.equals("house") && b.equals("ngôi nhà")) ||
                (a.equals("ngôi nhà") && b.equals("house"));
    }

    // ---------------- TIMER ----------------
    private void startTimer() {
        seconds = 0;
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private String formatTime(int sec) {
        int min = sec / 60;
        int s = sec % 60;
        return String.format("%02d:%02d", min, s);
    }

    // ---------------- RESTART GAME ----------------
    private void restartGame() {
        timerHandler.removeCallbacks(timerRunnable);
        startTimer();
        initGame();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }
}
