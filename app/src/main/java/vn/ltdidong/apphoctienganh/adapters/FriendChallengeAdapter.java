package vn.ltdidong.apphoctienganh.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.FriendChallenge;

/**
 * Adapter for Friend Challenges List
 */
public class FriendChallengeAdapter extends RecyclerView.Adapter<FriendChallengeAdapter.ViewHolder> {
    
    private Context context;
    private List<FriendChallenge> challenges;
    private String currentUserId;
    private OnChallengeActionListener listener;
    
    public interface OnChallengeActionListener {
        void onAcceptChallenge(FriendChallenge challenge);
        void onDeclineChallenge(FriendChallenge challenge);
        void onStartChallenge(FriendChallenge challenge);
        void onViewResults(FriendChallenge challenge);
    }
    
    public FriendChallengeAdapter(Context context, List<FriendChallenge> challenges, String currentUserId) {
        this.context = context;
        this.challenges = challenges;
        this.currentUserId = currentUserId;
    }
    
    public void setOnChallengeActionListener(OnChallengeActionListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_challenge, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendChallenge challenge = challenges.get(position);
        
        // Determine if current user is challenger or opponent
        boolean isChallenger = currentUserId.equals(challenge.getChallengerId());
        String opponentName = isChallenger ? challenge.getOpponentName() : challenge.getChallengerName();
        String opponentAvatar = isChallenger ? challenge.getOpponentAvatar() : challenge.getChallengerAvatar();
        
        // Set opponent info
        holder.tvOpponentName.setText(opponentName);
        if (opponentAvatar != null && !opponentAvatar.isEmpty()) {
            Glide.with(context)
                .load(opponentAvatar)
                .placeholder(R.drawable.default_avatar)
                .circleCrop()
                .into(holder.imgOpponentAvatar);
        } else {
            holder.imgOpponentAvatar.setImageResource(R.drawable.default_avatar);
        }
        
        // Set challenge info
        holder.tvChallengeIcon.setText(challenge.getTypeIcon());
        holder.tvChallengeTitle.setText(challenge.getChallengeTitle());
        holder.tvChallengeType.setText(challenge.getChallengeType().toUpperCase());
        
        // Set status and scores
        String status = challenge.getStatus();
        holder.tvStatus.setText(status.toUpperCase());
        
        // Set status color
        int statusColor;
        switch (status) {
            case "pending":
                statusColor = R.color.warning;
                break;
            case "accepted":
            case "in_progress":
                statusColor = R.color.info;
                break;
            case "completed":
                statusColor = R.color.success;
                break;
            case "expired":
            case "declined":
                statusColor = R.color.error;
                break;
            default:
                statusColor = R.color.text_secondary;
                break;
        }
        holder.tvStatus.setTextColor(context.getResources().getColor(statusColor));
        
        // Show scores if available
        if (challenge.isChallengerCompleted() || challenge.isOpponentCompleted()) {
            int yourScore = isChallenger ? challenge.getChallengerScore() : challenge.getOpponentScore();
            int theirScore = isChallenger ? challenge.getOpponentScore() : challenge.getChallengerScore();
            
            holder.tvScores.setVisibility(View.VISIBLE);
            holder.tvScores.setText("You: " + yourScore + " | " + opponentName + ": " + theirScore);
        } else {
            holder.tvScores.setVisibility(View.GONE);
        }
        
        // Show time remaining for pending/accepted challenges
        if (status.equals("pending") || status.equals("accepted")) {
            int hoursLeft = challenge.getHoursRemaining();
            holder.tvTimeRemaining.setVisibility(View.VISIBLE);
            holder.tvTimeRemaining.setText("⏰ " + hoursLeft + "h remaining");
        } else {
            holder.tvTimeRemaining.setVisibility(View.GONE);
        }
        
        // Show appropriate buttons based on status and user role
        holder.btnAccept.setVisibility(View.GONE);
        holder.btnDecline.setVisibility(View.GONE);
        holder.btnStart.setVisibility(View.GONE);
        holder.btnViewResults.setVisibility(View.GONE);
        
        if (status.equals("pending") && !isChallenger) {
            // Opponent can accept/decline
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnDecline.setVisibility(View.VISIBLE);
        } else if (status.equals("accepted") || status.equals("in_progress")) {
            // Can start if not completed yet
            boolean userCompleted = isChallenger ? challenge.isChallengerCompleted() : challenge.isOpponentCompleted();
            if (!userCompleted) {
                holder.btnStart.setVisibility(View.VISIBLE);
            }
        } else if (status.equals("completed")) {
            holder.btnViewResults.setVisibility(View.VISIBLE);
        }
        
        // Set button listeners
        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAcceptChallenge(challenge);
            }
        });
        
        holder.btnDecline.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeclineChallenge(challenge);
            }
        });
        
        holder.btnStart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStartChallenge(challenge);
            }
        });
        
        holder.btnViewResults.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewResults(challenge);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return challenges.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgOpponentAvatar;
        TextView tvOpponentName;
        TextView tvChallengeIcon;
        TextView tvChallengeTitle;
        TextView tvChallengeType;
        TextView tvStatus;
        TextView tvScores;
        TextView tvTimeRemaining;
        Button btnAccept;
        Button btnDecline;
        Button btnStart;
        Button btnViewResults;
        
        ViewHolder(View itemView) {
            super(itemView);
            imgOpponentAvatar = itemView.findViewById(R.id.imgOpponentAvatar);
            tvOpponentName = itemView.findViewById(R.id.tvOpponentName);
            tvChallengeIcon = itemView.findViewById(R.id.tvChallengeIcon);
            tvChallengeTitle = itemView.findViewById(R.id.tvChallengeTitle);
            tvChallengeType = itemView.findViewById(R.id.tvChallengeType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvScores = itemView.findViewById(R.id.tvScores);
            tvTimeRemaining = itemView.findViewById(R.id.tvTimeRemaining);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
            btnStart = itemView.findViewById(R.id.btnStart);
            btnViewResults = itemView.findViewById(R.id.btnViewResults);
        }
    }
}
