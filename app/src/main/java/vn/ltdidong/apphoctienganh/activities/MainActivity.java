package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.ArticleAdapter;
import vn.ltdidong.apphoctienganh.api.DictionaryApi;
import vn.ltdidong.apphoctienganh.api.NewsApi;
import vn.ltdidong.apphoctienganh.database.AppDatabase;
import vn.ltdidong.apphoctienganh.database.UserStreakDao;
import vn.ltdidong.apphoctienganh.functions.DBHelper;
import vn.ltdidong.apphoctienganh.functions.GCallBack;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;
import vn.ltdidong.apphoctienganh.models.Article;
import vn.ltdidong.apphoctienganh.models.NewsResponse;
import vn.ltdidong.apphoctienganh.models.Word;
import vn.ltdidong.apphoctienganh.models.WordEntry;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText searchEditText;
    private DictionaryApi dictionaryApi;
    private NewsApi newsApi;

    private RecyclerView newsRecyclerView;
    private ArticleAdapter newsAdapter;
    private List<Article> newsList;

    private TextView tvStreakCount;
    private TextView tvLongestStreak;

    BottomNavigationView bottomNav;

    private DBHelper sqlite;
    private TextView newWordSuggestion;

    private static final String[] INIT_WORDS = {
            "learn", "study", "practice", "skill", "language",
            "focus", "habit", "daily", "goal", "challenge",
            "progress", "result", "effort", "understand",
            "remember", "example", "meaning", "improve",
            "knowledge", "success"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize Retrofit for Dictionary
        Retrofit dictionaryRetrofit = new Retrofit.Builder()
                .baseUrl("https://api.dictionaryapi.dev/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        dictionaryApi = dictionaryRetrofit.create(DictionaryApi.class);

        // Initialize Retrofit for News
        Retrofit newsRetrofit = new Retrofit.Builder()
                .baseUrl("https://newsapi.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        newsApi = newsRetrofit.create(NewsApi.class);

        // 2. Bind Views
        searchEditText = findViewById(R.id.searchEditText);
        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_home);

        newsRecyclerView = findViewById(R.id.newsRecyclerView);

        // Initialize streak TextViews
        tvStreakCount = findViewById(R.id.tvStreakCount);
        tvLongestStreak = findViewById(R.id.tvLongestStreak);

        newWordSuggestion = findViewById(R.id.newWord);

        // Setup News RecyclerView
        newsList = new ArrayList<>();
        newsAdapter = new ArticleAdapter(this, newsList);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsRecyclerView.setAdapter(newsAdapter);

        // Load News
        loadEnglishNews();
        
        // Load user progress
        loadUserProgress();
        
        // Practice Now button click
        findViewById(R.id.btnPracticeNow).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SkillHomeActivity.class);
            startActivity(intent);
        });

        // Daily Challenge card click
        findViewById(R.id.cardDailyChallenge).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DailyChallengeActivity.class);
            startActivity(intent);
        });
        
        // Dashboard button click
        findViewById(R.id.btnDashboard).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LearningDashboardActivity.class);
            startActivity(intent);
        });
        
        // AI Tutor button click
        findViewById(R.id.cardAITutor).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AITutorActivity.class);
            startActivity(intent);
        });
        
        // Social Hub button click
        findViewById(R.id.cardSocialHub).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SocialHubActivity.class);
            startActivity(intent);
        });

        // 3. Search Event
        sqlite = new DBHelper(this);

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            boolean isSearch = (actionId == EditorInfo.IME_ACTION_SEARCH);
            boolean isDone = (actionId == EditorInfo.IME_ACTION_DONE);
            boolean isEnterKey = (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);

            if (isSearch || isDone || isEnterKey) {
                String word = searchEditText.getText().toString().trim();
                if (!word.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Đang tra từ: " + word, Toast.LENGTH_SHORT).show();
                    lookupWord(word);
                }
                return true;
            }
            return false;
        });

        getRandomSuggestion(word -> {
            if (word != null) newWordSuggestion.setText((String) word);
        });

        // xử lí khi người duùng bấm vào từ vựng gợi ý
        newWordSuggestion.setOnClickListener(v -> {
            String word = newWordSuggestion.getText().toString();

            if (!word.isBlank()) lookupWord(word);
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return  true;
            } else if (id == R.id.nav_skills) {
                Intent intent = new Intent(MainActivity.this, SkillHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_entertainment) {
                Intent intent = new Intent(MainActivity.this, EntertainmentActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_camera) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set selected item to Home when resuming main activity
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
        // Refresh user progress khi quay lại activity
        loadUserProgress();
    }

    /**
     * Load user streak từ database và hiển thị
     */
    private void loadUserProgress() {
        String userId = SharedPreferencesManager.getInstance(this).getUserId();
        if (userId == null) {
            // User chưa đăng nhập, hiển thị giá trị mặc định
            tvStreakCount.setText("0 day streak");
            tvLongestStreak.setText("Longest: 0 days");
            return;
        }

        AppDatabase db = AppDatabase.getDatabase(this);
        UserStreakDao streakDao = db.userStreakDao();
        
        // Load streak
        streakDao.getStreakByUser(userId).observe(this, streak -> {
            if (streak != null) {
                int currentStreak = streak.getCurrentStreak();
                int longestStreak = streak.getLongestStreak();
                
                String streakText = currentStreak + (currentStreak == 1 ? " day streak" : " days streak");
                String longestText = "Longest: " + longestStreak + (longestStreak == 1 ? " day" : " days");
                
                tvStreakCount.setText(streakText);
                tvLongestStreak.setText(longestText);
            } else {
                tvStreakCount.setText("0 day streak");
                tvLongestStreak.setText("Longest: 0 days");
            }
        });
    }

    private void loadEnglishNews() {
        Log.d(">>> Main Activity", "Bắt đầu load tin tức");
        // Replace with your actual API Key from NewsAPI.org
        String apiKey = "ce2c44c423d24ce7adf1b1990dc7ea20"; 
        // Changed query to target IELTS / English Learning news, sorted by published date
        newsApi.getEnglishNews("Learn English", "en", "publishedAt", 4, apiKey).enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    newsList.clear();
                    newsList.addAll(response.body().getArticles());
                    newsAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "News API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                Log.e(TAG, "News API Failed", t);
            }
        });
    }

    private void lookupWord(String word) {
        Log.d(TAG, "lookupWord called for: " + word);
        dictionaryApi.getDefinition(word).enqueue(new Callback<List<WordEntry>>() {
            @Override
            public void onResponse(Call<List<WordEntry>> call, Response<List<WordEntry>> response) {
                Log.d(TAG, "onResponse: isSuccessful = " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    WordEntry result = response.body().get(0);

                    // lưu lại lịch sử tìm kiếm
                    Log.d(">>> Main Activity", "Lưu lịch sử tra từ điển: " + result.getWord());
                    sqlite.saveSearchWord(result);

                    Log.d(TAG, "onResponse: Word found: " + result.getWord());
                    showResultDialog(result);
                } else {
                    Log.d(TAG, "onResponse: Word not found or empty response for: " + word + ", Code: " + response.code());
                    Toast.makeText(MainActivity.this, "Không tìm thấy từ: " + word, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<WordEntry>> call, Throwable t) {
                Log.e(TAG, "onFailure: API call failed!", t);
                t.printStackTrace();
                Toast.makeText(MainActivity.this, "LỖI: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showResultDialog(WordEntry entry) {
        Toast.makeText(MainActivity.this, "Tìm thấy: " + entry.getWord(), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("WORD_DATA", entry);
        startActivity(intent);
    }

    private void getRandomSuggestion(GCallBack cb) {
        new Thread(() -> {
            List<String> list = loadSuggestions();

            // nếu chưa có danh sách gợi ý nào thì load ngẫu nhiên từ api
            if (list.isEmpty()) {
                fetchRandomWordFromApi();
                return;
            }

            String result = list.get(new Random().nextInt(list.size()));

            runOnUiThread(() -> cb.returnResult(result));
        }).start();
    }

    private List<String> loadSuggestions() {

        // lấy danh sách synonym của 3 từ vựng gần nhất mà người dùng tìm kiếm
        List<Word> wordList = sqlite.getRecentWords(3);
        List<String> synList = new ArrayList<>();

        for (Word w : wordList) {
            synList.addAll(w.getSyn());
        }

        return synList;
    }

    private void fetchRandomWordFromApi() {
        Random random = new Random();
        String word = INIT_WORDS[random.nextInt(INIT_WORDS.length)];

        newWordSuggestion.setText(word);
    }
}
