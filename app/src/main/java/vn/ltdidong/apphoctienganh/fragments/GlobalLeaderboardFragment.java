package vn.ltdidong.apphoctienganh.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
    
    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private List<LeaderboardUser> leaderboard;
    private ProgressBar progressBar;
    
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
    
    private void loadLeaderboard() {
        progressBar.setVisibility(View.VISIBLE);
        
        socialManager.getGlobalLeaderboard(100, new SocialManager.LeaderboardCallback() {
            @Override
            public void onLeaderboard(List<LeaderboardUser> users) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        leaderboard.clear();
                        
                        // Mark current user
                        for (LeaderboardUser user : users) {
                            if (user.getUserId().equals(userId)) {
                                user.setCurrentUser(true);
                            }
                            leaderboard.add(user);
                        }
                        
                        adapter.notifyDataSetChanged();
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), 
                            "Failed to load leaderboard", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}
