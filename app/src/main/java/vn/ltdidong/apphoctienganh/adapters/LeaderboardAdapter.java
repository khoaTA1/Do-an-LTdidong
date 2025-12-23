package vn.ltdidong.apphoctienganh.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.LeaderboardUser;

/**
 * Adapter cho Leaderboard
 */
public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    
    private Context context;
    private List<LeaderboardUser> users;
    
    public LeaderboardAdapter(Context context, List<LeaderboardUser> users) {
        this.context = context;
        this.users = users;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardUser user = users.get(position);
        
        // Rank with medal for top 3
        String rankText;
        if (user.getRank() == 1) {
            rankText = "🥇";
        } else if (user.getRank() == 2) {
            rankText = "🥈";
        } else if (user.getRank() == 3) {
            rankText = "🥉";
        } else {
            rankText = String.valueOf(user.getRank());
        }
        holder.tvRank.setText(rankText);
        
        holder.tvUsername.setText(user.getUsername());
        holder.tvLevel.setText("Lv " + user.getLevel());
        holder.tvXP.setText(formatXP(user.getTotalXP()) + " XP");
        
        // Highlight current user
        if (user.isCurrentUser()) {
            holder.itemView.setBackgroundColor(0x2000BCD4); // Light cyan
        } else {
            holder.itemView.setBackgroundColor(0x00FFFFFF); // Transparent
        }
        
        // Show friend indicator
        if (user.isFriend()) {
            holder.tvUsername.append(" 👥");
        }
    }
    
    @Override
    public int getItemCount() {
        return users.size();
    }
    
    private String formatXP(long xp) {
        if (xp >= 1000000) {
            return String.format("%.1fM", xp / 1000000.0);
        } else if (xp >= 1000) {
            return String.format("%.1fK", xp / 1000.0);
        } else {
            return String.valueOf(xp);
        }
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvUsername, tvLevel, tvXP;
        
        ViewHolder(View view) {
            super(view);
            tvRank = view.findViewById(R.id.tvRank);
            tvUsername = view.findViewById(R.id.tvUsername);
            tvLevel = view.findViewById(R.id.tvLevel);
            tvXP = view.findViewById(R.id.tvXP);
        }
    }
}
