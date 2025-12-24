package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.noties.markwon.Markwon;
import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;

public class ReadingArticleActivity extends AppCompatActivity {

    private TextView tvContent;
    private Markwon markwon;
    private Button btnReadMore;
    private NestedScrollView nestedScrollView;

    // XP Reward Variables
    private long startTime;
    private boolean hasScrolledToEnd = false;
    private boolean isXpRewarded = false;
    private static final long MIN_READING_TIME_MILLIS = 15 * 1000; // 1 minute
    private static final int READING_XP_REWARD = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_article);

        startTime = System.currentTimeMillis();

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageView imgArticle = findViewById(R.id.imgArticleDetail);
        TextView tvTitle = findViewById(R.id.tvArticleTitleDetail);
        tvContent = findViewById(R.id.tvArticleContentDetail);
        TextView tvAuthor = findViewById(R.id.tvAuthorDate);
        btnReadMore = findViewById(R.id.btnReadMore);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        
        markwon = Markwon.create(this);

        // Get data from Intent
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content"); 
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String description = getIntent().getStringExtra("description");
        String author = getIntent().getStringExtra("author");
        String date = getIntent().getStringExtra("date");
        String url = getIntent().getStringExtra("url"); 

        // Set initial data
        tvTitle.setText(title);
        
        String metaInfo = "";
        if (author != null) metaInfo += "By " + author;
        if (date != null && date.length() >= 10) metaInfo += " • " + date.substring(0, 10); 
        tvAuthor.setText(metaInfo);

        Glide.with(this)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(imgArticle);

        btnBack.setOnClickListener(v -> finish());

        btnReadMore.setOnClickListener(v -> {
            if (url != null && !url.isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });

        // Setup Scroll Listener for XP
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (v.getChildAt(0).getBottom() <= (v.getHeight() + v.getScrollY())) {
                // Reached bottom
                hasScrolledToEnd = true;
                checkAndRewardXP();
            }
        });

        // Start fetching full content
        if (url != null && !url.isEmpty()) {
            tvContent.setText(""); 
            new FetchContentTask().execute(url);
        } else {
            String fullContent = "";
            if (description != null) fullContent += "**" + description + "**\n\n";
            if (content != null) {
                 String cleanContent = content.replaceAll("\\[\\+\\d+ chars\\]", "");
                 fullContent += cleanContent;
            }
            markwon.setMarkdown(tvContent, fullContent);
            btnReadMore.setVisibility(View.VISIBLE); 
        }
    }

    private void checkAndRewardXP() {
        if (isXpRewarded) return;

        long currentTime = System.currentTimeMillis();
        long duration = currentTime - startTime;

        if (hasScrolledToEnd && duration >= MIN_READING_TIME_MILLIS) {
            rewardXP();
        }
    }

    private void rewardXP() {
        String userId = SharedPreferencesManager.getInstance(this).getUserId();
        if (userId == null) return;

        isXpRewarded = true; // Mark as rewarded to prevent double reward

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long currentTotalXP = documentSnapshot.getLong("total_xp");
                        Long currentLevel = documentSnapshot.getLong("current_level");
                        Long currentLevelXP = documentSnapshot.getLong("current_level_xp");
                        Long xpToNextLevel = documentSnapshot.getLong("xp_to_next_level");

                        int newTotalXP = (currentTotalXP != null) ? currentTotalXP.intValue() : 0;
                        int newLevel = (currentLevel != null) ? currentLevel.intValue() : 1;
                        int newLevelXP = (currentLevelXP != null) ? currentLevelXP.intValue() : 0;
                        int newNextLevelXP = (xpToNextLevel != null) ? xpToNextLevel.intValue() : 100;

                        newTotalXP += READING_XP_REWARD;
                        newLevelXP += READING_XP_REWARD;

                        boolean leveledUp = false;
                        while (newLevelXP >= newNextLevelXP) {
                            leveledUp = true;
                            newLevelXP -= newNextLevelXP;
                            newLevel++;
                            newNextLevelXP = 100 + (newLevel - 1) * 50;
                        }

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("total_xp", newTotalXP);
                        updates.put("current_level", newLevel);
                        updates.put("current_level_xp", newLevelXP);
                        updates.put("xp_to_next_level", newNextLevelXP);

                        final int finalLevel = newLevel;
                        final boolean finalLeveledUp = leveledUp;

                        db.collection("users").document(userId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    String msg = "+" + READING_XP_REWARD + " XP for reading!";
                                    if (finalLeveledUp) {
                                        msg += "\n🎉 Level Up! You're now Level " + finalLevel + "!";
                                    }
                                    Toast.makeText(ReadingArticleActivity.this, msg, Toast.LENGTH_LONG).show();
                                });
                    }
                });
    }

    private class FetchContentTask extends AsyncTask<String, Void, String> {
        
        private final List<String> blockedPhrases = Arrays.asList(
            "sign up for a free account",
            "subscribe to read",
            "you have /5 articles left",
            "create an account",
            "log in",
            "cookie policy",
            "privacy policy",
            "all rights reserved",
            "by elizabeth redden", 
            "freshsplash" 
        );

        @Override
        protected String doInBackground(String... urls) {
            String url = urls[0];
            StringBuilder fullText = new StringBuilder();
            try {
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(10000)
                        .get();

                Elements paragraphs = doc.select("article p, .post-content p, .entry-content p, .story-body p, main p, #content p");
                
                if (paragraphs.isEmpty()) {
                     Elements allParagraphs = doc.select("p");
                     for (Element p : allParagraphs) {
                         if (p.text().length() > 50) {
                             paragraphs.add(p);
                         }
                     }
                }

                for (Element p : paragraphs) {
                    String text = p.text();
                    if (isValidContent(text)) {
                        fullText.append(text).append("\n\n");
                    }
                }

            } catch (IOException e) {
                return null;
            }
            return fullText.toString().trim(); 
        }

        private boolean isValidContent(String text) {
            if (text == null || text.trim().isEmpty()) return false;
            if (text.length() < 20 && !text.endsWith(".")) return false; 
            
            String lowerText = text.toLowerCase();
            for (String blocked : blockedPhrases) {
                if (lowerText.contains(blocked)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null && !result.isEmpty()) {
                markwon.setMarkdown(tvContent, result);
                btnReadMore.setVisibility(View.GONE);
            } else {
                String currentText = tvContent.getText().toString();
                if (currentText.isEmpty()) { 
                     tvContent.setText("Could not load full content automatically.\nPlease use the button below to read on the web.");
                }
                btnReadMore.setVisibility(View.VISIBLE);
            }
        }
    }
}
