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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.reading_comprehension_mode);

        sqlite = new DBHelper(this);

        sharedPreferences = getSharedPreferences("Reading_Skill_Param", MODE_PRIVATE);
        totalRP = sharedPreferences.getLong("totalPassedRP", 0);
        String temp = sharedPreferences.getString("passedIdRP", "0");

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

        setupRecyclerView();

        mainLoad();

        btnNext.setOnClickListener(v -> {
            scoring(currentRPObj);

            mainLoad();
        });

        btnSubmit.setOnClickListener(v -> {
            scoring(currentRPObj);

            Intent intent = new Intent(ReadingComprehensionActivity.this, ReadingSkillResultActivity.class);
            intent.putExtra("score", score);
            intent.putExtra("total", totalScore);
            startActivity(intent);
            finish();
        });
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
    }

    // Lấy random 1 passage
    private ReadingPassage choosePassage() {
        if (sqlite == null) sqlite = new DBHelper(this);

        // random bằng danh sách id có thể không liên tục
        List<Long> ids = sqlite.getAllPassageIds();
        Log.d(">>> RC Activity", "Số lượng passage hiện tại: " + ids.size());

        long randomId = 0;
        do {
            randomId = ids.get(new Random().nextInt(ids.size()));
        } while (passedIdRP.contains(String.valueOf(randomId)));

        Log.d(">>> RC Activity", "id được sinh nghẫu nhiên: " + randomId);
        passedIdRP.add(String.valueOf(randomId));
        sharedPreferences.edit().putString("passedIdRP", TextUtils.join(",", passedIdRP));
        totalRP++;
        sharedPreferences.edit().putLong("totalPassedRP", totalRP);

        ReadingPassage randomPassage = sqlite.getReadingPassageById(randomId);
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
