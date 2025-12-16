package vn.ltdidong.apphoctienganh.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.MMCard;

public class MemoryMatchCardAdapter extends RecyclerView.Adapter<MemoryMatchCardAdapter.CardViewHolder> {

    private List<MMCard> cards;
    private OnCardClickListener listener;
    private int mode;

    public interface OnCardClickListener {
        void onCardClick(MMCard card, int position);
    }

    public MemoryMatchCardAdapter(List<MMCard> cards, OnCardClickListener listener, int mode) {
        this.cards = cards;
        this.listener = listener;
        this.mode = mode;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvWord, tvWordRevr;
        ImageView cardBack;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvWord = itemView.findViewById(R.id.tvWord);
            tvWordRevr = itemView.findViewById(R.id.tvWordReverse);
            cardBack = itemView.findViewById(R.id.cardBack);
        }
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card_memory_match, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        MMCard card = cards.get(position);

        holder.tvWord.setText(card.getText());

        if (mode == 2) {
            holder.tvWordRevr.setText(card.getText());
        }

        if (card.isMatched()) {
            // Thẻ đã ghép đúng
            holder.tvWord.setVisibility(View.VISIBLE);

            if (mode == 2) {
                holder.tvWordRevr.setVisibility(View.VISIBLE);
            }

            holder.cardBack.setVisibility(View.INVISIBLE);
            holder.cardView.setAlpha(0.6f);
            holder.cardView.setClickable(false);

        } else {

            // Trạng thái mặt thẻ
            if (card.isFaceUp()) {
                holder.tvWord.setVisibility(View.VISIBLE);

                if (mode == 2) {
                    holder.tvWordRevr.setVisibility(View.VISIBLE);
                }

                holder.cardBack.setVisibility(View.INVISIBLE);
            } else {
                holder.tvWord.setVisibility(View.INVISIBLE);

                if (mode == 2) {
                    holder.tvWordRevr.setVisibility(View.INVISIBLE);
                }

                holder.cardBack.setVisibility(View.VISIBLE);
            }

            holder.cardView.setAlpha(1f);
            holder.cardView.setClickable(true);
        }

        holder.cardView.setOnClickListener(v -> {
            if (!card.isMatched() && !card.isFaceUp()) {
                flipCard(holder);
                listener.onCardClick(card, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    /**
     * Hiệu ứng lật thẻ
     */
    private void flipCard(CardViewHolder holder) {

        holder.cardView.animate()
                .rotationY(90f)
                .setDuration(150)
                .withEndAction(() -> {

                    if (holder.cardBack.getVisibility() == View.VISIBLE) {
                        holder.cardBack.setVisibility(View.INVISIBLE);
                        holder.tvWord.setVisibility(View.VISIBLE);
                    } else {
                        holder.cardBack.setVisibility(View.VISIBLE);
                        holder.tvWord.setVisibility(View.INVISIBLE);
                    }

                    holder.cardView.setRotationY(-90f);

                    holder.cardView.animate()
                            .rotationY(0f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }
}

