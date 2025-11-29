package vn.ltdidong.apphoctienganh.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.ReadingQAAdapter;
import vn.ltdidong.apphoctienganh.functions.DBHelper;
import vn.ltdidong.apphoctienganh.functions.FirestoreCallBack;
import vn.ltdidong.apphoctienganh.functions.LoadFromJSON;
import vn.ltdidong.apphoctienganh.models.QuestionAnswer;
import vn.ltdidong.apphoctienganh.models.ReadingPassage;
import vn.ltdidong.apphoctienganh.repositories.QuestionAnswerRepo;
import vn.ltdidong.apphoctienganh.repositories.ReadingPassageRepo;

public class ReadingComprehensionActivity extends AppCompatActivity {
    private TextView passage;
    private RecyclerView rvQuestions;
    private List<ReadingPassage> readingPassageQAlist;
    private List<Long> passagePassed = new ArrayList<>();
    private ReadingPassageRepo rprepo;
    private QuestionAnswerRepo qarepo;
    private int LIMIT_LOAD = 50;
    private DBHelper sqlite;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.reading_comprehension_mode);

        rprepo = new ReadingPassageRepo();
        qarepo = new QuestionAnswerRepo();
        sqlite = new DBHelper(this);

        // làm sạch danh sách đề đã làm
        passagePassed.clear();

        // ánh xạ một số thành phần
        passage = findViewById(R.id.ReadingPassage);

        // nạp dữ liệu từ firestore
        loadFromFirestore(cb -> {
            if (((String) cb).equals("ok")) Log.d(">>> RC Activity", "Đã nạp xong từ Firstore");

            // chọn ngẫu nhiên đoạn văn và câu hỏi tương ứng để hiển thị
            ReadingPassage chosenPassage = choosePassage();

            passage.setText(chosenPassage.getPassage());

            rvQuestions = findViewById(R.id.Questions);

            setupRecyclerView(rvQuestions, chosenPassage.getQAList());
        });
    }

    private void loadFromStorage() {
        readingPassageQAlist = LoadFromJSON.loadAllPassages(this);
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

    // Lấy random 1 passage
    private ReadingPassage choosePassage() {
        if (sqlite == null) sqlite = new DBHelper(this);

        // random bằng danh sách id có thể không liên tục
        List<Integer> ids = sqlite.getAllPassageIds();
        Log.d(">>> RC Activity", "Số lượng passage hiện tại: " + ids.size());

        int randomId = ids.get(new Random().nextInt(ids.size()));
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
