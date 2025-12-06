package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.LessonAdapter;
import vn.ltdidong.apphoctienganh.models.ListeningLesson;
import vn.ltdidong.apphoctienganh.viewmodel.ListeningViewModel;

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
    private ProgressBar progressBar;

    // Stats card views
    private TextView tvTotalLessons;
    private TextView tvCompleted;
    private TextView tvAvgScore;

    private String currentFilter = "ALL"; // ALL, EASY, MEDIUM, HARD

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listening_list);

        // Initialize views
        initViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup filters
        setupFilters();

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ListeningViewModel.class);

        // Bắt đầu observe data
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

        // Stats card views
        tvTotalLessons = findViewById(R.id.tvTotalLessons);
        tvCompleted = findViewById(R.id.tvCompleted);
        tvAvgScore = findViewById(R.id.tvAvgScore);

        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Listening Practice");
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
            intent.putExtra("lesson_id", lesson.getId());
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
            } else if (checkedId == R.id.chipEasy) {
                currentFilter = "EASY";
            } else if (checkedId == R.id.chipMedium) {
                currentFilter = "MEDIUM";
            } else if (checkedId == R.id.chipHard) {
                currentFilter = "HARD";
            }
            loadLessons();
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
            if (progressList != null) {
                adapter.setProgress(progressList);
            }
        });

        // Update stats card
        updateStats();
    }

    /**
     * Load danh sách bài học theo filter
     */
    private void loadLessons() {
        if (viewModel == null) {
            // ViewModel chưa được khởi tạo
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Remove previous observers to avoid multiple triggers
        viewModel.getAllLessons().removeObservers(this);
        viewModel.getLessonsByDifficulty(currentFilter).removeObservers(this);

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

    /**
     * Cập nhật stats card với thống kê từ Firebase
     */
    private void updateStats() {
        // Observe all lessons để tính tổng
        viewModel.getAllLessons().observe(this, lessons -> {
            if (lessons != null) {
                // Tổng số bài học
                int totalLessons = lessons.size();
                tvTotalLessons.setText(String.valueOf(totalLessons));

                // Observe progress để tính completed và average score
                viewModel.getAllProgress().observe(this, progressList -> {
                    if (progressList != null) {
                        int completedCount = 0;
                        double totalScore = 0;
                        int scoredLessons = 0;

                        // Đếm số bài hoàn thành và tính điểm trung bình
                        for (vn.ltdidong.apphoctienganh.models.UserProgress progress : progressList) {
                            if (progress.getStatus() != null && progress.getStatus().equals("COMPLETED")) {
                                completedCount++;
                            }
                            if (progress.getBestScore() > 0) {
                                totalScore += progress.getBestScore();
                                scoredLessons++;
                            }
                        }

                        // Cập nhật UI
                        tvCompleted.setText(String.valueOf(completedCount));

                        // Tính điểm trung bình
                        if (scoredLessons > 0) {
                            int avgScore = (int) (totalScore / scoredLessons);
                            tvAvgScore.setText(avgScore + "%");
                        } else {
                            tvAvgScore.setText("0%");
                        }
                    } else {
                        // Không có progress data
                        tvCompleted.setText("0");
                        tvAvgScore.setText("0%");
                    }
                });
            } else {
                // Không có lessons
                tvTotalLessons.setText("0");
                tvCompleted.setText("0");
                tvAvgScore.setText("0%");
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle back button
        finish();
        return true;
    }
}
