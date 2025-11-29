package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.functions.DBHelper;
import vn.ltdidong.apphoctienganh.functions.FirestoreCallBack;
import vn.ltdidong.apphoctienganh.models.QuestionAnswer;
import vn.ltdidong.apphoctienganh.models.ReadingPassage;
import vn.ltdidong.apphoctienganh.repositories.QuestionAnswerRepo;
import vn.ltdidong.apphoctienganh.repositories.ReadingPassageRepo;

public class RCTransActivity extends AppCompatActivity {
    private ReadingPassageRepo rprepo;
    private QuestionAnswerRepo qarepo;
    private int LIMIT_LOAD = 50;
    private DBHelper sqlite;
    private ProgressBar progressBar;
    private MaterialButton btnBack, btnStart;
    private TextView textLoading, textTitle, textExplain;

    @Override
    protected void onCreate(Bundle savedins) {
        super.onCreate(savedins);
        setContentView(R.layout.reading_mode_trans);

        // khởi tạo các repo và sqlite helper
        rprepo = new ReadingPassageRepo();
        qarepo = new QuestionAnswerRepo();
        sqlite = new DBHelper(this);

        // ánh xạ các thành phần layout
        progressBar = findViewById(R.id.progressLoading);
        btnBack = findViewById(R.id.btn_back);
        btnStart = findViewById(R.id.btn_start);
        textLoading = findViewById(R.id.txtLoading);
        textTitle = findViewById(R.id.txtTitle);
        textExplain = findViewById(R.id.txtExplanation);

        textTitle.setText(R.string.reading_comprehencion_title);
        textExplain.setText(R.string.reading_comprehension_desc);

        btnBack.setOnClickListener(v -> {
            finish();
        });

        // khi dữ liệu chưa load hoặc chưa load xong
        // nút start được làm mờ và không thể bấm
        btnStart.setAlpha(0.3f);
        btnStart.setEnabled(false);

        loadFromFirestore(cb -> {
            if (cb.toString().equals("ok")) {
                // thanh load được ẩn đi
                progressBar.setVisibility(View.GONE);
                textLoading.setVisibility(View.GONE);

                // nút start quay lại bình thường sau khi đợi hàm load dữ liệu xong
                btnStart.animate().alpha(1f).setDuration(300).start();
                btnStart.setEnabled(true);

                btnStart.setOnClickListener(v -> {
                    Intent intent = new Intent(RCTransActivity.this, ReadingComprehensionActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
        });

    }

    // hàm lấy dữ liệu từ firestore và lưu vào sqlite làm cache
    private void loadFromFirestore(FirestoreCallBack callback) {
        // lấy một số lượng đoạn văn từ firestore
        rprepo.getReadingPassagePagination(LIMIT_LOAD, list -> {
            if (list != null) {
                int totalPassages = ((List<ReadingPassage>) list).size();
                List<ReadingPassage> RPlist = (List<ReadingPassage>) list;

                // CountDownLatch đếm số passage
                CountDownLatch latch = new CountDownLatch(totalPassages);

                for (ReadingPassage rp : RPlist) {

                    // lấy các câu hỏi thuộc đoạn văn này bằng passage id
                    qarepo.getQuestionAnswerByPassageId(rp.getId(), qa_list -> {
                        if (qa_list != null) {
                            List<QuestionAnswer> QAlist = (List<QuestionAnswer>) qa_list;

                            // set danh sách câu hỏi - câu trả lời cho đoạn văn hiện tại
                            rp.setQAList(QAlist);
                        } else rp.setQAList(new ArrayList<>());

                        latch.countDown();
                    });
                }

                // kiểm tra xem danh sách đoạn văn và các câu hỏi cùng câu trả lời đã load đầy đủ chưa
                new Thread(() -> {
                    try {
                        // chờ hết countDown
                        latch.await();
                        runOnUiThread(() -> {
                            if (sqlite == null) sqlite = new DBHelper(this);
                            if (sqlite.insertRPList(RPlist) == 1) {
                                Log.d(">>> SQLite", "Thêm danh sách vào cache thành công");
                            } else {
                                Log.e("!!! SQLite", "Thêm danh sách vào cache KHÔNG thành công");
                            }

                            callback.returnResult("ok");
                        });
                    } catch (InterruptedException e) {
                        Log.e("!!! CountDown", "Lỗi: ", e);
                    }
                }).start();
            } else {
                Log.e("!!! Firestore", "Danh sách đoạn văn bị null");
            }
        });
    }
}
