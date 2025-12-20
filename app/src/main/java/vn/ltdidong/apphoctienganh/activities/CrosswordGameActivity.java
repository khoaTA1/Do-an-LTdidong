package vn.ltdidong.apphoctienganh.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.BuildConfig;
import vn.ltdidong.apphoctienganh.adapters.CrosswordAdapter;
import vn.ltdidong.apphoctienganh.api.GeminiApi;
import vn.ltdidong.apphoctienganh.models.CrosswordRow;
import vn.ltdidong.apphoctienganh.models.GeminiRequest;
import vn.ltdidong.apphoctienganh.models.GeminiResponse;

public class CrosswordGameActivity extends AppCompatActivity {

    private RecyclerView rvCrossword;
    private Button btnNewGame;
    private ImageButton btnBack;
    private LinearLayout layoutLoading;

    private CrosswordAdapter adapter;
    private List<CrosswordRow> gameRows;

    private GeminiApi geminiApi;
    // Using API Key from local.properties via BuildConfig
    private static final String API_KEY = "AIzaSyDOJpBmNfXE6aWZGRrb8Dy9XlzED1_QQNY";

    private TextView tvCurrentClue;
    private Button btnGuessKeyword;
    private String currentVerticalKeyword = "";
    private EditText etHiddenInput;
    private int selectedRowIndex = -1;

