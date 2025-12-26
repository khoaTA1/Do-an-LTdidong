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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.ActivityFeedAdapter;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;
import vn.ltdidong.apphoctienganh.managers.SocialManager;
import vn.ltdidong.apphoctienganh.models.ActivityItem;

/**
 * Social Activity Feed Fragment
 * Hiển thị hoạt động của bạn bè
 */
public class SocialActivityFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    
    private ActivityFeedAdapter adapter;
    private List<ActivityItem> activities;
    private SocialManager socialManager;
    private String userId;
    private boolean isDataLoaded = false; // Flag để tránh load trùng
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_social_activity, container, false);
        
        initViews(view);
        setupManagers();
        setupRecyclerView();
        setupSwipeRefresh();
        // Không load ở đây nữa, để onResume() load
        
        return view;
    }
    
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewActivity);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        activities = new ArrayList<>();
    }
    
    private void setupManagers() {
        if (getContext() != null) {
            userId = SharedPreferencesManager.getInstance(requireContext()).getUserId();
            socialManager = new SocialManager(requireContext());
        }
    }
    
    private void setupRecyclerView() {
        adapter = new ActivityFeedAdapter(requireContext(), activities);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }
    
    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(this::loadActivityFeed);
        swipeRefresh.setColorSchemeResources(
            R.color.primary,
            R.color.accent,
            R.color.success
        );
    }
    
    private void loadActivityFeed() {
        if (userId == null || userId.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
            tvEmpty.setText("Please log in to view your friends' activities");
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        
        socialManager.getFriendsActivityFeed(userId, new SocialManager.ActivityFeedCallback() {
            @Override
            public void onActivitiesLoaded(List<ActivityItem> loadedActivities) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        isDataLoaded = true; // Đánh dấu đã load xong
                        
                        activities.clear();
                        activities.addAll(loadedActivities);
                        adapter.notifyDataSetChanged();
                        
                        if (activities.isEmpty()) {
                            tvEmpty.setText("No activities yet.\nAdd friends to see their learning progress!");
                            tvEmpty.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(requireContext(), "Failed to load activities: " + error, 
                            Toast.LENGTH_SHORT).show();
                        
                        if (activities.isEmpty()) {
                            tvEmpty.setText("Failed to load activities.\nPull to refresh.");
                            tvEmpty.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Chỉ load khi chưa có data hoặc user muốn refresh
        if (userId != null && !userId.isEmpty() && !isDataLoaded) {
            loadActivityFeed();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Reset flag khi rời khỏi fragment để có thể refresh lần sau
        isDataLoaded = false;
    }
}
