package vn.ltdidong.apphoctienganh.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.api.DictionaryApi;
import vn.ltdidong.apphoctienganh.models.WordEntry;

public class StoryDetailActivity extends AppCompatActivity {
    
    private static final String TAG = "StoryDetailActivity";
    
    private ImageView btnBack;
    private TextView tvStoryTitle;
    private TextView tvChapterTitle;
    private TextView tvStoryContent;
    private ProgressBar progressBar;
    private TextView tvProgress;
    private LinearLayout choicesLayout;
    
    private String storyId;
    private String storyTitle;
    private int currentChapter = 1;
    private int totalChapters = 5;
    
    private DictionaryApi dictionaryApi;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_detail);
        
        storyId = getIntent().getStringExtra("story_id");
        storyTitle = getIntent().getStringExtra("story_title");
        
        initializeViews();
        loadChapter(currentChapter);
    }
    
    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvStoryTitle = findViewById(R.id.tvStoryTitle);
        tvChapterTitle = findViewById(R.id.tvChapterTitle);
        tvStoryContent = findViewById(R.id.tvStoryContent);
        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);
        choicesLayout = findViewById(R.id.choicesLayout);
        
        tvStoryTitle.setText(storyTitle);
        btnBack.setOnClickListener(v -> finish());
        
        progressBar.setMax(totalChapters);
        updateProgress();
        
        // Initialize Dictionary API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.dictionaryapi.dev/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        dictionaryApi = retrofit.create(DictionaryApi.class);
        
        // Enable text selection with custom action mode
        tvStoryContent.setTextIsSelectable(true);
        tvStoryContent.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                menu.clear();
                menu.add(0, 1, 0, "📖 Lookup");
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == 1) {
                    int start = tvStoryContent.getSelectionStart();
                    int end = tvStoryContent.getSelectionEnd();
                    String selectedText = tvStoryContent.getText().toString().substring(start, end).trim();
                    lookupWord(selectedText);
                    mode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });
    }
    
    private void loadChapter(int chapter) {
        tvChapterTitle.setText("Chapter " + chapter);
        
        // Load story content
        String content = getSampleContent(chapter);
        tvStoryContent.setText(content);
        
        // Load choices for this chapter
        loadChoices(chapter);
        
        updateProgress();
    }
    
    private String getSampleContent(int chapter) {
        switch (chapter) {
            case 1:
                return "Emma stood at the ancient map, her fingers tracing the mysterious symbols. " +
                       "The treasure was hidden somewhere in the forbidden forest. She felt both excited " +
                       "and nervous about the adventure ahead.\n\n" +
                       "\"Are you ready?\" her companion asked.\n\n" +
                       "Emma took a deep breath and nodded.";
                       
            case 2:
                return "The forest was darker than Emma expected. Strange sounds echoed through the trees. " +
                       "She remembered her grandmother's warning about the ancient guardian that protected " +
                       "the treasure.\n\n" +
                       "Suddenly, a magnificent creature appeared before them...";
                       
            default:
                return "To be continued...";
        }
    }
    
    private void lookupWord(String word) {
        // Show loading
        Toast.makeText(this, "Looking up: " + word, Toast.LENGTH_SHORT).show();
        
        dictionaryApi.getDefinition(word).enqueue(new Callback<List<WordEntry>>() {
            @Override
            public void onResponse(Call<List<WordEntry>> call, Response<List<WordEntry>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    WordEntry wordEntry = response.body().get(0);
                    showWordDefinitionDialog(wordEntry);
                } else {
                    Toast.makeText(StoryDetailActivity.this, 
                        "Word not found: " + word, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<WordEntry>> call, Throwable t) {
                Log.e(TAG, "Dictionary API Error", t);
                Toast.makeText(StoryDetailActivity.this, 
                    "Error looking up word", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showWordDefinitionDialog(WordEntry wordEntry) {
        StringBuilder message = new StringBuilder();
        
        // Phonetic
        if (wordEntry.getPhonetic() != null && !wordEntry.getPhonetic().isEmpty()) {
            message.append("🔊 Pronunciation: ").append(wordEntry.getPhonetic()).append("\n\n");
        }
        
        // Meanings
        if (wordEntry.getMeanings() != null && !wordEntry.getMeanings().isEmpty()) {
            for (int i = 0; i < Math.min(2, wordEntry.getMeanings().size()); i++) {
                WordEntry.Meaning meaning = wordEntry.getMeanings().get(i);
                message.append("📚 ").append(meaning.getPartOfSpeech()).append(":\n");
                
                if (meaning.getDefinitions() != null && !meaning.getDefinitions().isEmpty()) {
                    for (int j = 0; j < Math.min(2, meaning.getDefinitions().size()); j++) {
                        WordEntry.Definition def = meaning.getDefinitions().get(j);
                        message.append("  • ").append(def.getDefinition()).append("\n");
                        
                        if (def.getExample() != null && !def.getExample().isEmpty()) {
                            message.append("    Example: ").append(def.getExample()).append("\n");
                        }
                    }
                }
                message.append("\n");
            }
        }
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("📖 " + wordEntry.getWord())
            .setMessage(message.toString())
            .setPositiveButton("Got it!", null)
            .setNeutralButton("Add to Wishlist", (dialog, which) -> {
                Toast.makeText(this, "Added '" + wordEntry.getWord() + "' to wishlist!", 
                    Toast.LENGTH_SHORT).show();
            })
            .show();
    }
    
    private void loadChoices(int chapter) {
        choicesLayout.removeAllViews();
        
        if (chapter == 1) {
            addChoice("Enter the forest carefully", 2);
            addChoice("Wait for more information", 1);
        } else if (chapter == 2) {
            addChoice("Approach the creature", 3);
            addChoice("Hide behind a tree", 3);
        } else {
            // Last chapter or continue
            Button btnContinue = new Button(this);
            btnContinue.setText("Complete Chapter ✓");
            btnContinue.setOnClickListener(v -> {
                if (currentChapter < totalChapters) {
                    currentChapter++;
                    loadChapter(currentChapter);
                } else {
                    showCompletionDialog();
                }
            });
            choicesLayout.addView(btnContinue);
        }
    }
    
    private void addChoice(String choiceText, int nextChapter) {
        Button btnChoice = new Button(this);
        btnChoice.setText(choiceText);
        btnChoice.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        btnChoice.setOnClickListener(v -> {
            currentChapter = nextChapter;
            loadChapter(currentChapter);
        });
        choicesLayout.addView(btnChoice);
    }
    
    private void updateProgress() {
        progressBar.setProgress(currentChapter);
        tvProgress.setText(currentChapter + "/" + totalChapters + " chapters");
    }
    
    private void showCompletionDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("🎉 Story Completed!")
            .setMessage("Congratulations! You've finished this story.\n\n" +
                       "New vocabulary learned: 7 words\n" +
                       "XP earned: +50")
            .setPositiveButton("Continue Learning", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }
}
