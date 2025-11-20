package vn.ltdidong.apphoctienganh.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.functions.LoadFromJSON;
import vn.ltdidong.apphoctienganh.models.ReadingPassage;

public class ReadingComprehensionActivity extends AppCompatActivity {
    private TextView passage;

    private List<ReadingPassage> readingPassageQAlist;
    private List<Long> passagePassed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.reading_comprehension_mode);

        // nạp các json hiện có trong internal storage
        loadFromStorage();

        // làm sạch danh sách đề đã làm
        passagePassed.clear();

        // ánh xạ một số thành phần
        passage = findViewById(R.id.ReadingPassage);

        // chọn ngẫu nhiên đoạn văn và câu hỏi tương ứng để hiển thị
        ReadingPassage chosenPassage = choosePassage();

        passage.setText(chosenPassage.getPassage());
    }

    private void loadFromStorage() {
        readingPassageQAlist = LoadFromJSON.loadAllPassages(this);
    }

    // Lấy random 1 passage
    private ReadingPassage choosePassage() {
        ReadingPassage randomPassage = new ReadingPassage();
        while (true) {
            if (!readingPassageQAlist.isEmpty()) {
                int index = (int) (Math.random() * readingPassageQAlist.size());
                randomPassage = readingPassageQAlist.get(index);

                // nếu đoạn văn chưa được làm thì trả về
                // nếu không thì tiếp tục vòng lặp
                long passageId = randomPassage.getId();
                if (!passagePassed.contains(passageId)) {
                    passagePassed.add(passageId);
                    break;
                }
            }
        }

        return randomPassage;
    }
}
