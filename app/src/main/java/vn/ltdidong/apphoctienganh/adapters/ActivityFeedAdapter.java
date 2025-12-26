package vn.ltdidong.apphoctienganh.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.ActivityItem;

/**
 * Adapter for Activity Feed
 */
public class ActivityFeedAdapter extends RecyclerView.Adapter<ActivityFeedAdapter.ViewHolder> {
    
    private Context context;
    private List<ActivityItem> activities;
    
    public ActivityFeedAdapter(Context context, List<ActivityItem> activities) {
        this.context = context;
        this.activities = activities;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_activity_feed, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityItem activity = activities.get(position);
        
        // Set user info
        holder.tvUserName.setText(activity.getUserName());
        holder.tvTimeAgo.setText(activity.getTimeAgo());
        
        // Set activity icon
        holder.tvActivityIcon.setText(activity.getActivityIcon());
        
        // Set activity content
        holder.tvTitle.setText(activity.getTitle());
        holder.tvDescription.setText(activity.getDescription());
        
        // Set XP if available
        if (activity.getXpGained() > 0) {
            holder.tvXp.setVisibility(View.VISIBLE);
            holder.tvXp.setText("+" + activity.getXpGained() + " XP");
        } else {
            holder.tvXp.setVisibility(View.GONE);
        }
        
        // Load user avatar
        if (activity.getUserAvatar() != null && !activity.getUserAvatar().isEmpty()) {
            Glide.with(context)
                .load(activity.getUserAvatar())
                .placeholder(R.drawable.default_avatar)
                .circleCrop()
                .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.default_avatar);
        }
        
        // Set background color based on activity type
        int colorResId;
        switch (activity.getActivityType()) {
            case "achievement":
                colorResId = R.color.activity_achievement;
                break;
            case "level_up":
                colorResId = R.color.activity_level_up;
                break;
            case "streak":
                colorResId = R.color.activity_streak;
                break;
            case "challenge":
                colorResId = R.color.activity_challenge;
                break;
            default:
                colorResId = R.color.activity_default;
                break;
        }
        holder.viewColorIndicator.setBackgroundResource(colorResId);
    }
    
    @Override
    public int getItemCount() {
        return activities.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvUserName;
        TextView tvTimeAgo;
        TextView tvActivityIcon;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvXp;
        View viewColorIndicator;
        
        ViewHolder(View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvTimeAgo = itemView.findViewById(R.id.tvTimeAgo);
            tvActivityIcon = itemView.findViewById(R.id.tvActivityIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvXp = itemView.findViewById(R.id.tvXp);
            viewColorIndicator = itemView.findViewById(R.id.viewColorIndicator);
        }
    }
}
