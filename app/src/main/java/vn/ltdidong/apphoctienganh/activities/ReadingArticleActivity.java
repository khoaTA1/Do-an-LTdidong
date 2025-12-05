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

import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import io.noties.markwon.Markwon;
import vn.ltdidong.apphoctienganh.R;

public class ReadingArticleActivity extends AppCompatActivity {

    private TextView tvContent;
    private ProgressBar progressBar;
    private Markwon markwon;
    private Button btnReadMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_article);

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageView imgArticle = findViewById(R.id.imgArticleDetail);
        TextView tvTitle = findViewById(R.id.tvArticleTitleDetail);
        tvContent = findViewById(R.id.tvArticleContentDetail);
        TextView tvAuthor = findViewById(R.id.tvAuthorDate);
        btnReadMore = findViewById(R.id.btnReadMore);
        
        markwon = Markwon.create(this);

        // Get data from Intent
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content"); // This is the short preview
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String description = getIntent().getStringExtra("description");
        String author = getIntent().getStringExtra("author");
        String date = getIntent().getStringExtra("date");
        String url = getIntent().getStringExtra("url"); 

        // Set initial data
        tvTitle.setText(title);
        
        String metaInfo = "";
        if (author != null) metaInfo += "By " + author;
        if (date != null) metaInfo += " • " + date.substring(0, 10); 
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

        // Hide button initially if we are attempting to fetch content
        btnReadMore.setVisibility(View.GONE);

        // Start fetching full content
        if (url != null && !url.isEmpty()) {
            // Don't set "Loading..." text initially to avoid flicker/space if it loads fast
            // Or use a dedicated ProgressBar instead of text in tvContent
            tvContent.setText(""); 
            new FetchContentTask().execute(url);
        } else {
            // Fallback to description + short content
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
            "by elizabeth redden", // Example of author line in body
            "freshsplash" // Image credits
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
                e.printStackTrace();
                return null;
            }
            return fullText.toString().trim(); // Trim result to remove leading/trailing newlines
        }

        private boolean isValidContent(String text) {
            if (text == null || text.trim().isEmpty()) return false;
            // Filter out very short lines that are likely captions or metadata
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
                if (currentText.isEmpty()) { // Only show error if nothing else is shown
                     tvContent.setText("Could not load full content automatically.\nPlease use the button below to read on the web.");
                }
                btnReadMore.setVisibility(View.VISIBLE);
            }
        }
    }
}
