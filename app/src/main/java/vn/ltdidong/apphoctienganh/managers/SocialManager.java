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
        Log.d(TAG, "Searching users with query: " + query);
        
        // Search by email first
        firestore.collection("users")
            .whereEqualTo("email", query)
            .limit(20)
            .get()
            .addOnSuccessListener(emailSnapshot -> {
                List<Map<String, Object>> users = new ArrayList<>();
                
                // Add email matches
                for (QueryDocumentSnapshot doc : emailSnapshot) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("userId", doc.getId());
                    
                    // Use fullname, fallback to username or email
                    String displayName = doc.getString("fullname");
                    if (displayName == null || displayName.isEmpty()) {
                        displayName = doc.getString("username");
                    }
                    if (displayName == null || displayName.isEmpty()) {
                        displayName = doc.getString("email");
                    }
                    userData.put("username", displayName);
                    userData.put("email", doc.getString("email"));
                    userData.put("fullname", doc.getString("fullname"));
                    userData.put("level", doc.getLong("current_level"));
                    userData.put("totalXP", doc.getLong("total_xp"));
                    users.add(userData);
                }
                
                // If no email match, try username/fullname search
                if (users.isEmpty()) {
                    firestore.collection("users")
                        .orderBy("fullname")
                        .startAt(query)
                        .endAt(query + "\uf8ff")
                        .limit(20)
                        .get()
                        .addOnSuccessListener(nameSnapshot -> {
                            for (QueryDocumentSnapshot doc : nameSnapshot) {
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("userId", doc.getId());
                                
                                String displayName = doc.getString("fullname");
                                if (displayName == null || displayName.isEmpty()) {
                                    displayName = doc.getString("username");
                                }
                                if (displayName == null || displayName.isEmpty()) {
                                    displayName = doc.getString("email");
                                }
                                userData.put("username", displayName);
                                userData.put("email", doc.getString("email"));
                                userData.put("fullname", doc.getString("fullname"));
                                userData.put("level", doc.getLong("current_level"));
                                userData.put("totalXP", doc.getLong("total_xp"));
                                users.add(userData);
                            }
                            
                            Log.d(TAG, "Found " + users.size() + " users");
                            if (callback != null) {
                                callback.onUsersFound(users);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to search by name", e);
                            if (callback != null) {
                                callback.onError(e.getMessage());
                            }
                        });
                } else {
                    Log.d(TAG, "Found " + users.size() + " users by email");
                    if (callback != null) {
                        callback.onUsersFound(users);
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to search users", e);
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
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Cannot get friends: userId is null or empty");
            if (callback != null) {
                callback.onFriendsList(new ArrayList<>());
            }
            return;
        }
        
        firestore.collection("users")
            .document(userId)
            .collection("friends")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<String> friendIds = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    String friendId = doc.getString("friendId");
                    if (friendId != null && !friendId.isEmpty()) {
                        friendIds.add(friendId);
                    }
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
                        
                        // Try fullname first, fallback to username, then email
                        String name = doc.getString("fullname");
                        if (name == null || name.isEmpty()) {
                            name = doc.getString("username");
                        }
                        if (name == null || name.isEmpty()) {
                            name = doc.getString("email");
                        }
                        friend.setFriendName(name);
                        
                        friend.setFriendEmail(doc.getString("email"));
                        friend.setFriendAvatarUrl(doc.getString("avatarUrl"));
                        friend.setFriendLevel(doc.getLong("current_level") != null ? 
                            doc.getLong("current_level").intValue() : 0);
                        friend.setFriendTotalXP(doc.getLong("total_xp") != null ? 
                            doc.getLong("total_xp") : 0);
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
        Log.d(TAG, "Fetching global leaderboard with limit: " + limit);
        
        firestore.collection("users")
            .orderBy("total_xp", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                Log.d(TAG, "Query successful, found " + querySnapshot.size() + " documents");
                List<LeaderboardUser> leaderboard = new ArrayList<>();
                int rank = 1;
                
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    LeaderboardUser user = new LeaderboardUser();
                    user.setUserId(doc.getId());
                    
                    // Try fullname first, fallback to username, then email
                    String displayName = doc.getString("fullname");
                    if (displayName == null || displayName.isEmpty()) {
                        displayName = doc.getString("username");
                    }
                    if (displayName == null || displayName.isEmpty()) {
                        displayName = doc.getString("email");
                    }
                    user.setUsername(displayName != null ? displayName : "Unknown");
                    
                    user.setAvatarUrl(doc.getString("avatarUrl"));
                    
                    // Use current_level from Firebase
                    user.setLevel(doc.getLong("current_level") != null ? 
                        doc.getLong("current_level").intValue() : 0);
                    
                    // Use total_xp from Firebase
                    user.setTotalXP(doc.getLong("total_xp") != null ? 
                        doc.getLong("total_xp") : 0);
                    
                    user.setRank(rank++);
                    leaderboard.add(user);
                    
                    Log.d(TAG, "User " + rank + ": " + user.getUsername() + " (XP: " + user.getTotalXP() + ")");
                }
                
                Log.d(TAG, "Returning " + leaderboard.size() + " users in leaderboard");
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
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Cannot get friends leaderboard: userId is null or empty");
            if (callback != null) {
                callback.onLeaderboard(new ArrayList<>());
            }
            return;
        }
        
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
                                
                                // Try fullname first, fallback to username, then email
                                String displayName = doc.getString("fullname");
                                if (displayName == null || displayName.isEmpty()) {
                                    displayName = doc.getString("username");
                                }
                                if (displayName == null || displayName.isEmpty()) {
                                    displayName = doc.getString("email");
                                }
                                user.setUsername(displayName != null ? displayName : "Unknown");
                                
                                user.setAvatarUrl(doc.getString("avatarUrl"));
                                user.setLevel(doc.getLong("current_level") != null ? 
                                    doc.getLong("current_level").intValue() : 0);
                                user.setTotalXP(doc.getLong("total_xp") != null ? 
                                    doc.getLong("total_xp") : 0);
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
    /**
     * Lấy danh sách friend requests đang pending
     */
    public void getPendingFriendRequests(String userId, FriendRequestsCallback callback) {
        firestore.collection("friend_requests")
            .whereEqualTo("toUserId", userId)
            .whereEqualTo("status", "PENDING")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Map<String, Object>> requests = new ArrayList<>();
                
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    Map<String, Object> requestData = new HashMap<>();
                    requestData.put("requestId", doc.getId());
                    requestData.put("fromUserId", doc.getString("fromUserId"));
                    requestData.put("fromUserName", doc.getString("toUserName")); // Name of sender
                    requestData.put("fromUserEmail", doc.getString("toUserEmail"));
                    requestData.put("timestamp", doc.getLong("timestamp"));
                    
                    // Get sender's full info
                    String fromUserId = doc.getString("fromUserId");
                    firestore.collection("users")
                        .document(fromUserId)
                        .get()
                        .addOnSuccessListener(userDoc -> {
                            if (userDoc.exists()) {
                                String displayName = userDoc.getString("fullname");
                                if (displayName == null || displayName.isEmpty()) {
                                    displayName = userDoc.getString("username");
                                }
                                if (displayName == null || displayName.isEmpty()) {
                                    displayName = userDoc.getString("email");
                                }
                                requestData.put("fromUserName", displayName);
                                requestData.put("level", userDoc.getLong("current_level"));
                                requestData.put("totalXP", userDoc.getLong("total_xp"));
                            }
                            
                            requests.add(requestData);
                            
                            // If all requests loaded, return
                            if (requests.size() == querySnapshot.size()) {
                                if (callback != null) {
                                    callback.onRequestsLoaded(requests);
                                }
                            }
                        });
                }
                
                if (querySnapshot.isEmpty() && callback != null) {
                    callback.onRequestsLoaded(new ArrayList<>());
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to get friend requests", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            });
    }
    
    /**
     * Reject friend request
     */
    public void rejectFriendRequest(String requestId, FriendCallback callback) {
        firestore.collection("friend_requests")
            .document(requestId)
            .update("status", "REJECTED")
            .addOnSuccessListener(aVoid -> {
                if (callback != null) {
                    callback.onSuccess("Request rejected");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to reject request", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            });
    }
    
    /**
     * Get count of pending friend requests
     */
    public void getPendingRequestCount(String userId, RequestCountCallback callback) {
        firestore.collection("friend_requests")
            .whereEqualTo("toUserId", userId)
            .whereEqualTo("status", "PENDING")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (callback != null) {
                    callback.onCount(querySnapshot.size());
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to get request count", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            });
    }
    
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
    
    public interface FriendRequestsCallback {
        void onRequestsLoaded(List<Map<String, Object>> requests);
        void onError(String error);
    }
    
    public interface RequestCountCallback {
        void onCount(int count);
        void onError(String error);
    }
    
    public interface ActivityFeedCallback {
        void onActivitiesLoaded(List<vn.ltdidong.apphoctienganh.models.ActivityItem> activities);
        void onError(String error);
    }
    
    public interface ChallengeCallback {
        void onSuccess(String challengeId);
        void onError(String error);
    }
    
    public interface ChallengesListCallback {
        void onChallengesLoaded(List<vn.ltdidong.apphoctienganh.models.FriendChallenge> challenges);
        void onError(String error);
    }
    
    public interface ChallengeActionCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    // ============= FRIEND CHALLENGE METHODS =============
    
    /**
     * Tạo thách đấu với bạn bè
     */
    public void createChallenge(String challengerId, String challengerName, String opponentId, 
                               String opponentName, String challengeType, String title, 
                               String description, int totalQuestions, ChallengeCallback callback) {
        
        vn.ltdidong.apphoctienganh.models.FriendChallenge challenge = new vn.ltdidong.apphoctienganh.models.FriendChallenge();
        challenge.setChallengerId(challengerId);
        challenge.setChallengerName(challengerName);
        challenge.setOpponentId(opponentId);
        challenge.setOpponentName(opponentName);
        challenge.setChallengeType(challengeType);
        challenge.setChallengeTitle(title);
        challenge.setChallengeDescription(description);
        challenge.setTotalQuestions(totalQuestions);
        
        // Get avatars
        getUserAvatar(challengerId, challengerAvatar -> {
            challenge.setChallengerAvatar(challengerAvatar);
            
            getUserAvatar(opponentId, opponentAvatar -> {
                challenge.setOpponentAvatar(opponentAvatar);
                
                // Save to Firestore
                firestore.collection("friend_challenges")
                    .add(challenge)
                    .addOnSuccessListener(docRef -> {
                        Log.d(TAG, "Challenge created: " + docRef.getId());
                        
                        // Create notification for opponent
                        createChallengeNotification(opponentId, challengerName, title);
                        
                        // Create activity
                        createActivity(challengerId, challengerName, "challenge",
                            "Challenge Created!",
                            "Challenged " + opponentName + " to " + title,
                            0);
                        
                        if (callback != null) {
                            callback.onSuccess(docRef.getId());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to create challenge", e);
                        if (callback != null) {
                            callback.onError(e.getMessage());
                        }
                    });
            });
        });
    }
    
    /**
     * Lấy danh sách challenges của user
     */
    public void getUserChallenges(String userId, ChallengesListCallback callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) {
                callback.onChallengesLoaded(new ArrayList<>());
            }
            return;
        }
        
        // Get challenges where user is challenger or opponent
        firestore.collection("friend_challenges")
            .whereIn("status", java.util.Arrays.asList("pending", "accepted", "in_progress"))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<vn.ltdidong.apphoctienganh.models.FriendChallenge> challenges = new ArrayList<>();
                
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    vn.ltdidong.apphoctienganh.models.FriendChallenge challenge = 
                        doc.toObject(vn.ltdidong.apphoctienganh.models.FriendChallenge.class);
                    challenge.setId(doc.getId());
                    
                    // Filter for this user
                    if (userId.equals(challenge.getChallengerId()) || 
                        userId.equals(challenge.getOpponentId())) {
                        
                        // Check if expired
                        if (challenge.isExpired()) {
                            challenge.setStatus("expired");
                            updateChallengeStatus(challenge.getId(), "expired");
                        }
                        
                        challenges.add(challenge);
                    }
                }
                
                Log.d(TAG, "Loaded " + challenges.size() + " challenges for user");
                if (callback != null) {
                    callback.onChallengesLoaded(challenges);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to load challenges", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            });
    }
    
    /**
     * Lấy lịch sử challenges đã hoàn thành
     */
    public void getChallengeHistory(String userId, ChallengesListCallback callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) {
                callback.onChallengesLoaded(new ArrayList<>());
            }
            return;
        }
        
        firestore.collection("friend_challenges")
            .whereEqualTo("status", "completed")
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<vn.ltdidong.apphoctienganh.models.FriendChallenge> challenges = new ArrayList<>();
                
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    vn.ltdidong.apphoctienganh.models.FriendChallenge challenge = 
                        doc.toObject(vn.ltdidong.apphoctienganh.models.FriendChallenge.class);
                    challenge.setId(doc.getId());
                    
                    // Filter for this user
                    if (userId.equals(challenge.getChallengerId()) || 
                        userId.equals(challenge.getOpponentId())) {
                        challenges.add(challenge);
                    }
                }
                
                if (callback != null) {
                    callback.onChallengesLoaded(challenges);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to load challenge history", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            });
    }
    
    /**
     * Chấp nhận thách đấu
     */
    public void acceptChallenge(String challengeId, ChallengeActionCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "accepted");
        updates.put("acceptedAt", System.currentTimeMillis());
        
        firestore.collection("friend_challenges")
            .document(challengeId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Challenge accepted: " + challengeId);
                if (callback != null) {
                    callback.onSuccess("Challenge accepted!");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to accept challenge", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            });
    }
    
    /**
     * Từ chối thách đấu
     */
    public void declineChallenge(String challengeId, ChallengeActionCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "declined");
        
        firestore.collection("friend_challenges")
            .document(challengeId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Challenge declined: " + challengeId);
                if (callback != null) {
                    callback.onSuccess("Challenge declined");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to decline challenge", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            });
    }
    
    /**
     * Cập nhật điểm số của challenge
     */
    public void updateChallengeScore(String challengeId, String userId, int score, 
                                    ChallengeActionCallback callback) {
        
        firestore.collection("friend_challenges")
            .document(challengeId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    vn.ltdidong.apphoctienganh.models.FriendChallenge challenge = 
                        doc.toObject(vn.ltdidong.apphoctienganh.models.FriendChallenge.class);
                    
                    Map<String, Object> updates = new HashMap<>();
                    
                    if (userId.equals(challenge.getChallengerId())) {
                        updates.put("challengerScore", score);
                        updates.put("challengerCompleted", true);
                    } else if (userId.equals(challenge.getOpponentId())) {
                        updates.put("opponentScore", score);
                        updates.put("opponentCompleted", true);
                    }
                    
                    updates.put("status", "in_progress");
                    
                    // Check if both completed
                    boolean bothCompleted = false;
                    if (userId.equals(challenge.getChallengerId())) {
                        bothCompleted = challenge.isOpponentCompleted();
                    } else {
                        bothCompleted = challenge.isChallengerCompleted();
                    }
                    
                    if (bothCompleted) {
                        // Determine winner
                        challenge.setChallengerScore(
                            userId.equals(challenge.getChallengerId()) ? score : challenge.getChallengerScore()
                        );
                        challenge.setOpponentScore(
                            userId.equals(challenge.getOpponentId()) ? score : challenge.getOpponentScore()
                        );
                        challenge.setChallengerCompleted(true);
                        challenge.setOpponentCompleted(true);
                        challenge.determineWinner();
                        
                        updates.put("status", "completed");
                        updates.put("winnerId", challenge.getWinnerId());
                        updates.put("winnerName", challenge.getWinnerName());
                        updates.put("xpReward", challenge.getXpReward());
                        updates.put("completedAt", challenge.getCompletedAt());
                    }
                    
                    firestore.collection("friend_challenges")
                        .document(challengeId)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            if (callback != null) {
                                callback.onSuccess("Score updated!");
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (callback != null) {
                                callback.onError(e.getMessage());
                            }
                        });
                }
            });
    }
    
    /**
     * Lấy bảng xếp hạng challenges (theo số thắng)
     */
    public void getChallengeLeaderboard(LeaderboardCallback callback) {
        firestore.collection("users")
            .orderBy("challengeWins", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<LeaderboardUser> leaderboard = new ArrayList<>();
                
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    LeaderboardUser user = new LeaderboardUser();
                    user.setUserId(doc.getId());
                    
                    String name = doc.getString("fullname");
                    if (name == null || name.isEmpty()) {
                        name = doc.getString("username");
                    }
                    if (name == null || name.isEmpty()) {
                        name = doc.getString("email");
                    }
                    user.setUsername(name);
                    user.setAvatarUrl(doc.getString("avatarUrl"));
                    
                    Long wins = doc.getLong("challengeWins");
                    user.setTotalXP(wins != null ? wins : 0);
                    
                    Long level = doc.getLong("current_level");
                    user.setLevel(level != null ? level.intValue() : 1);
                    
                    leaderboard.add(user);
                }
                
                if (callback != null) {
                    callback.onLeaderboard(leaderboard);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to load challenge leaderboard", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            });
    }
    
    // Helper methods
    private void getUserAvatar(String userId, AvatarCallback callback) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String avatar = doc.getString("avatarUrl");
                    callback.onAvatarLoaded(avatar);
                } else {
                    callback.onAvatarLoaded(null);
                }
            })
            .addOnFailureListener(e -> callback.onAvatarLoaded(null));
    }
    
    private void updateChallengeStatus(String challengeId, String status) {
        firestore.collection("friend_challenges")
            .document(challengeId)
            .update("status", status);
    }
    
    private void createChallengeNotification(String userId, String challengerName, String title) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("type", "challenge_invite");
        notification.put("title", "New Challenge!");
        notification.put("message", challengerName + " challenged you to " + title);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);
        
        firestore.collection("notifications").add(notification);
    }
    
    private interface AvatarCallback {
        void onAvatarLoaded(String avatarUrl);
    }
    
    /**
     * Lấy activity feed của bạn bè
     */
    public void getFriendsActivityFeed(String userId, ActivityFeedCallback callback) {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Cannot get activity feed: userId is null");
            if (callback != null) {
                callback.onActivitiesLoaded(new ArrayList<>());
            }
            return;
        }
        
        // First get friends list
        getFriends(userId, new FriendsListCallback() {
            @Override
            public void onFriendsList(List<Friend> friends) {
                if (friends.isEmpty()) {
                    callback.onActivitiesLoaded(new ArrayList<>());
                    return;
                }
                
                // Get friend IDs
                List<String> friendIds = new ArrayList<>();
                for (Friend friend : friends) {
                    friendIds.add(friend.getFriendId());
                }
                
                // Fetch activities from all friends
                fetchActivitiesForUsers(friendIds, callback);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to get friends for activity feed: " + error);
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }
    
    /**
     * Lấy activities cho danh sách users
     */
    private void fetchActivitiesForUsers(List<String> userIds, ActivityFeedCallback callback) {
        // Query activities collection for these users
        firestore.collection("social_activities")
            .whereIn("userId", userIds)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<vn.ltdidong.apphoctienganh.models.ActivityItem> activities = new ArrayList<>();
                
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    vn.ltdidong.apphoctienganh.models.ActivityItem activity = doc.toObject(vn.ltdidong.apphoctienganh.models.ActivityItem.class);
                    activity.setId(doc.getId());
                    activities.add(activity);
                }
                
                Log.d(TAG, "Loaded " + activities.size() + " activities");
                if (callback != null) {
                    callback.onActivitiesLoaded(activities);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to fetch activities", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            });
    }
    
    /**
     * Tạo activity khi user đạt achievement
     */
    public void createAchievementActivity(String userId, String userName, String achievementName, int xpGained) {
        createActivity(userId, userName, "achievement", 
            "Achievement Unlocked!", 
            "Unlocked: " + achievementName, 
            xpGained);
    }
    
    /**
     * Tạo activity khi user hoàn thành bài học
     */
    public void createCompletionActivity(String userId, String userName, String lessonName, int score) {
        createActivity(userId, userName, "completion", 
            "Completed a lesson", 
            lessonName + " - Score: " + score + "%", 
            score / 2);
    }
    
    /**
     * Tạo activity khi user level up
     */
    public void createLevelUpActivity(String userId, String userName, int newLevel) {
        createActivity(userId, userName, "level_up", 
            "Level Up!", 
            "Reached Level " + newLevel, 
            newLevel * 10);
    }
    
    /**
     * Tạo activity khi user đạt streak mới
     */
    public void createStreakActivity(String userId, String userName, int streakDays) {
        createActivity(userId, userName, "streak", 
            "Streak Milestone!", 
            streakDays + " days learning streak 🔥", 
            streakDays * 5);
    }
    
    /**
     * Tạo activity chung
     */
    private void createActivity(String userId, String userName, String type, String title, String description, int xpGained) {
        vn.ltdidong.apphoctienganh.models.ActivityItem activity = new vn.ltdidong.apphoctienganh.models.ActivityItem();
        activity.setUserId(userId);
        activity.setUserName(userName);
        activity.setActivityType(type);
        activity.setTitle(title);
        activity.setDescription(description);
        activity.setXpGained(xpGained);
        activity.setTimestamp(System.currentTimeMillis());
        
        // Get user avatar
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String avatarUrl = doc.getString("avatarUrl");
                    activity.setUserAvatar(avatarUrl);
                }
                
                // Save activity to Firestore
                firestore.collection("social_activities")
                    .add(activity)
                    .addOnSuccessListener(docRef -> {
                        Log.d(TAG, "Activity created: " + docRef.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to create activity", e);
                    });
            });
    }
}
