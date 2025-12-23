package vn.ltdidong.apphoctienganh.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.LeaderboardAdapter;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;
import vn.ltdidong.apphoctienganh.managers.SocialManager;
import vn.ltdidong.apphoctienganh.models.LeaderboardUser;

/**
 * Global Leaderboard Fragment
 */
public class GlobalLeaderboardFragment extends Fragment {
    
    private static final String TAG = "GlobalLeaderboard";
    
    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private List<LeaderboardUser> leaderboard;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    
    private SocialManager socialManager;
    private String userId;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_global_leaderboard, container, false);
        
        initViews(view);
        setupManagers();
        setupRecyclerView();
        loadLeaderboard();
        
        return view;
    }
    
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewLeaderboard);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        leaderboard = new ArrayList<>();
        
        Log.d(TAG, "Views initialized");
    }
    
    private void setupManagers() {
        userId = SharedPreferencesManager.getInstance(requireContext()).getUserId();
        socialManager = new SocialManager(requireContext());
    }
    
    private void setupRecyclerView() {
        adapter = new LeaderboardAdapter(requireContext(), leaderboard);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }
    
    private void loadLeaderboard() {
        Log.d(TAG, "Loading leaderboard for userId: " + userId);
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.GONE);
        }
        
        // Lấy tất cả users từ Firebase (max 10000 - Firebase limit)
        socialManager.getGlobalLeaderboard(10000, new SocialManager.LeaderboardCallback() {
            @Override
            public void onLeaderboard(List<LeaderboardUser> users) {
                Log.d(TAG, "Received " + users.size() + " users from Firebase");
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        leaderboard.clear();
                        
                        if (users.isEmpty()) {
                            // Show empty state
                            recyclerView.setVisibility(View.GONE);
                            if (tvEmptyState != null) {
                                tvEmptyState.setVisibility(View.VISIBLE);
                                tvEmptyState.setText("No users in leaderboard yet.\nBe the first!");
                            }
                            Log.d(TAG, "No users found in Firebase");
                        } else {
                            // Mark current user and show list
                            for (LeaderboardUser user : users) {
                                if (userId != null && user.getUserId().equals(userId)) {
                                    user.setCurrentUser(true);
                                }
                                leaderboard.add(user);
                            }
                            
                            recyclerView.setVisibility(View.VISIBLE);
                            if (tvEmptyState != null) {
                                tvEmptyState.setVisibility(View.GONE);
                            }
                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "Leaderboard updated with " + leaderboard.size() + " users");
                        }
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading leaderboard: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        if (tvEmptyState != null) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            tvEmptyState.setText("Failed to load leaderboard\n\n" + error);
                        }
                        Toast.makeText(requireContext(), 
                            "Error: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
}
