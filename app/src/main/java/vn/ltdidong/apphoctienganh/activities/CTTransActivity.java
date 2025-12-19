package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.functions.DBHelper;
import vn.ltdidong.apphoctienganh.functions.FirestoreCallBack;
import vn.ltdidong.apphoctienganh.models.ClozeTestQA;
import vn.ltdidong.apphoctienganh.models.ReadingPassage;
import vn.ltdidong.apphoctienganh.repositories.ClozeTestQARepo;

public class CTTransActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private ImageButton btnBack;
    private MaterialButton btnStart;
    private TextView textTitle, textExplain, textLoading;
    private ClozeTestQARepo ctqarepo;
    private DBHelper sqlite;
    private static final int LIMIT_LOAD = 30;

    @Override
    protected void onCreate(Bundle saveIns) {
        super.onCreate(saveIns);
        setContentView(R.layout.reading_mode_trans);

        // ánh xạ các thành phần layout
        progressBar = findViewById(R.id.progressLoading);
        btnBack = findViewById(R.id.btn_back);
        btnStart = findViewById(R.id.btn_start);
        textLoading = findViewById(R.id.txtLoading);
        textTitle = findViewById(R.id.txtTitle);
        textExplain = findViewById(R.id.txtExplanation);

        textTitle.setText(R.string.cloze_test_title);
        textExplain.setText(R.string.cloze_test_desc);

        ctqarepo = new ClozeTestQARepo();
        sqlite = new DBHelper(this);

        btnBack.setOnClickListener(v -> {
            finish();
        });

        // khi dữ liệu chưa load hoặc chưa load xong
        // nút start được làm mờ và không thể bấm
        btnStart.setAlpha(0.3f);
        btnStart.setEnabled(false);

        loadFromFirestore(cb -> {
            if (cb.toString().equals("ok")) {
                progressBar.setVisibility(View.GONE);
                textLoading.setVisibility(View.GONE);

                // nút start quay lại bình thường sau khi đợi hàm load dữ liệu xong
                btnStart.animate().alpha(1f).setDuration(300).start();
                btnStart.setEnabled(true);

                btnStart.setOnClickListener(v -> {
                    Intent intent = new Intent(CTTransActivity.this, ClozeTestActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }

    // hàm lấy dữ liệu từ firestore và lưu vào sqlite làm cache
    private void loadFromFirestore(FirestoreCallBack callback) {
        // lấy một số lượng đoạn văn từ firestore
        ctqarepo.getClozeTestQAList(LIMIT_LOAD, list -> {
            if (list != null) {
                List<ClozeTestQA> QAlist = (List<ClozeTestQA>) list;

                if (sqlite == null) sqlite = new DBHelper(this);
                if (sqlite.insertCTQAList(QAlist) == 1) {
                    Log.d(">>> SQLite", "Thêm danh sách vào cache thành công");
                } else {
                    Log.e("!!! SQLite", "Thêm danh sách vào cache KHÔNG thành công");
                }

                callback.returnResult("ok");
            } else {
                Log.e("!!! Firestore", "Danh sách cloze test QA bị null");
            }
        });
    }
}
