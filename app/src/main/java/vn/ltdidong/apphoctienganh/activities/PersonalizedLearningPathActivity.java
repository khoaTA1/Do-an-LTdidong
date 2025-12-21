package vn.ltdidong.apphoctienganh.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.LearningPathAdapter;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;
import vn.ltdidong.apphoctienganh.managers.AdaptiveLearningPathManager;
import vn.ltdidong.apphoctienganh.models.LearningPathStep;

/**
 * Personalized Learning Path Activity
 * - AI tạo lộ trình học cá nhân hóa
 * - Điều chỉnh độ khó tự động
 * - Theo dõi tiến độ
 */
public class PersonalizedLearningPathActivity extends AppCompatActivity {
    
    private ImageButton btnBack;
    private RecyclerView recyclerView;
    private LearningPathAdapter adapter;
    private List<LearningPathStep> pathSteps;
    private ProgressBar progressBar;
    private TextView tvNoPath, tvPathSummary;
    private Button btnGeneratePath;
    private View setupLayout;
    private EditText etDays, etDailyMinutes, etGoal;
    
    private AdaptiveLearningPathManager pathManager;
    private String userId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personalized_learning_path);
        
        initViews();
        setupManagers();
        setupRecyclerView();
        setupListeners();
        loadExistingPath();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerViewPath);
        progressBar = findViewById(R.id.progressBar);
        tvNoPath = findViewById(R.id.tvNoPath);
        tvPathSummary = findViewById(R.id.tvPathSummary);
        btnGeneratePath = findViewById(R.id.btnGeneratePath);
        setupLayout = findViewById(R.id.setupLayout);
        etDays = findViewById(R.id.etDays);
        etDailyMinutes = findViewById(R.id.etDailyMinutes);
        etGoal = findViewById(R.id.etGoal);
        
        pathSteps = new ArrayList<>();
    }
    
    private void setupManagers() {
        userId = SharedPreferencesManager.getInstance(this).getUserId();
        pathManager = new AdaptiveLearningPathManager(this);
    }
    
    private void setupRecyclerView() {
        adapter = new LearningPathAdapter(this, pathSteps);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        adapter.setOnStepClickListener(step -> {
            // Navigate to appropriate skill activity
            Toast.makeText(this, "Starting: " + step.getTitle(), Toast.LENGTH_SHORT).show();
            // TODO: Open skill activity based on step.getSkillType()
        });
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnGeneratePath.setOnClickListener(v -> generateNewPath());
    }
    
    private void loadExistingPath() {
        progressBar.setVisibility(View.VISIBLE);
        
        pathManager.loadLearningPath(userId, 
            new AdaptiveLearningPathManager.PathLoadCallback() {
                @Override
                public void onPathLoaded(List<LearningPathStep> steps) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        
                        if (steps.isEmpty()) {
                            showSetupView();
                        } else {
                            showPathView(steps);
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        showSetupView();
                    });
                }
            });
    }
    
    private void generateNewPath() {
        String daysStr = etDays.getText().toString().trim();
        String minutesStr = etDailyMinutes.getText().toString().trim();
        String goal = etGoal.getText().toString().trim();
        
        if (daysStr.isEmpty() || minutesStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int days = Integer.parseInt(daysStr);
        int minutes = Integer.parseInt(minutesStr);
        
        if (goal.isEmpty()) {
            goal = "Improve overall English skills";
        }
        
        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        setupLayout.setVisibility(View.GONE);
        
        pathManager.generateAILearningPath(userId, days, minutes, goal,
            new AdaptiveLearningPathManager.PathGenerationCallback() {
                @Override
                public void onPathGenerated(List<LearningPathStep> steps) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        showPathView(steps);
                        Toast.makeText(PersonalizedLearningPathActivity.this,
                            "Learning path generated!", Toast.LENGTH_SHORT).show();
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        setupLayout.setVisibility(View.VISIBLE);
                        Toast.makeText(PersonalizedLearningPathActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
    }
    
    private void showSetupView() {
        setupLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvNoPath.setVisibility(View.VISIBLE);
        tvPathSummary.setVisibility(View.GONE);
    }
    
    private void showPathView(List<LearningPathStep> steps) {
        pathSteps.clear();
        pathSteps.addAll(steps);
        adapter.notifyDataSetChanged();
        
        setupLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        tvNoPath.setVisibility(View.GONE);
        tvPathSummary.setVisibility(View.VISIBLE);
        
        // Update summary
        int completed = 0;
        for (LearningPathStep step : steps) {
            if (step.isCompleted()) completed++;
        }
        tvPathSummary.setText(String.format("Progress: %d/%d steps completed", 
            completed, steps.size()));
    }
}
