package vn.ltdidong.apphoctienganh.activities;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.noties.markwon.Markwon;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.api.GeminiApi;
import vn.ltdidong.apphoctienganh.models.GeminiRequest;
import vn.ltdidong.apphoctienganh.models.GeminiResponse;

public class InteractiveTranslationActivity extends AppCompatActivity {

    private TextView tvFullParagraph, tvVietnameseSentence, tvProgress;
    private EditText etUserTranslation;
    private Button btnCheck, btnNextSentence;
    private ImageButton btnBack;
    private ProgressBar progressBarInfo;

    private GeminiApi geminiApi;
    // Corrected API Key
    private static final String API_KEY = "AIzaSyA3D1pQIeGGpam1dcXWg6PfDaXW_e6rPwE";

    private List<String> sentences;
    private int currentSentenceIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interactive_translation);

        tvFullParagraph = findViewById(R.id.tvFullParagraph);
        tvVietnameseSentence = findViewById(R.id.tvVietnameseSentence);
        etUserTranslation = findViewById(R.id.etUserTranslation);
        btnCheck = findViewById(R.id.btnCheck);
        btnNextSentence = findViewById(R.id.btnNextSentence);
        btnBack = findViewById(R.id.btnBack);
        tvProgress = findViewById(R.id.tvProgress);
        progressBarInfo = findViewById(R.id.progressBarInfo);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        geminiApi = retrofit.create(GeminiApi.class);

        btnBack.setOnClickListener(v -> finish());
        btnCheck.setOnClickListener(v -> checkTranslation());
        btnNextSentence.setOnClickListener(v -> generateNewParagraph());

        generateNewParagraph();
    }

    private void generateNewParagraph() {
        // Show loading state in UI instead of Dialog
        tvFullParagraph.setText("Generating a new paragraph...");
        tvVietnameseSentence.setText("...");
        btnCheck.setEnabled(false);
        etUserTranslation.setEnabled(false);

        String prompt = "Give me a short Vietnamese paragraph (3-4 sentences) for English translation practice. Separate sentences with a period. No extra text.";
        geminiApi.generateContent(API_KEY, new GeminiRequest(prompt)).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                // Re-enable UI
                btnCheck.setEnabled(true);
                etUserTranslation.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    String paragraph = response.body().getOutputText().trim();
                    // Filter out empty strings from the split
                    sentences = Arrays.stream(paragraph.split("\\.\\s*"))
                                      .filter(s -> !s.trim().isEmpty())
                                      .collect(Collectors.toList());
                    currentSentenceIndex = 0;
                    displayCurrentSentence();
                    btnNextSentence.setVisibility(View.GONE);
                    btnCheck.setVisibility(View.VISIBLE);
                    etUserTranslation.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(InteractiveTranslationActivity.this, "Failed to get paragraph", Toast.LENGTH_SHORT).show();
                    tvFullParagraph.setText("Could not load paragraph. Tap 'New Paragraph' to try again.");
                    btnNextSentence.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                // Re-enable UI
                btnCheck.setEnabled(true);
                etUserTranslation.setEnabled(true);
                Toast.makeText(InteractiveTranslationActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                tvFullParagraph.setText("Network error. Tap 'New Paragraph' to try again.");
                btnNextSentence.setVisibility(View.VISIBLE);
            }
        });
    }

    private void displayCurrentSentence() {
        updateParagraphHighlighting();

        if (sentences != null && !sentences.isEmpty() && currentSentenceIndex < sentences.size()) {
            String sentence = sentences.get(currentSentenceIndex);
            tvVietnameseSentence.setText(sentence.trim() + ".");
            etUserTranslation.setText("");
            tvProgress.setText("Sentence " + (currentSentenceIndex + 1) + " of " + sentences.size());
            int progress = (int) (((float) (currentSentenceIndex) / sentences.size()) * 100);
            progressBarInfo.setProgress(progress);
        } else {
            // End of paragraph
            tvVietnameseSentence.setText("Excellent! You've completed the paragraph.");
            etUserTranslation.setVisibility(View.GONE);
            btnCheck.setVisibility(View.GONE);
            btnNextSentence.setVisibility(View.VISIBLE);
            progressBarInfo.setProgress(100);
        }
    }

    private void updateParagraphHighlighting() {
        if (sentences == null || sentences.isEmpty()) return;

        StringBuilder htmlText = new StringBuilder();
        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i).trim();
            if (i < currentSentenceIndex) {
                // Completed sentences are bold
                htmlText.append("<b>").append(sentence).append(".</b> ");
            } else {
                // Incomplete sentences are normal
                htmlText.append(sentence).append(". ");
            }
        }
        
        tvFullParagraph.setText(Html.fromHtml(htmlText.toString(), Html.FROM_HTML_MODE_LEGACY));
    }

    private void checkTranslation() {
        String originalSentence = tvVietnameseSentence.getText().toString();
        String userTranslation = etUserTranslation.getText().toString().trim();

        if (userTranslation.isEmpty()) {
            Toast.makeText(this, "Please provide a translation.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state in UI
        btnCheck.setEnabled(false);
        btnCheck.setText("Checking...");

        String prompt = "As an English teacher, check this translation. Be strict but fair.\n" +
                "Original (Vietnamese): \"" + originalSentence + "\"\n" +
                "Student's translation (English): \"" + userTranslation + "\"\n" +
                "First, in one single word, is the student's translation 'Correct', 'Partially', or 'Incorrect'?'" +
                "Then, provide a better/alternative translation and a very short, simple explanation of any errors.";

        geminiApi.generateContent(API_KEY, new GeminiRequest(prompt)).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                // Restore button state
                btnCheck.setEnabled(true);
                btnCheck.setText("Check Answer");

                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().getOutputText();
                    if (result.toLowerCase().trim().startsWith("correct")) {
                        Toast.makeText(InteractiveTranslationActivity.this, "Correct! Moving to next sentence.", Toast.LENGTH_SHORT).show();
                        currentSentenceIndex++;
                        displayCurrentSentence();
                    } else {
                        showCorrectionDialog(result);
                    }
                } else {
                    Toast.makeText(InteractiveTranslationActivity.this, "Check failed.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                // Restore button state
                btnCheck.setEnabled(true);
                btnCheck.setText("Check Answer");
                Toast.makeText(InteractiveTranslationActivity.this, "Network error.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCorrectionDialog(String markdown) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_result_sheet, null);
        bottomSheetDialog.setContentView(view);

        TextView tvContent = view.findViewById(R.id.tvMarkdownContent);
        Button btnTryAgain = view.findViewById(R.id.btnKeepWriting);
        Button btnNext = view.findViewById(R.id.btnNewTopicSheet);

        btnTryAgain.setText("Let me try again");
        btnNext.setText("Show me the next sentence");
        btnNext.setVisibility(View.VISIBLE);

        Markwon markwon = Markwon.create(InteractiveTranslationActivity.this);
        markwon.setMarkdown(tvContent, markdown);

        btnTryAgain.setOnClickListener(v -> bottomSheetDialog.dismiss());
        btnNext.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            currentSentenceIndex++;
            displayCurrentSentence();
        });

        bottomSheetDialog.show();
    }
}
