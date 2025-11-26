package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.LessonAdapter;
import vn.ltdidong.apphoctienganh.models.ListeningLesson;
import vn.ltdidong.apphoctienganh.viewmodel.ListeningViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

/**
 * Activity hiển thị danh sách các bài học Listening
 * Cho phép lọc theo độ khó và xem tiến độ
 */
public class ListeningListActivity extends AppCompatActivity {
    
    private ListeningViewModel viewModel;
    private LessonAdapter adapter;
    private RecyclerView recyclerView;
    private ChipGroup chipGroupFilter;
    private View emptyStateLayout;
    private View progressBar;
    
    private String currentFilter = "ALL"; // ALL, EASY, MEDIUM, HARD
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listening_list);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ListeningViewModel.class);
        
        // Initialize views
        initViews();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup filters
        setupFilters();
        
        // Observe data
        observeData();
    }
    
    /**
     * Khởi tạo các views
     */
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewLessons);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        progressBar = findViewById(R.id.progressBar);
        
        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    /**
     * Setup RecyclerView với adapter và layout manager
     */
    private void setupRecyclerView() {
        // Khởi tạo adapter với click listener
        adapter = new LessonAdapter(lesson -> {
            // Khi click vào bài học, mở LessonDetailActivity
            Intent intent = new Intent(ListeningListActivity.this, LessonDetailActivity.class);
            intent.putExtra("LESSON_ID", lesson.getId());
            startActivity(intent);
        });
        
        // Set adapter và layout manager
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
    }
    
    /**
     * Setup filter chips
     */
    private void setupFilters() {
        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                currentFilter = "ALL";
                loadLessons();
            } else if (checkedId == R.id.chipEasy) {
                currentFilter = "EASY";
                loadLessons();
            } else if (checkedId == R.id.chipMedium) {
                currentFilter = "MEDIUM";
                loadLessons();
            } else if (checkedId == R.id.chipHard) {
                currentFilter = "HARD";
                loadLessons();
            }
        });
    }
    
    /**
     * Observe dữ liệu từ ViewModel
     */
    private void observeData() {
        // Load lessons theo filter hiện tại
        loadLessons();
        
        // Observe progress để hiển thị trên từng card
        viewModel.getAllProgress().observe(this, progressList -> {
            adapter.setProgress(progressList);
        });
    }
    
    /**
     * Load danh sách bài học theo filter
     */
    private void loadLessons() {
        progressBar.setVisibility(View.VISIBLE);
        
        if (currentFilter.equals("ALL")) {
            // Lấy tất cả bài học
            viewModel.getAllLessons().observe(this, lessons -> {
                progressBar.setVisibility(View.GONE);
                updateUI(lessons);
            });
        } else {
            // Lấy bài học theo độ khó
            viewModel.getLessonsByDifficulty(currentFilter).observe(this, lessons -> {
                progressBar.setVisibility(View.GONE);
                updateUI(lessons);
            });
        }
    }
    
    /**
     * Cập nhật UI sau khi có data
     */
    private void updateUI(java.util.List<ListeningLesson> lessons) {
        if (lessons == null || lessons.isEmpty()) {
            // Hiển thị empty state
            recyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            // Hiển thị danh sách
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            adapter.setLessons(lessons);
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        // Handle back button
        finish();
        return true;
    }
}
