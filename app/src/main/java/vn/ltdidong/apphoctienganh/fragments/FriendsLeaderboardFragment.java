package vn.ltdidong.apphoctienganh.fragments;

import android.os.Bundle;
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
 * Friends Leaderboard Fragment
 */
public class FriendsLeaderboardFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private List<LeaderboardUser> leaderboard;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    
    private SocialManager socialManager;
    private String userId;
    private boolean isDataLoaded = false; // Flag để tránh load trùng
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_leaderboard, container, false);
        
        initViews(view);
        setupManagers();
        setupRecyclerView();
        // Load sẽ được gọi trong onResume()
        
        return view;
    }
    
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewLeaderboard);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        leaderboard = new ArrayList<>();
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
    
    private void loadFriendsLeaderboard() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        
        if (userId == null || userId.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            tvEmpty.setText("Please log in to view friends leaderboard");
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }
        
        socialManager.getFriendsLeaderboard(userId, new SocialManager.LeaderboardCallback() {
            @Override
            public void onLeaderboard(List<LeaderboardUser> users) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        isDataLoaded = true; // Đánh dấu đã load xong
                        leaderboard.clear();
                        leaderboard.addAll(users);
                        adapter.notifyDataSetChanged();
                        
                        if (users.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            tvEmpty.setText("Add friends to see their rankings!");
                        }
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("Failed to load friends leaderboard");
                    });
                }
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Chỉ load khi chưa có data
        if (!isDataLoaded) {
            loadFriendsLeaderboard();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Reset flag khi rời khỏi fragment để có thể refresh lần sau
        isDataLoaded = false;
    }
}
