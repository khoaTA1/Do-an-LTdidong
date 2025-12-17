package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.functions.DBHelper;
import vn.ltdidong.apphoctienganh.models.ClozeTestQA;

public class ClozeTestActivity extends AppCompatActivity {
    private TextView question1, question2, question3, question4, btnBack, process;
    private EditText answer1, answer2, answer3, answer4;
    private Button btnNext, btnSubmit;

    private static class QAView {
        TextView question;
        EditText answer;

        QAView(TextView q, EditText a) {
            this.question = q;
            this.answer = a;
        }
    }

    private List<QAView> QAlist;

    private List<ClozeTestQA> dataQA;
    private List<Long> qaPassed;
    private DBHelper sqlite;
    private int score = 0;
    private int currentQA = 1;

    @Override
    protected void onCreate(Bundle saveIns) {
        super.onCreate(saveIns);
        setContentView(R.layout.cloze_test_mode);

        sqlite = new DBHelper(this);
        qaPassed = new ArrayList<>();

        // ánh xạ các thành phn view
        question1 = findViewById(R.id.cloze_question_1);
        question2 = findViewById(R.id.cloze_question_2);
        question3 = findViewById(R.id.cloze_question_3);
        question4 = findViewById(R.id.cloze_question_4);

        answer1 = findViewById(R.id.cloze_answer_1);
        answer2 = findViewById(R.id.cloze_answer_2);
        answer3 = findViewById(R.id.cloze_answer_3);
        answer4 = findViewById(R.id.cloze_answer_4);

        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.back_arrow);
        btnSubmit = findViewById(R.id.btnSubmit);

        process = findViewById(R.id.practice_process);

        // thêm các view câu hỏi và câu trả lời vào danh sách để duyệt sau
        QAView qa1 = new QAView(question1, answer1);
        QAView qa2 = new QAView(question2, answer2);
        QAView qa3 = new QAView(question3, answer3);
        QAView qa4 = new QAView(question4, answer4);

        QAlist = new ArrayList<>();
        QAlist.add(qa1);
        QAlist.add(qa2);
        QAlist.add(qa3);
        QAlist.add(qa4);

        // setup mũi tên quay lại
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // setup câu hỏi và câu trả lời
        loadNextQA();
        process.setText(String.valueOf(currentQA));

        // setup các nút còn lại
        btnSubmit.setOnClickListener(v -> {
            scoring();

            Intent intent = new Intent(ClozeTestActivity.this, ReadingSkillResultActivity.class);
            intent.putExtra("score", score);
            intent.putExtra("total", 20);
            startActivity(intent);
            finish();
        });

        if (currentQA == 2) {
            btnNext.setAlpha(0.3f);
            btnNext.setEnabled(false);
        }

        btnNext.setOnClickListener(v -> {
            scoring();

            loadNextQA();
            process.setText(String.valueOf(currentQA));
        });
    }

    private void scoring() {
        for (int i = 0; i < 4; i++) {
            QAView view = QAlist.get(i);

            if (i >= dataQA.size()) break;

            ClozeTestQA data = dataQA.get(i);

            // lấy câu trả lời của người dùng với:
            // xóa khoảng trắng 2 đầu
            // chuyển hết các kí tự hoa thành thường
            // xóa khoảng trắng giữa các kí tự thành 1 chuỗi thống nhất
            // => để dễ so sánh với dữ liệu gốc
            String userAnswer = view.answer.getText().toString().trim().toLowerCase().strip();

            // lấy dữ liệu gốc: làm tương tự cho chắc
            String correctAnswer = data.getAnswer().trim().toLowerCase().strip();

            // nếu người dùng trả lời đúng => tăng điểm
            if (userAnswer.equals(correctAnswer)) score++;
        }

        // qua lượt mới, tăng đếm
        currentQA++;
    }

    private void loadNextQA() {
        if (dataQA == null) dataQA = new ArrayList<>();
        else dataQA.clear();

        new Thread(() -> {
            final List<ClozeTestQA> newQA = new ArrayList<>();

            for (int i = 0; i < 4; i++) {
                ClozeTestQA qa = randomQA2();
                if (qa != null) newQA.add(qa);
            }

            runOnUiThread(() -> {
                dataQA.clear();
                dataQA.addAll(newQA);

                for (int i = 0; i < 4; i++) {
                    QAView view = QAlist.get(i);
                    if (i >= newQA.size()) {
                        break;
                    } else {
                        view.question.setText(newQA.get(i).getQuestion());
                        view.answer.setText("");
                    }
                }
            });
        }).start();
    }

    private ClozeTestQA randomQA() {
         List<Long> ids = sqlite.getAllCLozeTestQAIds();
         ClozeTestQA qa;
         long randomId;

         // random cho đến khi id không nằm trong danh sách các QA đã làm qua
         do {
             randomId = ids.get(new Random().nextInt(ids.size()));
         } while (qaPassed.contains(randomId));

         // thêm vào danh sách QA đã làm
         qaPassed.add(randomId);

         qa = sqlite.getCTQAById(randomId);

         return qa;
    }

    private ClozeTestQA randomQA2() {
        List<Long> ids = sqlite.getAllCLozeTestQAIds();
        List<Long> remaining = new ArrayList<>();

        for (Long id : ids) {
            if (!qaPassed.contains(id)) remaining.add(id);
        }

        if (remaining.isEmpty()) return null;

        long selectedId = remaining.get(new Random().nextInt(remaining.size()));
        qaPassed.add(selectedId);

        return sqlite.getCTQAById(selectedId);
    }
}
