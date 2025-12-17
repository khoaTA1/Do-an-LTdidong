package vn.ltdidong.apphoctienganh.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.ChallengeItem;

/**
 * Adapter cho RecyclerView hiển thị danh sách challenges
 */
public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ViewHolder> {

    private List<ChallengeItem> challenges;
    private OnChallengeClickListener listener;

    public interface OnChallengeClickListener {
        void onChallengeClick(ChallengeItem item);
    }

    public ChallengeAdapter(List<ChallengeItem> challenges, OnChallengeClickListener listener) {
        this.challenges = challenges;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_challenge, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChallengeItem item = challenges.get(position);
        
        holder.tvTitle.setText(item.getTitle());
        holder.tvDescription.setText(item.getDescription());
        holder.tvXP.setText("+" + item.getXpReward() + " XP");
        
        // Set icon based on type
        int iconRes = getIconForType(item.getType());
        holder.ivIcon.setImageResource(iconRes);
        
        // Show check mark if completed
        if (item.isCompleted()) {
            holder.ivCheck.setVisibility(View.VISIBLE);
            holder.itemView.setAlpha(0.6f);
        } else {
            holder.ivCheck.setVisibility(View.GONE);
            holder.itemView.setAlpha(1.0f);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (!item.isCompleted() && listener != null) {
                listener.onChallengeClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    private int getIconForType(String type) {
        switch (type) {
            case "reading":
                return R.drawable.ic_reading;
            case "writing":
                return R.drawable.ic_writing;
            case "listening":
                return R.drawable.ic_listening;
            case "speaking":
                return R.drawable.ic_speaking;
            case "vocabulary":
                return R.drawable.ic_vocabulary;
            case "flashcard":
                return R.drawable.ic_flashcard;
            case "quiz":
                return R.drawable.ic_quiz;
            default:
                return R.drawable.ic_challenge;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvXP;
        ImageView ivCheck;

        ViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_challenge_icon);
            tvTitle = itemView.findViewById(R.id.tv_challenge_title);
            tvDescription = itemView.findViewById(R.id.tv_challenge_description);
            tvXP = itemView.findViewById(R.id.tv_challenge_xp);
            ivCheck = itemView.findViewById(R.id.iv_check);
        }
    }
}
