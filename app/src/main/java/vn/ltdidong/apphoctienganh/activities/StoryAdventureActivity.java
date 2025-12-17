package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.StoryAdapter;
import vn.ltdidong.apphoctienganh.models.Story;

public class StoryAdventureActivity extends AppCompatActivity {
    
    private ImageView btnBack;
    private ChipGroup chipGroupCategories;
    private RecyclerView rvStories;
    private StoryAdapter storyAdapter;
    private List<Story> storyList;
    private String selectedCategory = "All";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_adventure);
        
        initializeViews();
        setupCategories();
        loadStories();
        setupRecyclerView();
    }
    
    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        rvStories = findViewById(R.id.rvStories);
        
        btnBack.setOnClickListener(v -> finish());
    }
    
    private void setupCategories() {
        String[] categories = {"All", "Adventure", "Mystery", "Fantasy", "Romance", "Sci-Fi", "Comedy"};
        
        for (String category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setChecked(category.equals("All"));
            chip.setOnClickListener(v -> {
                selectedCategory = category;
                filterStories(category);
            });
            chipGroupCategories.addView(chip);
        }
    }
    
    private void loadStories() {
        storyList = new ArrayList<>();
        
        // Sample stories - Replace with Firebase data
        storyList.add(new Story("1", "The Lost Treasure", 
            "Join Emma on an exciting adventure to find hidden treasure", 
            "Adventure", "Beginner", "", 15, false, 5));
            
        storyList.add(new Story("2", "Mystery at Midnight Manor", 
            "Solve the mystery of the haunted mansion", 
            "Mystery", "Intermediate", "", 20, false, 7));
            
        storyList.add(new Story("3", "Dragon's Quest", 
            "A young knight's journey to save the kingdom", 
            "Fantasy", "Advanced", "", 25, false, 10));
            
        storyList.add(new Story("4", "Love in Paris", 
            "A romantic tale in the city of lights", 
            "Romance", "Intermediate", "", 18, true, 6));
            
        storyList.add(new Story("5", "Space Odyssey", 
            "Explore the mysteries of outer space", 
            "Sci-Fi", "Advanced", "", 30, true, 12));
            
        storyList.add(new Story("6", "The Clumsy Detective", 
            "A comedy about a detective who always gets lucky", 
            "Comedy", "Beginner", "", 12, false, 4));
    }
    
    private void setupRecyclerView() {
        storyAdapter = new StoryAdapter(this, storyList, story -> {
            if (story.isLocked()) {
                Toast.makeText(this, "🔒 Complete previous stories to unlock!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(StoryAdventureActivity.this, StoryDetailActivity.class);
                intent.putExtra("story_id", story.getId());
                intent.putExtra("story_title", story.getTitle());
                startActivity(intent);
            }
        });
        
        rvStories.setLayoutManager(new LinearLayoutManager(this));
        rvStories.setAdapter(storyAdapter);
    }
    
    private void filterStories(String category) {
        if (category.equals("All")) {
            storyAdapter.updateStories(storyList);
        } else {
            List<Story> filtered = new ArrayList<>();
            for (Story story : storyList) {
                if (story.getCategory().equals(category)) {
                    filtered.add(story);
                }
            }
            storyAdapter.updateStories(filtered);
        }
    }
}
