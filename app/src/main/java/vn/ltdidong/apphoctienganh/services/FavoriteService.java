package vn.ltdidong.apphoctienganh.services;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;
import vn.ltdidong.apphoctienganh.models.User;
import vn.ltdidong.apphoctienganh.models.WordEntry;

public class FavoriteService {

    private static FavoriteService instance;
    private DatabaseReference databaseReference;

    // TODO: Nhớ thay link database của bạn vào đây nếu chưa cấu hình trong google-services.json
    // Nếu project tạo ở server Singapore/Asia, bắt buộc phải có link này
    private static final String DATABASE_URL = "https://LINK_DATABASE_CUA_BAN_O_DAY";

    public static synchronized FavoriteService getInstance() {
        if (instance == null) {
            instance = new FavoriteService();
        }
        return instance;
    }

    private FavoriteService() {
        try {
            if (DATABASE_URL.contains("https")) {
                databaseReference = FirebaseDatabase.getInstance(DATABASE_URL).getReference("favorites");
            } else {
                databaseReference = FirebaseDatabase.getInstance().getReference("favorites");
            }
        } catch (Exception e) {
            Log.e("FavoriteService", "Lỗi khởi tạo DB: " + e.getMessage());
        }
    }

    public interface OnListCallback {
        void onResult(List<WordEntry> list);
        void onError(String message);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onFail(String msg);
    }

    // CẦN TRUYỀN CONTEXT VÀO ĐỂ LẤY USER TỪ SHAREDPREFERENCES
    public void getAllFavorites(Context context, OnListCallback callback) {
        if (databaseReference == null) {
            callback.onError("Lỗi kết nối Database");
            return;
        }

        // 1. Lấy thông tin User từ SharedPreferences (thay vì FirebaseAuth)
        User user = SharedPreferencesManager.getInstance(context).getUser();

        if (user == null || user.getEmail() == null) {
            callback.onError("Người dùng chưa đăng nhập (SharedPrefs empty)");
            return;
        }

        // 2. Tạo Key định danh (Vì email chứa dấu chấm . mà Firebase không cho phép làm key, nên thay bằng _)
        String userKey = user.getEmail().replace(".", "_").replace("@", "_");

        databaseReference.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<WordEntry> list = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        WordEntry word = data.getValue(WordEntry.class);
                        if (word != null) list.add(word);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                callback.onResult(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void addFavorite(Context context, WordEntry word, SimpleCallback callback) {
        if (databaseReference == null) return;

        User user = SharedPreferencesManager.getInstance(context).getUser();
        if (user == null) {
            callback.onFail("Chưa đăng nhập");
            return;
        }

        String userKey = user.getEmail().replace(".", "_").replace("@", "_");

        // Tạo key cho từ vựng (bỏ ký tự đặc biệt)
        String wordKey = word.getWord().trim().replaceAll("[.#$\\[\\]]", "");

        databaseReference.child(userKey).child(wordKey).setValue(word)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFail(e.getMessage()));
    }
}