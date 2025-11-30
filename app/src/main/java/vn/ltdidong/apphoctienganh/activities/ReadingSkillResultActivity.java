package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import vn.ltdidong.apphoctienganh.R;

public class ReadingSkillResultActivity extends AppCompatActivity {
    private TextView congratTitle, score, total;
    private MaterialButton btnBack;
    @Override
    protected void onCreate(Bundle saveIns) {
        super.onCreate(saveIns);
        setContentView(R.layout.reading_skill_all_mode_result);

        // ánh xạ các thành phần view
        congratTitle = findViewById(R.id.congrat_title);
        score = findViewById(R.id.score);
        total = findViewById(R.id.total);
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> {
            finish();
        });

        Intent intent = getIntent();
        int intentScore = intent.getIntExtra("score", -1);
        int intentTotal = intent.getIntExtra("total", -1);

        if (intentScore == -1 || intentTotal == -1) {
            congratTitle.setText("Error, please try again");
        } else if ((float) intentScore / intentTotal < 0.5) {
            congratTitle.setText("Keep going!\nEvery mistake is a step toward improvement");
            score.setText(String.valueOf(intentScore));
            total.setText("/" + intentTotal);
        } else {
            congratTitle.setText("Congratulation!\nThat's a good result");
            score.setText(String.valueOf(intentScore));
            total.setText("/" + intentTotal);
        }
    }
}
