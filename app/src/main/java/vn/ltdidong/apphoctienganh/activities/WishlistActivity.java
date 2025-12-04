package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.WishlistAdapter;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;
import vn.ltdidong.apphoctienganh.models.User;
// Lưu ý: Import WordEntry của bạn để chuyển màn hình
import vn.ltdidong.apphoctienganh.models.WordEntry;

public class WishlistActivity extends AppCompatActivity {

    private RecyclerView rvWishlist;
    private WishlistAdapter adapter;
    private List<String> favoriteList;
    private FirebaseFirestore db;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        // 1. Khởi tạo
        db = FirebaseFirestore.getInstance();
        rvWishlist = findViewById(R.id.rvWishlist);
        ImageButton btnBack = findViewById(R.id.btnBackWishlist);

        favoriteList = new ArrayList<>();

        // Cấu hình RecyclerView
        rvWishlist.setLayoutManager(new LinearLayoutManager(this));

        // Xử lý sự kiện Adapter
        adapter = new WishlistAdapter(favoriteList, new WishlistAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String word) {
                // KHI CLICK VÀO TỪ:
                // Lưu ý: Vì ta chỉ lưu mỗi cái Tên từ trên Firestore,
                // nên khi click vào đây, ta cần tạo một WordEntry giả chỉ có tên
                // và chuyển sang DetailActivity.
                // DetailActivity cần code gọi lại API nếu thiếu dữ liệu (hoặc bạn phải sửa logic lưu cả object JSON).

                WordEntry entry = new WordEntry();
                entry.setWord(word);

                Intent intent = new Intent(WishlistActivity.this, DetailActivity.class);
                intent.putExtra("WORD_DATA", entry);
                startActivity(intent);

                // Gợi ý: Tại DetailActivity, bạn nên thêm logic check:
                // Nếu wordEntry.getMeanings() == null thì gọi lại API Dictionary để lấy nghĩa.
            }

            @Override
            public void onDeleteClick(String word, int position) {
                deleteWordFromFirestore(word, position);
            }
        });

        rvWishlist.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        // 2. Lấy UserID và tải dữ liệu
        User user = SharedPreferencesManager.getInstance(this).getUser();
        if (user != null) {
            userID = String.valueOf(user.getId());
            loadWishlistData();
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadWishlistData() {
        // Đường dẫn: users -> [userID] -> favorites
        db.collection("users").document(userID).collection("favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    favoriteList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Lấy ID của document chính là từ vựng (ví dụ: "apple")
                        favoriteList.add(doc.getId());
                    }
                    adapter.notifyDataSetChanged();

                    if (favoriteList.isEmpty()) {
                        Toast.makeText(this, "Danh sách trống", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Wishlist", "Error", e);
                });
    }

    private void deleteWordFromFirestore(String word, int position) {
        db.collection("users").document(userID)
                .collection("favorites").document(word)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Xóa thành công trên Server thì mới xóa trên Giao diện
                    favoriteList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, favoriteList.size());
                    Toast.makeText(this, "Đã xóa " + word, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Xóa thất bại!", Toast.LENGTH_SHORT).show());
    }
}