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

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    private List<String> wordList;
    private OnItemClickListener listener;

    // Interface để Activity xử lý sự kiện click
    public interface OnItemClickListener {
        void onItemClick(String word);
        void onDeleteClick(String word, int position);
    }

    public WishlistAdapter(List<String> wordList, OnItemClickListener listener) {
        this.wordList = wordList;
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
        String word = wordList.get(position);
        holder.tvWord.setText(word);

        // Sự kiện click vào item (để xem chi tiết)
        holder.itemView.setOnClickListener(v -> listener.onItemClick(word));

        // Sự kiện click nút xóa
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(word, position));
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tvWordItem);
            btnDelete = itemView.findViewById(R.id.btnDeleteItem);
        }
    }
}