    private Button btnRestart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crossword_game);

        rvCrossword = findViewById(R.id.rvCrossword);
        btnNewGame = findViewById(R.id.btnNewGame);
        btnRestart = findViewById(R.id.btnRestart);
        btnBack = findViewById(R.id.btnBack);
        layoutLoading = findViewById(R.id.layoutLoading);
        tvCurrentClue = findViewById(R.id.tvCurrentClue);
        btnGuessKeyword = findViewById(R.id.btnGuessKeyword);
        etHiddenInput = findViewById(R.id.etHiddenInput);

        gameRows = new ArrayList<>();
        adapter = new CrosswordAdapter(this, gameRows, this::onRowClick);
        rvCrossword.setLayoutManager(new LinearLayoutManager(this));
        rvCrossword.setAdapter(adapter);

        setupGemini();
        setupInputLogic();

        btnNewGame.setOnClickListener(v -> generatePuzzle());
        btnRestart.setOnClickListener(v -> restartGame());
        btnBack.setOnClickListener(v -> finish());
        btnGuessKeyword.setOnClickListener(v -> showGuessKeywordDialog());

        // Start a game automatically
        // generatePuzzle(); -> Move to onResume/onCreate check
    }

    private void setupInputLogic() {
        etHiddenInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (selectedRowIndex != -1 && selectedRowIndex < gameRows.size()) {
                    CrosswordRow row = gameRows.get(selectedRowIndex);
                    if (!row.isSolved()) {
                        String input = s.toString().toUpperCase();
                        // Limit input to word length
                        if (input.length() > row.getWord().length()) {
                            input = input.substring(0, row.getWord().length());
                            etHiddenInput.setText(input);
                            etHiddenInput.setSelection(input.length());
                        }

                        row.setCurrentInput(input);
                        adapter.notifyItemChanged(selectedRowIndex);

                        // Check if correct
                        if (input.equalsIgnoreCase(row.getWord())) {
                            row.setSolved(true);
                            adapter.notifyItemChanged(selectedRowIndex);
                            saveGameState(); // Save on solve
                            Toast.makeText(CrosswordGameActivity.this, "Correct!", Toast.LENGTH_SHORT).show();
                            checkWinCondition();

                            // Hide keyboard
                            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(
                                    android.content.Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(etHiddenInput.getWindowToken(), 0);
                            selectedRowIndex = -1;
                            adapter.setSelectedRowIndex(-1);
                        } else {
                            // Save progress even if not solved yet (optional, but good for partial inputs)
                            saveGameState();
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
    }

    private void setupGemini() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        geminiApi = retrofit.create(GeminiApi.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // We load game state here if gameRows is empty (e.g. process death recovery)
        if (gameRows.isEmpty()) {
            loadGameState();
        }
    }

    private void generatePuzzle() {
        setLoading(true);
        if (tvCurrentClue != null)
            tvCurrentClue.setText("Loading new puzzle...");
        currentVerticalKeyword = "";

        // Prompt for Gemini - Requesting Vertical Alignment Data
        String prompt = "Generate a crossword puzzle with a hidden vertical keyword.\n" +
                "1. Choose a vertical keyword (e.g., \"SCHOOL\", \"SUMMER\", \"PLANET\") - 5 to 7 letters long.\n" +
                "2. Generate horizontal words. Each horizontal word must contain the corresponding letter of the vertical keyword at a specific position.\n"
                +
                "3. Theme: General Knowledge, School, or Daily Life (English A1-B1).\n" +
                "4. Output STRICTLY valid JSON object ONLY. Format:\n" +
                "{ \"main_keyword\": \"SCHOOL\", \"rows\": [ {\"word\": \"BUS\", \"clue\": \"Vehicle\", \"key_index\": 2}, {\"word\": \"CAT\", \"clue\": \"Animal\", \"key_index\": 0} ] }";

        geminiApi.generateContent(API_KEY, new GeminiRequest(prompt)).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonOutput = response.body().getOutputText();
                    Log.d("Crossword", "Gemini Response: " + jsonOutput);
                    parseAndLoadGame(jsonOutput);
                } else {
                    // If 2.5 fails (e.g. 503 Overloaded), try fallback to 1.5
                    Log.w("Crossword", "Gemini 2.5 failed with code " + response.code() + ". Trying fallback...");
                    generatePuzzleFallback(prompt);
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                setLoading(false);
                Log.e("Crossword", "Network Failure", t);
                Toast.makeText(CrosswordGameActivity.this, "Network error. Please check your connection.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generatePuzzleFallback(String prompt) {
        geminiApi.generateContentFallback(API_KEY, new GeminiRequest(prompt)).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonOutput = response.body().getOutputText();
                    Log.d("Crossword", "Gemini 1.5 Response: " + jsonOutput);
                    parseAndLoadGame(jsonOutput);
                } else {
                    setLoading(false);
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string()
                                : "Unknown error";
                        Log.e("Crossword", "Fallback API Error: " + response.code() + " - " + errorBody);
                    } catch (Exception e) {
                        Log.e("Crossword", "Error reading error body", e);
                    }
                    Toast.makeText(CrosswordGameActivity.this, "Failed to load level. Please try again later.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                setLoading(false);
                Log.e("Crossword", "Fallback Network Failure", t);
                Toast.makeText(CrosswordGameActivity.this, "Network error.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void parseAndLoadGame(String jsonString) {
        try {
            // Clean up markdown if present (e.g., ```json ... ```)
            if (jsonString.contains("```")) {
                jsonString = jsonString.replace("```json", "").replace("```", "");
            }
            jsonString = jsonString.trim();

            Gson gson = new Gson();
            // Try parsing new structure first
            try {
                GameData gameData = gson.fromJson(jsonString, GameData.class);
                if (gameData != null && gameData.rows != null) {
                    currentVerticalKeyword = gameData.main_keyword;
                    gameRows.clear();
                    int index = 1;

                    int maxKeyIndex = 0;
                    int maxRightSide = 0;

                    for (WordData data : gameData.rows) {
                        if (data.word != null && data.clue != null) {
                            CrosswordRow row = new CrosswordRow(index++, data.word, data.clue, data.key_index);
                            gameRows.add(row);

                            // Calculate dimensions
                            if (row.getKeyIndex() > maxKeyIndex) {
                                maxKeyIndex = row.getKeyIndex();
                            }
                            int rightSide = row.getWord().length() - 1 - row.getKeyIndex();
                            if (rightSide > maxRightSide) {
                                maxRightSide = rightSide;
                            }
                        }
                    }

                    int totalCols = maxKeyIndex + 1 + maxRightSide;
                    // Pass to adapter
                    adapter.setGridDimensions(maxKeyIndex, totalCols);

                } else {
                    // Fallback for old format (List<WordData>)
                    Type listType = new TypeToken<List<WordData>>() {
                    }.getType();
                    List<WordData> wordDataList = gson.fromJson(jsonString, listType);
                    gameRows.clear();
                    int index = 1;
                    for (WordData data : wordDataList) {
                        if (data.word != null && data.clue != null) {
                            gameRows.add(new CrosswordRow(index++, data.word, data.clue));
                        }
                    }
                    // Default sizing for fallback
                    adapter.setGridDimensions(4, 12);
                }
            } catch (Exception ex) {
                // Try simple list fallback
                Type listType = new TypeToken<List<WordData>>() {
                }.getType();
                List<WordData> wordDataList = gson.fromJson(jsonString, listType);
                gameRows.clear();
                int index = 1;
                for (WordData data : wordDataList) {
                    if (data.word != null && data.clue != null) {
                        gameRows.add(new CrosswordRow(index++, data.word, data.clue));
                    }
                }
                adapter.setGridDimensions(4, 12);
            }

            saveGameState(); // Initial save
            adapter.notifyDataSetChanged();
            setLoading(false);
            if (!gameRows.isEmpty()) {
                tvCurrentClue.setText("Tap a row to see the clue...");
            }

        } catch (Exception e) {
            Log.e("Crossword", "JSON Parse error", e);
            setLoading(false);
            Toast.makeText(this, "Error loading level data.", Toast.LENGTH_SHORT).show();
        }
    }

    private static final String PREFS_NAME = "CrosswordPrefs";
    private static final String KEY_GAME_STATE = "GameState";

    private void saveGameState() {
        try {
            SavedGameData savedData = new SavedGameData();
            savedData.rows = gameRows;
            savedData.verticalKeyword = currentVerticalKeyword;
            savedData.isGameOver = false; // Simplified

            Gson gson = new Gson();
            String json = gson.toJson(savedData);

            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString(KEY_GAME_STATE, json)
                    .apply();
        } catch (Exception e) {
            Log.e("Crossword", "Error saving game", e);
        }
    }

    private void loadGameState() {
        try {
            String json = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(KEY_GAME_STATE, null);
            if (json == null) {
                generatePuzzle();
                return;
            }

            Gson gson = new Gson();
            SavedGameData savedData = gson.fromJson(json, SavedGameData.class);

            if (savedData != null && savedData.rows != null && !savedData.rows.isEmpty()) {
                gameRows.clear();
                gameRows.addAll(savedData.rows);
                currentVerticalKeyword = savedData.verticalKeyword;

                // Recalculate dimensions
                int maxKeyIndex = 0;
                int maxRightSide = 0;
                for (CrosswordRow row : gameRows) {
                    if (row.getKeyIndex() > maxKeyIndex)
                        maxKeyIndex = row.getKeyIndex();
                    int rightSide = row.getWord().length() - 1 - row.getKeyIndex();
                    if (rightSide > maxRightSide)
                        maxRightSide = rightSide;
                }
                int totalCols = maxKeyIndex + 1 + maxRightSide;
                adapter.setGridDimensions(maxKeyIndex, totalCols);

                adapter.notifyDataSetChanged();
                tvCurrentClue.setText("Resumed game. Tap a row to continue.");
            } else {
                generatePuzzle();
            }
        } catch (Exception e) {
            Log.e("Crossword", "Error loading game", e);
            generatePuzzle();
        }
    }

    private void restartGame() {
        for (CrosswordRow row : gameRows) {
            row.setCurrentInput("");
            row.setSolved(false);
        }
        selectedRowIndex = -1;
        adapter.setSelectedRowIndex(-1);
        adapter.notifyDataSetChanged();
        saveGameState();
        Toast.makeText(this, "Game Restarted", Toast.LENGTH_SHORT).show();
    }

    // Inner class for saving
    private static class SavedGameData {
        List<CrosswordRow> rows;
        String verticalKeyword;
        boolean isGameOver;
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveGameState();
    }

    private void onRowClick(CrosswordRow row, int position) {
        // Show clue in the top TextView
        tvCurrentClue
                .setText("Q" + row.getIndex() + ": " + row.getQuestion() + " (" + row.getWord().length() + " letters)");

        if (row.isSolved()) {
            selectedRowIndex = -1;
            adapter.setSelectedRowIndex(-1);
            return;
        }

        // Set selected row
        selectedRowIndex = position;
        adapter.setSelectedRowIndex(position);

        // Setup hidden input
        etHiddenInput.setText(row.getCurrentInput());
        etHiddenInput.setSelection(etHiddenInput.getText().length());

        // Show Keyboard
        etHiddenInput.requestFocus();
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(
                android.content.Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(etHiddenInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
    }

    private void showGuessKeywordDialog() {
        if (currentVerticalKeyword == null || currentVerticalKeyword.isEmpty()) {
            Toast.makeText(this, "No hidden keyword for this level.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Guess Vertical Keyword");
        builder.setMessage("Enter the hidden vertical word (" + currentVerticalKeyword.length() + " letters):");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String guess = input.getText().toString().trim();
            if (guess.equalsIgnoreCase(currentVerticalKeyword)) {
                // Win immediately
                for (CrosswordRow row : gameRows) {
                    row.setCurrentInput(row.getWord());
                    row.setSolved(true);
                }
                adapter.notifyDataSetChanged();

                new AlertDialog.Builder(this)
                        .setTitle("VICTORY!")
                        .setMessage("Correct! The keyword was " + currentVerticalKeyword
                                + ".\nYou solved the entire puzzle!")
                        .setPositiveButton("New Game", (d, w) -> generatePuzzle())
                        .setNegativeButton("Close", null)
                        .show();

                // Clear saved state on win (or keep it as 'solved')
                // saveGameState();
            } else {
                Toast.makeText(this, "Incorrect keyword.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void checkWinCondition() {
        boolean allSolved = true;
        for (CrosswordRow row : gameRows) {
            if (!row.isSolved()) {
                allSolved = false;
                break;
            }
        }

        if (allSolved) {
            new AlertDialog.Builder(this)
                    .setTitle("CONGRATULATIONS!")
                    .setMessage("You have solved all words!")
                    .setPositiveButton("New Game", (dialog, which) -> generatePuzzle())
                    .setNegativeButton("Close", null)
                    .show();
        }
    }

    private void setLoading(boolean isLoading) {
        if (layoutLoading != null) {
            layoutLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnNewGame != null) {
            btnNewGame.setEnabled(!isLoading);
        }
    }

    // Helper class for JSON parsing
    private static class GameData {
        String main_keyword;
        List<WordData> rows;
    }

    private static class WordData {
        String word;
        String clue;
        int key_index = -1;
    }
}