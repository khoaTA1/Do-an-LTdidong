package vn.ltdidong.apphoctienganh.repositories;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import vn.ltdidong.apphoctienganh.functions.FirestoreCallBack;
import vn.ltdidong.apphoctienganh.models.User;

public class UserRepo {
    private String COLLECTION_NAME = "users";
    private final FirebaseFirestore db;

    public UserRepo() {
        db = FirebaseFirestore.getInstance();
    }

    // CREATE
    public void createUser(Map<String, Object> user, Context context) {

        // lưu vào firestore
        // lấy user id mới nhất và cập nhật lại bằng transaction nhằm tránh race condition
        DocumentReference docRef = db.collection("pref").document("trackLastUserId");

        db.runTransaction(transaction -> {
                    DocumentSnapshot docSnap = transaction.get(docRef);

                    long lastUserId = docSnap.getLong("lastUserId");

                    // cập nhật pref
                    transaction.update(docRef, "lastUserId", lastUserId + 1);

                    // lưu user mới vào firestore
                    transaction.set(db.collection(COLLECTION_NAME).document(String.valueOf(lastUserId + 1)), user);

                    return lastUserId + 1;
                }).addOnSuccessListener(userId -> {
                    Log.d(">>> User Repo", "Đã thêm user: " + userId);
                    Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("!!! User Repo", "Lỗi: ", e);
                    Toast.makeText(context, "Đã có lỗi xảy ra!", Toast.LENGTH_SHORT).show();
                });
    }

    // READ
    public void getUserById(long id, FirestoreCallBack callback) {

        db.collection(COLLECTION_NAME)
                .document(String.valueOf(id))
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Log.d(">>> User Repo", "Tồn tại user");

                        User user = new User();

                        // user.setId(Long.valueOf(snapshot.getId())); // snapshot.getId() returns String, not convertible
                        user.setFullName(snapshot.getString("fullname"));
                        user.setEmail(snapshot.getString("email"));
                        user.setAddress(snapshot.getString("address"));
                        //user.setCreatedAt(snapshot.getTimestamp("createdAt").toString());
                        //user.setPassword(snapshot.getString("password"));
                        //user.setS

                        callback.returnResult(user);
                    } else {
                        Log.d("!!! User Repo", "Không tồn tại user");
                        callback.returnResult(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("!!! User Repo", "Lỗi: ", e);
                });
    }

    // UPDATE
    public void updateUser(User user) {

        db.collection(COLLECTION_NAME)
                .document(String.valueOf(user.getId()))
                .set(user)
                .addOnSuccessListener(v -> {
                    Log.d(">>> User Repo", "Đã cập nhật user");
                }).addOnFailureListener(e -> {
                    Log.e("!!! User Repo", "Lỗi: ", e);
                });
    }

    // DELETE
    public void deleteUser(long id) {

        db.collection(COLLECTION_NAME)
                .document(String.valueOf(id))
                .delete()
                .addOnSuccessListener(v -> {
                    Log.d(">>> User Repo", "Đã xóa user");
                })
                .addOnFailureListener(e -> {
                    Log.e("!!! User Repo", "Lỗi: ", e);
                });
    }
}
