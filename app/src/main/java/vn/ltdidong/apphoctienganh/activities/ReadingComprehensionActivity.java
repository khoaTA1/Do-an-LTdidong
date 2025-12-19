package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
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
    private TextView passage;
    private RecyclerView rvQuestions;
    private List<ReadingPassage> readingPassageQAlist;
    private List<Long> passagePassed = new ArrayList<>();
    private DBHelper sqlite;
    private ImageButton btnBack;
    private Button btnSubmit;
    private int score = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.reading_comprehension_mode);

        sqlite = new DBHelper(this);

        // làm sạch danh sách đề đã làm
        passagePassed.clear();

        // ánh xạ một số thành phần
        passage = findViewById(R.id.ReadingPassage);
        btnBack = findViewById(R.id.back_arrow);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnBack.setOnClickListener(v -> {
            finish();
        });

        // chọn ngẫu nhiên đoạn văn và câu hỏi tương ứng để hiển thị
        ReadingPassage chosenPassage = choosePassage();

        passage.setText(chosenPassage.getPassage());

        rvQuestions = findViewById(R.id.Questions);

        setupRecyclerView(rvQuestions, chosenPassage.getQAList());

        btnSubmit.setOnClickListener(v -> {
            scoring(chosenPassage);

            Intent intent = new Intent(ReadingComprehensionActivity.this, ReadingSkillResultActivity.class);
            intent.putExtra("score", score);
            intent.putExtra("total", 5);
            startActivity(intent);
            finish();
        });
    }

    // tính điểm
    private void scoring(ReadingPassage chosenPassage) {
        for (QuestionAnswer QA : chosenPassage.getQAList()) {
            int userAnswer = QA.getUserAnswer();
            int correctAnswer = QA.getCorrectAnswer();

            // nếu người dùng trả lời đúng => tăng điểm
            if (userAnswer == correctAnswer) score++;
        }

        // qua lượt mới, tăng đếm
        // currentRP++;
    }

    // Lấy random 1 passage
    private ReadingPassage choosePassage() {
        if (sqlite == null) sqlite = new DBHelper(this);

        // random bằng danh sách id có thể không liên tục
        List<Long> ids = sqlite.getAllPassageIds();
        Log.d(">>> RC Activity", "Số lượng passage hiện tại: " + ids.size());

        long randomId = ids.get(new Random().nextInt(ids.size()));
        Log.d(">>> RC Activity", "id được sinh nghẫu nhiên: " + randomId);

        ReadingPassage randomPassage = sqlite.getReadingPassageById(randomId);
        Log.d(">>> RC Activity", "Đã tìm được đoạn văn ngẫu nhiên: " + randomPassage.getPassage());

        return randomPassage;
    }

    // khởi tạo recycler view
    private void setupRecyclerView(RecyclerView recyclerView, List<QuestionAnswer> questionList) {
        ReadingQAAdapter adapter = new ReadingQAAdapter(questionList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

}
