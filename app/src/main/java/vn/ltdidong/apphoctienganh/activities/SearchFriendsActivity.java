package vn.ltdidong.apphoctienganh.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    
    private ImageButton btnBack, btnSearch;
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
            String friendName = (String) user.get("username");
            String friendEmail = (String) user.get("email");
            
            sendFriendRequest(friendId, friendName, friendEmail);
        });
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnSearch.setOnClickListener(v -> performSearch());
    }
    
    private void performSearch() {
        String query = etSearch.getText().toString().trim();
        
        if (query.isEmpty()) {
            Toast.makeText(this, "Enter username or email", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(android.view.View.VISIBLE);
        
        socialManager.searchUsers(query, new SocialManager.SearchCallback() {
            @Override
            public void onUsersFound(List<Map<String, Object>> users) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    searchResults.clear();
                    
                    // Filter out current user
                    for (Map<String, Object> user : users) {
                        if (!userId.equals(user.get("userId"))) {
                            searchResults.add(user);
                        }
                    }
                    
                    adapter.notifyDataSetChanged();
                    
                    if (searchResults.isEmpty()) {
                        Toast.makeText(SearchFriendsActivity.this,
                            "No users found", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            @Override
            public void onError(String error) {
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
