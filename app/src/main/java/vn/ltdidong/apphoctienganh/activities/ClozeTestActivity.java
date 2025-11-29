package vn.ltdidong.apphoctienganh.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import vn.ltdidong.apphoctienganh.R;

public class ClozeTestActivity extends AppCompatActivity {
    private TextView question1, question2, question3, question4, btnBack;
    private EditText answer1, answer2, answer3, answer4;

    @Override
    protected void onCreate(Bundle saveIns) {
        super.onCreate(saveIns);
        setContentView(R.layout.cloze_test_mode);

        // ánh xạ các thành phn view
        question1 = findViewById(R.id.cloze_question_1);
        question2 = findViewById(R.id.cloze_question_2);
        question3 = findViewById(R.id.cloze_question_3);
        question4 = findViewById(R.id.cloze_question_4);

        answer1 = findViewById(R.id.cloze_answer_1);
        answer2 = findViewById(R.id.cloze_answer_2);
        answer3 = findViewById(R.id.cloze_answer_3);
        answer4 = findViewById(R.id.cloze_answer_4);

        btnBack = findViewById(R.id.back_arrow);

        btnBack.setOnClickListener(v -> {
            finish();
        });


    }
}
