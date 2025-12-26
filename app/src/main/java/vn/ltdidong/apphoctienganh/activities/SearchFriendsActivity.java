package vn.ltdidong.apphoctienganh.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.UserSearchAdapter;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;
import vn.ltdidong.apphoctienganh.managers.SocialManager;

/**
 * Search Friends Activity - Tìm kiếm và thêm bạn
 */
public class SearchFriendsActivity extends AppCompatActivity {
    
    private static final String TAG = "SearchFriends";
    
    private ImageButton btnBack;
    private MaterialButton btnSearch;
    private EditText etSearch;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private UserSearchAdapter adapter;
    private List<Map<String, Object>> searchResults;
    
    private SocialManager socialManager;
    private String userId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friends);
        
        initViews();
        setupManagers();
        setupRecyclerView();
        setupListeners();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSearch = findViewById(R.id.btnSearch);
        etSearch = findViewById(R.id.etSearch);
        recyclerView = findViewById(R.id.recyclerViewResults);
        progressBar = findViewById(R.id.progressBar);
        
        searchResults = new ArrayList<>();
    }
    
    private void setupManagers() {
        userId = SharedPreferencesManager.getInstance(this).getUserId();
        socialManager = new SocialManager(this);
    }
    
    private void setupRecyclerView() {
        adapter = new UserSearchAdapter(this, searchResults);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        adapter.setOnAddFriendClickListener(user -> {
            String friendId = (String) user.get("userId");
            
            // Get name with fallback logic
            String friendName = (String) user.get("fullname");
            if (friendName == null || friendName.isEmpty()) {
                friendName = (String) user.get("username");
            }
            if (friendName == null || friendName.isEmpty()) {
                friendName = (String) user.get("email");
            }
            
            String friendEmail = (String) user.get("email");
            
            // Create final variables for lambda
            String finalFriendName = friendName;
            
            // Show confirmation dialog before sending request
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Add Friend")
                .setMessage("Send friend request to " + finalFriendName + "?")
                .setPositiveButton("Send", (dialog, which) -> {
                    sendFriendRequest(friendId, finalFriendName, friendEmail);
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnSearch.setOnClickListener(v -> performSearch());
        
        // Search when pressing Enter on keyboard
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }
    
    private void performSearch() {
        String query = etSearch.getText().toString().trim();
        
        Log.d(TAG, "Performing search with query: " + query);
        
        if (query.isEmpty()) {
            Toast.makeText(this, "Enter name or email to search", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Hide keyboard
        android.view.inputmethod.InputMethodManager imm = 
            (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        
        progressBar.setVisibility(android.view.View.VISIBLE);
        
        socialManager.searchUsers(query, new SocialManager.SearchCallback() {
            @Override
            public void onUsersFound(List<Map<String, Object>> users) {
                Log.d(TAG, "Found " + users.size() + " users");
                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    searchResults.clear();
                    
                    // Filter out current user
                    for (Map<String, Object> user : users) {
                        String searchUserId = (String) user.get("userId");
                        if (userId == null || !userId.equals(searchUserId)) {
                            searchResults.add(user);
                        }
                    }
                    
                    adapter.notifyDataSetChanged();
                    
                    if (searchResults.isEmpty()) {
                        Toast.makeText(SearchFriendsActivity.this,
                            "No users found matching \"" + query + "\"", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SearchFriendsActivity.this,
                            "Found " + searchResults.size() + " user(s)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Search error: " + error);
                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    Toast.makeText(SearchFriendsActivity.this,
                        "Search failed: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void sendFriendRequest(String friendId, String friendName, String friendEmail) {
        socialManager.sendFriendRequest(userId, friendId, friendName, friendEmail,
            new SocialManager.FriendCallback() {
                @Override
                public void onSuccess(String message) {
                    runOnUiThread(() -> 
                        Toast.makeText(SearchFriendsActivity.this,
                            message, Toast.LENGTH_SHORT).show());
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> 
                        Toast.makeText(SearchFriendsActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show());
                }
            });
    }
}
