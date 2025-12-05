package vn.ltdidong.apphoctienganh.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.WordEntry;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private List<WordEntry> wordList;
    private final OnItemAction listener;

    // Interface để Activity xử lý sự kiện
    public interface OnItemAction {
        void onDelete(WordEntry word, int position);
    }

    public FavoriteAdapter(List<WordEntry> wordList, OnItemAction listener) {
        this.wordList = wordList;
        this.listener = listener;
    }

    public void updateList(List<WordEntry> newList) {
        this.wordList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_word, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WordEntry word = wordList.get(position);
        holder.tvWord.setText(word.getWord());

        // Sử dụng hàm Helper getFirstDefinition() chúng ta đã viết
        holder.tvMeaning.setText(word.getFirstDefinition());

        // Bắt sự kiện xóa
        holder.btnDelete.setOnClickListener(v -> {
            listener.onDelete(word, position);
        });
    }

    @Override
    public int getItemCount() {
        return wordList == null ? 0 : wordList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord, tvMeaning;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tv_word_item);
            tvMeaning = itemView.findViewById(R.id.tv_meaning_item);
            btnDelete = itemView.findViewById(R.id.btn_delete_item);
        }
    }
}