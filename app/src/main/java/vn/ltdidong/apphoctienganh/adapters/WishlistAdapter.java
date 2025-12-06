package vn.ltdidong.apphoctienganh.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.WordEntry; // Import đúng model

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    // CHÚ Ý: Đổi List<String> thành List<WordEntry> để hiển thị được nghĩa
    // Nếu Activity của bạn đang truyền List<String>, bạn cần sửa Activity hoặc giữ nguyên Logic cũ
    // Nhưng TỐT NHẤT là nên truyền List<WordEntry> xuống Adapter.

    // Dưới đây mình giữ logic Adapter nhận List<String> tên từ (như code cũ của bạn)
    // Nhưng mình khuyên bạn nên đổi sang List<WordEntry> để hiển thị được nghĩa tiếng Việt.

    private List<String> listWords;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String word);
        void onDeleteClick(String word, int position);
    }

    public WishlistAdapter(List<String> listWords, OnItemClickListener listener) {
        this.listWords = listWords;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wishlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String word = listWords.get(position);

        // Hiển thị từ tiếng Anh
        holder.tvWord.setText(word);

        // Vì hiện tại Adapter này chỉ có danh sách TÊN (List<String>),
        // nên ta không hiển thị nghĩa ở đây được (trừ khi sửa Activity truyền object vào).
        // Tạm thời set text mặc định hoặc ẩn đi.
        holder.tvMeaning.setText("Click để xem chi tiết");

        holder.itemView.setOnClickListener(v -> listener.onItemClick(word));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(word, position));
    }

    @Override
    public int getItemCount() {
        return listWords.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord, tvMeaning;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Đảm bảo ID trong layout item_wishlist.xml đúng tên nhé
            tvWord = itemView.findViewById(R.id.tv_word);
            tvMeaning = itemView.findViewById(R.id.tv_meaning);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}