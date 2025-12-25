package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.ReadingQAAdapter;
import vn.ltdidong.apphoctienganh.functions.DBHelper;
import vn.ltdidong.apphoctienganh.functions.FirestoreCallBack;
import vn.ltdidong.apphoctienganh.functions.LoadFromJSON;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;
import vn.ltdidong.apphoctienganh.models.ClozeTestQA;
import vn.ltdidong.apphoctienganh.models.QuestionAnswer;
import vn.ltdidong.apphoctienganh.models.ReadingPassage;
import vn.ltdidong.apphoctienganh.repositories.QuestionAnswerRepo;
import vn.ltdidong.apphoctienganh.repositories.ReadingPassageRepo;

public class ReadingComprehensionActivity extends AppCompatActivity {
    private TextView passage, process;
    private RecyclerView rvQuestions;
    private List<QuestionAnswer> QAlist = new ArrayList<>();
    private ReadingQAAdapter adapter;
    private List<Long> passagePassed = new ArrayList<>();
    private DBHelper sqlite;
    private ImageButton btnBack;
    private Button btnSubmit, btnNext;
    private int score = 0;
    private int totalScore = 0;
    private int currentRP = 1;
    private ReadingPassage currentRPObj;
    private SharedPreferences sharedPreferences;
    private long totalRP = 0;
    private List<String> passedIdRP;
    private SharedPreferencesManager sharedPreferencesManager;
    private FirebaseFirestore firestore;
    private int lvl = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceSt) {
        super.onCreate(savedInstanceSt);
        EdgeToEdge.enable(this);
        setContentView(R.layout.reading_comprehension_mode);

        sqlite = new DBHelper(this);

        sharedPreferences = getSharedPreferences("Reading_Skill_Param", MODE_PRIVATE);
        totalRP = sharedPreferences.getLong("totalPassedRP", 0);
        String temp = sharedPreferences.getString("passedIdRP", "");

        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        firestore = FirebaseFirestore.getInstance();

        if (temp.isBlank()) {
            passedIdRP = new ArrayList<>();
        } else {
            passedIdRP = new ArrayList<>(Arrays.asList(temp.split(",")));
        }


        // làm sạch danh sách đề đã làm
        passagePassed.clear();

        // ánh xạ một số thành phần
        passage = findViewById(R.id.ReadingPassage);
        process = findViewById(R.id.practice_process);
        btnBack = findViewById(R.id.back_arrow);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnNext = findViewById(R.id.btnNext);
        rvQuestions = findViewById(R.id.Questions);

        btnBack.setOnClickListener(v -> {
            finish();
        });

        userLvlLoad(cb -> {
            lvl = (int) cb;

            setupRecyclerView();

            mainLoad();

            btnNext.setOnClickListener(v -> {
                scoring(currentRPObj);

                mainLoad();
            });

            // tắt nút submit cho đến khi lướt qua hết tất cả các câu hỏi
            btnSubmit.setAlpha(0.3f);
            btnSubmit.setEnabled(false);

            btnSubmit.setOnClickListener(v -> {
                scoring(currentRPObj);

                Intent intent = new Intent(ReadingComprehensionActivity.this, ReadingSkillResultActivity.class);
                intent.putExtra("score", score);
                intent.putExtra("total", totalScore);

                // cập nhật thống kê cho user
                float calculatedScore = totalScore > 0 ? (float) score /totalScore * 10 : 5f;
                updateUserStatistics(calculatedScore);

                startActivity(intent);
                finish();
            });
        });
    }

    private void userLvlLoad(FirestoreCallBack cb) {
        String email = sharedPreferencesManager.getUserEmail();
        firestore.collection("users").whereEqualTo("email", email).get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        DocumentSnapshot doc = snap.getDocuments().get(0);
                        Long temp = doc.getLong("current_level");
                        if (temp != null) {
                            int userLvl = temp.intValue();
                            Log.d(">>> RC Trans Activity", "Level của người dùng: " + userLvl);

                            if (userLvl >= 2) cb.returnResult(1);
                        } else {
                            Log.e("!!! RC Trans Activity", "Trường level null -> level mặc định");
                            cb.returnResult(0);
                        }
                    } else {
                        Log.e("!!! RC Trans Activity", "Không tìm thấy người dùng -> level mặc định");
                        cb.returnResult(0);
                    }
                });

    }

    // thay đổi điểm đánh giá chung dựa trên kết quả của người dùng
    private void updateUserStatistics(float score) {
        String uid = sharedPreferencesManager.getUserId().toString();
        DocumentReference ref = firestore.collection("users").document(uid);

        firestore.runTransaction(trans -> {
            DocumentSnapshot DS = trans.get(ref);

            if (!DS.exists()) {
                Log.e("!!! RC Activity", "Không tìm thấy người dùng để update statistics");
                return null;
            }

            Long temp = DS.getLong("reading");
            int readingPoint = 500;
            if (temp != null) readingPoint = temp.intValue();

            readingPoint += (int) exchangePoint(score) * 100;

            // kiểm tra sau khi cộng/trừ nếu điểm đánh giá chung vượt quá thang đo 10 thì kiềm lại
            if (readingPoint <= 0) {
                readingPoint = 0;
            } else if (readingPoint >= 10) {
                readingPoint = 1000;
            }

            trans.update(ref, "reading", readingPoint);
            Log.d(">>> RC Activity", "Đã update thống kê cho người dùng có ID: " + uid + " (" + readingPoint + ")");
            return null;
        });
    }

    // chuyển đổi kết quả nhận được thành điểm đánh giá chung cho firestore
    private float exchangePoint(float score) {
        int temp = (int) score;
        float returnPoint = (temp - 5) * 0.1f;
        Log.d("DEBUG", "Score -> Point: " + temp + " -> " + returnPoint);
        return returnPoint;
    }

    // load đoạn văn và câu hỏi
    private void mainLoad() {
        // clear nội dung đoạn văn
        passage.setText("");

        currentRPObj = choosePassage();

        // cập nhật lại nội dung đoạn văn và danh sách câu hỏi
        passage.setText(currentRPObj.getPassage());
        updateRecyclerView(currentRPObj.getQAList());

        process.setText(String.valueOf(currentRP));

        if (currentRP == 2) {
            btnNext.setEnabled(false);
            btnNext.setAlpha(0.3f);
        }
    }

    // tính điểm
    private void scoring(ReadingPassage chosenPassage) {
        for (QuestionAnswer QA : chosenPassage.getQAList()) {
            int userAnswer = QA.getUserAnswer();
            int correctAnswer = QA.getCorrectAnswer();

            // nếu người dùng trả lời đúng => tăng điểm
            if (userAnswer == correctAnswer) score++;
            totalScore++;
        }

        // qua lượt mới, tăng đếm
        currentRP++;

        if (currentRP == 2) {
            btnSubmit.setAlpha(1f);
            btnSubmit.setEnabled(true);
        }
    }

    // Lấy random 1 passage
    private ReadingPassage choosePassage() {
        if (sqlite == null) sqlite = new DBHelper(this);

        // random bằng danh sách id có thể không liên tục
        List<Long> ids = sqlite.getAllPassageIds(lvl);
        Log.d(">>> RC Activity", "Số lượng passage hiện tại: " + ids.size());

        Log.d(">>> RC Activity", "Passed Id RP: " + passedIdRP);

        List<Long> available = new ArrayList<>();
        for (Long id : ids) {
            if (!passedIdRP.contains(String.valueOf(id))) {
                available.add(id);
            }
        }

        if (available.isEmpty()) {
            passedIdRP.clear();
            available.addAll(ids);
        }

        long randomId = available.get(new Random().nextInt(available.size()));

        Log.d(">>> RC Activity", "id được sinh nghẫu nhiên: " + randomId);
        passedIdRP.add(String.valueOf(randomId));
        sharedPreferences.edit().putString("passedIdRP", TextUtils.join(",", passedIdRP)).apply();
        Log.d(">>> RC Activity", "Passed Id RP new: " + passedIdRP);
        totalRP++;
        sharedPreferences.edit().putLong("totalPassedRP", totalRP).apply();

        ReadingPassage randomPassage = sqlite.getReadingPassageById(randomId, lvl);
        Log.d(">>> RC Activity", "Đã tìm được đoạn văn ngẫu nhiên: " + randomPassage.getPassage());

        return randomPassage;
    }

    // khởi tạo và update recycler view
    private void setupRecyclerView() {
        adapter = new ReadingQAAdapter(QAlist);
        rvQuestions.setAdapter(adapter);
        rvQuestions.setLayoutManager(new LinearLayoutManager(this));
    }

    private void updateRecyclerView(List<QuestionAnswer> newQAList) {
        QAlist.clear();
        QAlist.addAll(newQAList);

        adapter.notifyDataSetChanged();
    }
}
