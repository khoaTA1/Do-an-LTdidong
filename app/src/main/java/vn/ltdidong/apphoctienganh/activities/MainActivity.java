package vn.ltdidong.apphoctienganh.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log; // Thêm import này

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.api.DictionaryApi;
import vn.ltdidong.apphoctienganh.models.Meaning;
import vn.ltdidong.apphoctienganh.models.WordEntry;
import android.content.Intent;
import android.view.KeyEvent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText searchEditText;
    private DictionaryApi dictionaryApi;

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Khởi tạo Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.dictionaryapi.dev/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        dictionaryApi = retrofit.create(DictionaryApi.class);

        // 2. Ánh xạ EditText
        searchEditText = findViewById(R.id.searchEditText);
        bottomNav = findViewById(R.id.bottomNavigation);


        // 3. Bắt sự kiện khi người dùng nhấn nút Search trên bàn phím
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            // Chỉ bắt sự kiện khi phím được ẤN XUỐNG (ACTION_DOWN) để tránh bị double
            if (event != null && event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            // Kiểm tra xem có phải nút Search, Done hoặc Enter không
            boolean isSearch = (actionId == EditorInfo.IME_ACTION_SEARCH);
            boolean isDone = (actionId == EditorInfo.IME_ACTION_DONE);
            boolean isEnterKey = (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);

            if (isSearch || isDone || isEnterKey) {
                String word = searchEditText.getText().toString().trim();
                if (!word.isEmpty()) {
                    // Ẩn bàn phím đi cho gọn
                    // InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    // imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

                    Toast.makeText(MainActivity.this, "Đang tra từ: " + word, Toast.LENGTH_SHORT).show();
                    lookupWord(word);
                }
                return true; // Đánh dấu là đã xử lý xong, không cho chạy tiếp
            }
            return false;
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Xử lý khi bấm Home (Ví dụ: reload lại trang, hoặc không làm gì)
                return true;
            }
            else if (id == R.id.nav_writing) {
                // Xử lý khi bấm Writing -> Mở trang WritingActivity
                Intent intent = new Intent(MainActivity.this, WritingActivity.class);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_reading) {
                // Code mở trang Reading...
                return true;
            }
            else if (id == R.id.nav_listening) {
                // Code mở trang Listening...
                return true;
            }
            else if (id == R.id.nav_speaking) {
                // Code mở trang Speaking...
                return true;
            }
            else if (id == R.id.nav_profile) {
                // Code mở trang Profile...
                return true;
            }

            return false;
        });
    }

    private void lookupWord(String word) {
        Log.d(TAG, "lookupWord called for: " + word); // Thêm Log này
        dictionaryApi.getDefinition(word).enqueue(new Callback<List<WordEntry>>() {
            @Override
            public void onResponse(Call<List<WordEntry>> call, Response<List<WordEntry>> response) {
                Log.d(TAG, "onResponse: isSuccessful = " + response.isSuccessful()); // Thêm Log này
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // THÀNH CÔNG -> Chuyển trang
                    WordEntry result = response.body().get(0);
                    Log.d(TAG, "onResponse: Word found: " + result.getWord()); // Thêm Log này
                    // GỌI HÀM CHUYỂN TRANG Ở ĐÂY
                    showResultDialog(result);
                } else {
                    // CÓ MẠNG NHƯNG KHÔNG TÌM THẤY TỪ
                    Log.d(TAG, "onResponse: Word not found or empty response for: " + word + ", Code: " + response.code()); // Thêm Log này
                    Toast.makeText(MainActivity.this, "Không tìm thấy từ: " + word, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<WordEntry>> call, Throwable t) {
                // IN LỖI RA LOGCAT VÀ MÀN HÌNH ĐỂ SOI
                Log.e(TAG, "onFailure: API call failed!", t); // Thêm Log này
                t.printStackTrace(); // In lỗi chi tiết ra Logcat
                Toast.makeText(MainActivity.this, "LỖI: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Hàm hiển thị kết quả dạng hộp thoại đơn giản
    private void showResultDialog(WordEntry entry) {
        // Tạo Intent để chuyển sang DetailActivity
        Toast.makeText(MainActivity.this, "Tìm thấy: " + entry.getWord(), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);

        // Đóng gói dữ liệu gửi đi (Key là "WORD_DATA")
        intent.putExtra("WORD_DATA", entry);

        // Bắt đầu chuyển trang
        startActivity(intent);
    }
}