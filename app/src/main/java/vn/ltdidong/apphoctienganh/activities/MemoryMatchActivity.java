package vn.ltdidong.apphoctienganh.activities;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
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
    private int diff = 0;

    // two player mode
    private TextView scorePlr1, scorePlr2;
    private int[] scrs = {0, 0};
    private int first;
    private MaterialCardView plr1, plr2;

    // Colors for active/inactive player
    private static final int COLOR_ACTIVE = 0xFF4CAF50;   // Green
    private static final int COLOR_PLR1_INACTIVE = 0xFFFFEBEE; // Light pink
    private static final int COLOR_PLR2_INACTIVE = 0xFFE3F2FD; // Light blue

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
            diff = getIntent().getIntExtra("difficulty", 0);
            // 0 = dễ, 1 = vừa, 2 = khó

            singlePLayerMode();
        } else {
            first = getIntent().getIntExtra("firstPlayer", 0);
            // xử lý lượt đi đầu tiên

            twoPlayerMode();
        }

    }

    private void singlePLayerMode() {
        setContentView(R.layout.activity_memory_match);

        rvCards = findViewById(R.id.rvCards);
        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);
        btnRestart = findViewById(R.id.btnRestart);

        int columnDiff = 2;
        switch (diff) {
            case 0: // Dễ: 2x4 = 8 thẻ (4 cặp)
                columnDiff = 2;
                break;
            case 1: // Vừa: 3x4 = 12 thẻ (6 cặp)
                columnDiff = 3;
                break;
            case 2: // Khó: 4x4 = 16 thẻ (8 cặp)
                columnDiff = 4;
                break;
        }

        rvCards.setLayoutManager(new GridLayoutManager(this, columnDiff));

        initGame(1);
        startTimer();

        btnRestart.setOnClickListener(v -> restartGame(1));
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void twoPlayerMode() {
        setContentView(R.layout.activity_memory_match_2);

        scorePlr1 = findViewById(R.id.tvScore);
        scorePlr2 = findViewById(R.id.tvScore2);
        rvCards = findViewById(R.id.rvCards);
        btnRestart = findViewById(R.id.btnRestart);
        plr1 = findViewById(R.id.plr1);
        plr2 = findViewById(R.id.plr2);

        rvCards.setLayoutManager(new GridLayoutManager(this, 4));

        initGame(2);

        btnRestart.setOnClickListener(v -> restartGame(2));
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        if (first == 0) {
            plr1.setCardBackgroundColor(COLOR_ACTIVE);
            plr2.setCardBackgroundColor(COLOR_PLR2_INACTIVE);
        } else {
            plr1.setCardBackgroundColor(COLOR_PLR1_INACTIVE);
            plr2.setCardBackgroundColor(COLOR_ACTIVE);
        }
    }

    // ---------------------- GAME INIT ------------------------------
    private void initGame(int mode) {
        cards.clear();

        // Danh sách từ vựng mẫu (bạn có thể load từ DB hoặc API)
        List<String[]> allVocab = Arrays.asList(
                new String[]{"apple", "quả táo"},
                new String[]{"dog", "con chó"},
                new String[]{"book", "quyển sách"},
                new String[]{"car", "xe hơi"},
                new String[]{"house", "ngôi nhà"},
                new String[]{"cat", "con mèo"},
                new String[]{"water", "nước"},
                new String[]{"tree", "cây"}
        );

        // Số cặp theo độ khó
        int pairCount = 4; // Mặc định dễ
        if (mode == 2) {
            pairCount = 8; // 2 người chơi: 16 thẻ
        } else {
            switch (diff) {
                case 0: pairCount = 4; break;  // Dễ: 8 thẻ
                case 1: pairCount = 6; break;  // Vừa: 12 thẻ
                case 2: pairCount = 8; break;  // Khó: 16 thẻ
            }
        }

        List<String[]> vocab = allVocab.subList(0, pairCount);
        
        int idCounter = 0;

        // Tạo card EN - VI
        for (String[] pair : vocab) {
            cards.add(new MMCard(idCounter++, pair[0], true));  // english
            cards.add(new MMCard(idCounter++, pair[1], false)); // vietnamese
        }

        // Shuffle
        Collections.shuffle(cards);

        adapter = new MemoryMatchCardAdapter(cards, (card, position) -> {
            onCardClicked(card, position, mode);
        }, mode);

        rvCards.setAdapter(adapter);

        if (mode == 1) {
            score = 0;
            tvScore.setText("Điểm: 0");
        } else {
            scrs[0] = 0;
            scrs[1] = 0;
            scorePlr1.setText("+0");
            scorePlr2.setText("+0");
        }
    }

    // -------------------- HANDLE CARD CLICK ------------------------
    private void onCardClicked(MMCard card, int position, int mode) {
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

                // tính điểm trong chế độ 1 người
                if (mode == 1) {
                    score++;
                    tvScore.setText("Điểm: " + score);
                } else {
                    // tính điểm trong chế độ 2 người
                    scrs[first]++;

                    // render score
                    if (first == 1) {
                        scorePlr2.setText("+" + scrs[first]);
                    } else {
                        scorePlr1.setText("+" + scrs[first]);
                    }
                }

                firstCard = null;
                isProcessing = false;

                // Check if game is over
                if (isGameOver()) {
                    Handler handler = new Handler();
                    handler.postDelayed(() -> showGameOverDialog(mode), 500);
                }

                swapTurn();
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

                    // nếu trong chế độ 2 người thì mới có swap turn
                    if (mode == 2) swapTurn();
                }, 800);
            }

        }
    }

    private void swapTurn() {
        if (plr1 == null || plr2 == null) return; // Safety check for single player mode
        
        if (first == 1) {
            first = 0;
            plr1.setCardBackgroundColor(COLOR_ACTIVE);
            plr2.setCardBackgroundColor(COLOR_PLR2_INACTIVE);
        } else {
            first = 1;
            plr1.setCardBackgroundColor(COLOR_PLR1_INACTIVE);
            plr2.setCardBackgroundColor(COLOR_ACTIVE);
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

    // ---------------- CHECK GAME OVER ----------------
    private boolean isGameOver() {
        for (MMCard card : cards) {
            if (!card.isMatched()) {
                return false;
            }
        }
        return true;
    }

    private void showGameOverDialog(int mode) {
        timerHandler.removeCallbacks(timerRunnable);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🎉 Hoàn thành!");

        String message;
        if (mode == 1) {
            message = "Bạn đã hoàn thành trò chơi!\n\n" +
                    "⏱️ Thời gian: " + formatTime(seconds) + "\n" +
                    "🏆 Điểm số: " + score + " cặp";
        } else {
            String winner;
            if (scrs[0] > scrs[1]) {
                winner = "Player 1 thắng! 🥇";
            } else if (scrs[1] > scrs[0]) {
                winner = "Player 2 thắng! 🥇";
            } else {
                winner = "Hòa! 🤝";
            }
            message = winner + "\n\n" +
                    "👤 Player 1: " + scrs[0] + " cặp\n" +
                    "👤 Player 2: " + scrs[1] + " cặp";
        }

        builder.setMessage(message);
        builder.setPositiveButton("Chơi lại", (dialog, which) -> {
            restartGame(mode);
        });
        builder.setNegativeButton("Thoát", (dialog, which) -> {
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }

    // ---------------- RESTART GAME ----------------
    private void restartGame(int mode) {
        timerHandler.removeCallbacks(timerRunnable);
        if (mode == 1) {
            startTimer();
        }
        initGame(mode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }
}
