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
import java.util.Map;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.FriendRequestAdapter;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;
import vn.ltdidong.apphoctienganh.managers.SocialManager;

/**
 * Fragment hiển thị friend requests
 */
public class FriendRequestsFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private FriendRequestAdapter adapter;
    private List<Map<String, Object>> friendRequests;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    
    private SocialManager socialManager;
    private String userId;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_requests, container, false);
        
        initViews(view);
        setupManagers();
        setupRecyclerView();
        loadFriendRequests();
        
        return view;
    }
    
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewRequests);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        friendRequests = new ArrayList<>();
    }
    
    private void setupManagers() {
        userId = SharedPreferencesManager.getInstance(requireContext()).getUserId();
        socialManager = new SocialManager(requireContext());
    }
    
    private void setupRecyclerView() {
        adapter = new FriendRequestAdapter(requireContext(), friendRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        
        // Accept request
        adapter.setOnAcceptClickListener((request, position) -> {
            String requestId = (String) request.get("requestId");
            String fromUserId = (String) request.get("fromUserId");
            
            socialManager.acceptFriendRequest(requestId, userId, fromUserId,
                new SocialManager.FriendCallback() {
                    @Override
                    public void onSuccess(String message) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                                friendRequests.remove(position);
                                adapter.notifyItemRemoved(position);
                                
                                if (friendRequests.isEmpty()) {
                                    tvEmpty.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });
        });
        
        // Reject request
        adapter.setOnRejectClickListener((request, position) -> {
            String requestId = (String) request.get("requestId");
            
            socialManager.rejectFriendRequest(requestId,
                new SocialManager.FriendCallback() {
                    @Override
                    public void onSuccess(String message) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Request rejected", Toast.LENGTH_SHORT).show();
                                friendRequests.remove(position);
                                adapter.notifyItemRemoved(position);
                                
                                if (friendRequests.isEmpty()) {
                                    tvEmpty.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });
        });
    }
    
    private void loadFriendRequests() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        
        socialManager.getPendingFriendRequests(userId, new SocialManager.FriendRequestsCallback() {
            @Override
            public void onRequestsLoaded(List<Map<String, Object>> requests) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        friendRequests.clear();
                        friendRequests.addAll(requests);
                        adapter.notifyDataSetChanged();
                        
                        if (requests.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
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
                        tvEmpty.setText("Failed to load requests");
                    });
                }
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadFriendRequests();
    }
}
