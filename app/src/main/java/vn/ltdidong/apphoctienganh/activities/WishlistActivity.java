package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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
import vn.ltdidong.apphoctienganh.models.WordEntry;

public class WishlistActivity extends AppCompatActivity {

    private RecyclerView rvWishlist;
    private WishlistAdapter adapter;
    private List<String> favoriteListNames; // Dùng cho Adapter (hiển thị tên)
    private ArrayList<WordEntry> flashcardDataList; // Dùng gửi sang Flashcard
    private FirebaseFirestore db;
    private String userID;
    private Button btnStartFlashcard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        db = FirebaseFirestore.getInstance();
        rvWishlist = findViewById(R.id.rvWishlist);
        ImageButton btnBack = findViewById(R.id.btnBackWishlist);
        btnStartFlashcard = findViewById(R.id.btnStartFlashcard);

        favoriteListNames = new ArrayList<>();
        flashcardDataList = new ArrayList<>();

        rvWishlist.setLayoutManager(new LinearLayoutManager(this));

        // Setup Adapter
        adapter = new WishlistAdapter(favoriteListNames, new WishlistAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String word) {
                WordEntry selectedEntry = findEntryByWord(word);
                if (selectedEntry == null) {
                    selectedEntry = new WordEntry();
                    selectedEntry.setWord(word);
                }
                Intent intent = new Intent(WishlistActivity.this, DetailActivity.class);
                intent.putExtra("WORD_DATA", selectedEntry);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(String word, int position) {
                deleteWordFromFirestore(word, position);
            }
        });

        rvWishlist.setAdapter(adapter);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // --- SỰ KIỆN NÚT ÔN TẬP ---
        if (btnStartFlashcard != null) {
            btnStartFlashcard.setOnClickListener(v -> {
                if (flashcardDataList == null || flashcardDataList.isEmpty()) {
                    Toast.makeText(this, "Danh sách trống, hãy thêm từ vựng!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Gửi list object sang FlashcardActivity
                Intent intent = new Intent(WishlistActivity.this, FlashcardActivity.class);
                intent.putExtra("FLASHCARD_LIST", flashcardDataList);
                startActivity(intent);
            });
        }

        // Lấy User và Load Data
        User user = SharedPreferencesManager.getInstance(this).getUser();
        if (user != null) {
            userID = String.valueOf(user.getId());
            loadWishlistData();
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private WordEntry findEntryByWord(String word) {
        for (WordEntry entry : flashcardDataList) {
            if (entry.getWord().equalsIgnoreCase(word)) return entry;
        }
        return null;
    }

    private void loadWishlistData() {
        db.collection("users").document(userID).collection("favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    favoriteListNames.clear();
                    flashcardDataList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        favoriteListNames.add(doc.getId());

                        WordEntry entry = doc.toObject(WordEntry.class);
                        if (entry != null) {
                            // Đảm bảo có tên từ vựng
                            if (entry.getWord() == null || entry.getWord().isEmpty()) {
                                entry.setWord(doc.getId());
                            }
                            flashcardDataList.add(entry);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Wishlist", "Lỗi tải data: " + e.getMessage()));
    }

    private void deleteWordFromFirestore(String word, int position) {
        db.collection("users").document(userID)
                .collection("favorites").document(word)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (position >= 0 && position < favoriteListNames.size()) {
                        favoriteListNames.remove(position);

                        // Xóa trong list data
                        for (int i = 0; i < flashcardDataList.size(); i++) {
                            if (flashcardDataList.get(i).getWord().equals(word)) {
                                flashcardDataList.remove(i);
                                break;
                            }
                        }
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, favoriteListNames.size());
                    }
                    Toast.makeText(this, "Đã xóa " + word, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Xóa thất bại!", Toast.LENGTH_SHORT).show());
    }
}