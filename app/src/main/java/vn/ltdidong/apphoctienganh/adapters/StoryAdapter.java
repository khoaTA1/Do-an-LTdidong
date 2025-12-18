package vn.ltdidong.apphoctienganh.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.Story;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {
    
    private Context context;
    private List<Story> storyList;
    private OnStoryClickListener listener;
    
    public interface OnStoryClickListener {
        void onStoryClick(Story story);
    }
    
    public StoryAdapter(Context context, List<Story> storyList, OnStoryClickListener listener) {
        this.context = context;
        this.storyList = storyList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_story, parent, false);
        return new StoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        Story story = storyList.get(position);
        
        // Set icon based on category
        String icon = getIconForCategory(story.getCategory());
        holder.tvStoryIcon.setText(icon);
        
        holder.tvStoryTitle.setText(story.getTitle());
        holder.tvStoryDescription.setText(story.getDescription());
        holder.chipLevel.setText(story.getLevel());
        holder.tvDuration.setText("⏱️ " + story.getDuration() + " min");
        holder.tvChapters.setText("📖 " + story.getChaptersCount() + " chapters");
        
        // Set level chip color
        int chipColor = getLevelColor(story.getLevel());
        holder.chipLevel.setChipBackgroundColorResource(chipColor);
        
        // Show/hide lock icon
        holder.tvLockIcon.setVisibility(story.isLocked() ? View.VISIBLE : View.GONE);
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> listener.onStoryClick(story));
    }
    
    @Override
    public int getItemCount() {
        return storyList.size();
    }
    
    public void updateStories(List<Story> newStories) {
        this.storyList = newStories;
        notifyDataSetChanged();
    }
    
    private String getIconForCategory(String category) {
        switch (category) {
            case "Adventure": return "🗺️";
            case "Mystery": return "🔍";
            case "Fantasy": return "🐉";
            case "Romance": return "💕";
            case "Sci-Fi": return "🚀";
            case "Comedy": return "😄";
            default: return "📚";
        }
    }
    
    private int getLevelColor(String level) {
        switch (level) {
            case "Beginner": return R.color.level_beginner;
            case "Intermediate": return R.color.level_intermediate;
            case "Advanced": return R.color.level_advanced;
            default: return R.color.level_beginner;
        }
    }
    
    static class StoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvStoryIcon, tvStoryTitle, tvStoryDescription, tvDuration, tvChapters, tvLockIcon;
        Chip chipLevel;
        
        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStoryIcon = itemView.findViewById(R.id.tvStoryIcon);
            tvStoryTitle = itemView.findViewById(R.id.tvStoryTitle);
            tvStoryDescription = itemView.findViewById(R.id.tvStoryDescription);
            chipLevel = itemView.findViewById(R.id.chipLevel);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvChapters = itemView.findViewById(R.id.tvChapters);
            tvLockIcon = itemView.findViewById(R.id.tvLockIcon);
        }
    }
}
