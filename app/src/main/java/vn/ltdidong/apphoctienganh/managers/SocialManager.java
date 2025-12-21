package vn.ltdidong.apphoctienganh.managers;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.ltdidong.apphoctienganh.models.Friend;
import vn.ltdidong.apphoctienganh.models.LeaderboardUser;

/**
 * Manager cho Social Features
 * - Quản lý bạn bè
 * - Leaderboard
 * - Chia sẻ thành tích
 * - Thách đấu
 */
public class SocialManager {
    
    private static final String TAG = "SocialManager";
    private Context context;
    private FirebaseFirestore firestore;
    
    public SocialManager(Context context) {
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
    }
    
    /**
     * Tìm kiếm user theo email hoặc username
     */
    public void searchUsers(String query, SearchCallback callback) {
        firestore.collection("users")
            .orderBy("username")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .limit(20)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Map<String, Object>> users = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    Map<String, Object> userData = doc.getData();
                    userData.put("userId", doc.getId());
                    users.add(userData);
                }
                if (callback != null) {
                    callback.onUsersFound(users);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Search failed", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            });
    }
    
    /**
     * Gửi lời mời kết bạn
     */
    public void sendFriendRequest(String fromUserId, String toUserId, String toUserName, 
                                   String toUserEmail, FriendCallback callback) {
        // Create friend request in both users' collections
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("fromUserId", fromUserId);
        requestData.put("toUserId", toUserId);
        requestData.put("toUserName", toUserName);
        requestData.put("toUserEmail", toUserEmail);
        requestData.put("status", "PENDING");
        requestData.put("timestamp", System.currentTimeMillis());
        
        firestore.collection("friend_requests")
            .add(requestData)
            .addOnSuccessListener(doc -> {
                if (callback != null) {
                    callback.onSuccess("Friend request sent!");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to send friend request", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            });
    }
    
    /**
     * Chấp nhận lời mời kết bạn
     */
    public void acceptFriendRequest(String requestId, String userId, String friendId, 
                                     FriendCallback callback) {
        // Update request status
        firestore.collection("friend_requests")
            .document(requestId)
            .update("status", "ACCEPTED")
            .addOnSuccessListener(aVoid -> {
                // Add to friends collection for both users
                addFriend(userId, friendId);
                addFriend(friendId, userId);
                
                if (callback != null) {
                    callback.onSuccess("Friend added!");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to accept request", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            });
    }
    
    private void addFriend(String userId, String friendId) {
        Map<String, Object> friendData = new HashMap<>();
        friendData.put("friendId", friendId);
        friendData.put("timestamp", System.currentTimeMillis());
        
        firestore.collection("users")
            .document(userId)
            .collection("friends")
            .document(friendId)
            .set(friendData);
    }
    
    /**
     * Lấy danh sách bạn bè
     */
    public void getFriends(String userId, FriendsListCallback callback) {
        firestore.collection("users")
            .document(userId)
            .collection("friends")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<String> friendIds = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    friendIds.add(doc.getString("friendId"));
                }
                
                // Get friends' info
                if (!friendIds.isEmpty()) {
                    getFriendsInfo(friendIds, callback);
                } else {
                    if (callback != null) {
                        callback.onFriendsList(new ArrayList<>());
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to get friends", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            });
    }
    
    private void getFriendsInfo(List<String> friendIds, FriendsListCallback callback) {
        List<Friend> friends = new ArrayList<>();
        final int[] counter = {0};
        
        for (String friendId : friendIds) {
            firestore.collection("users")
                .document(friendId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Friend friend = new Friend();
                        friend.setFriendId(friendId);
                        friend.setFriendName(doc.getString("username"));
                        friend.setFriendEmail(doc.getString("email"));
                        friend.setFriendAvatarUrl(doc.getString("avatarUrl"));
                        friend.setFriendLevel(doc.getLong("level") != null ? 
                            doc.getLong("level").intValue() : 0);
                        friend.setFriendTotalXP(doc.getLong("totalXP") != null ? 
                            doc.getLong("totalXP") : 0);
                        friend.setStatus("ACCEPTED");
                        friends.add(friend);
                    }
                    
                    counter[0]++;
                    if (counter[0] == friendIds.size()) {
                        if (callback != null) {
                            callback.onFriendsList(friends);
                        }
                    }
                });
        }
    }
    
    /**
     * Lấy bảng xếp hạng toàn cầu
     */
    public void getGlobalLeaderboard(int limit, LeaderboardCallback callback) {
        firestore.collection("users")
            .orderBy("totalXP", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<LeaderboardUser> leaderboard = new ArrayList<>();
                int rank = 1;
                
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    LeaderboardUser user = new LeaderboardUser();
                    user.setUserId(doc.getId());
                    user.setUsername(doc.getString("username"));
                    user.setAvatarUrl(doc.getString("avatarUrl"));
                    user.setLevel(doc.getLong("level") != null ? 
                        doc.getLong("level").intValue() : 0);
                    user.setTotalXP(doc.getLong("totalXP") != null ? 
                        doc.getLong("totalXP") : 0);
                    user.setRank(rank++);
                    leaderboard.add(user);
                }
                
                if (callback != null) {
                    callback.onLeaderboard(leaderboard);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to get leaderboard", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            });
    }
    
    /**
     * Lấy bảng xếp hạng bạn bè
     */
    public void getFriendsLeaderboard(String userId, LeaderboardCallback callback) {
        // First get friends list
        getFriends(userId, new FriendsListCallback() {
            @Override
            public void onFriendsList(List<Friend> friends) {
                if (friends.isEmpty()) {
                    callback.onLeaderboard(new ArrayList<>());
                    return;
                }
                
                // Get friends leaderboard data
                List<LeaderboardUser> leaderboard = new ArrayList<>();
                final int[] counter = {0};
                
                for (Friend friend : friends) {
                    firestore.collection("users")
                        .document(friend.getFriendId())
                        .get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                LeaderboardUser user = new LeaderboardUser();
                                user.setUserId(friend.getFriendId());
                                user.setUsername(doc.getString("username"));
                                user.setAvatarUrl(doc.getString("avatarUrl"));
                                user.setLevel(doc.getLong("level") != null ? 
                                    doc.getLong("level").intValue() : 0);
                                user.setTotalXP(doc.getLong("totalXP") != null ? 
                                    doc.getLong("totalXP") : 0);
                                user.setFriend(true);
                                leaderboard.add(user);
                            }
                            
                            counter[0]++;
                            if (counter[0] == friends.size()) {
                                // Sort by XP
                                leaderboard.sort((a, b) -> 
                                    Long.compare(b.getTotalXP(), a.getTotalXP()));
                                
                                // Add ranks
                                for (int i = 0; i < leaderboard.size(); i++) {
                                    leaderboard.get(i).setRank(i + 1);
                                }
                                
                                if (callback != null) {
                                    callback.onLeaderboard(leaderboard);
                                }
                            }
                        });
                }
            }
            
            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }
    
    /**
     * Xóa bạn
     */
    public void removeFriend(String userId, String friendId, FriendCallback callback) {
        firestore.collection("users")
            .document(userId)
            .collection("friends")
            .document(friendId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                // Also remove from friend's list
                firestore.collection("users")
                    .document(friendId)
                    .collection("friends")
                    .document(userId)
                    .delete();
                
                if (callback != null) {
                    callback.onSuccess("Friend removed");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to remove friend", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            });
    }
    
    // Callbacks
    public interface SearchCallback {
        void onUsersFound(List<Map<String, Object>> users);
        void onError(String error);
    }
    
    public interface FriendCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    public interface FriendsListCallback {
        void onFriendsList(List<Friend> friends);
        void onError(String error);
    }
    
    public interface LeaderboardCallback {
        void onLeaderboard(List<LeaderboardUser> users);
        void onError(String error);
    }
}
