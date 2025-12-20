package vn.ltdidong.apphoctienganh.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.managers.PersonalizedRecommendationEngine.Recommendation;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {
    
    private List<Recommendation> recommendations = new ArrayList<>();
    private OnRecommendationClickListener listener;
    
    public interface OnRecommendationClickListener {
        void onStartClick(Recommendation recommendation);
    }
    
    public void setOnRecommendationClickListener(OnRecommendationClickListener listener) {
        this.listener = listener;
    }
    
    public void setRecommendations(List<Recommendation> recommendations) {
        this.recommendations = recommendations != null ? recommendations : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_recommendation, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recommendation rec = recommendations.get(position);
        
        holder.tvTitle.setText(rec.title);
        holder.tvDescription.setText(rec.description);
        holder.tvDuration.setText("⏱ " + rec.estimatedMinutes + " phút");
        holder.tvDifficulty.setText("📚 " + getDifficultyText(rec.difficulty));
        holder.tvPriority.setText(String.valueOf(rec.priority));
        
        // Set priority color
        int color = getPriorityColor(rec.priority);
        holder.tvPriority.setBackgroundColor(color);
        
        holder.btnStart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStartClick(rec);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return recommendations.size();
    }
    
    private String getDifficultyText(String difficulty) {
        if (difficulty == null) return "";
        switch (difficulty) {
            case "EASY": return "Dễ";
            case "MEDIUM": return "Trung bình";
            case "HARD": return "Khó";
            default: return difficulty;
        }
    }
    
    private int getPriorityColor(int priority) {
        if (priority >= 5) return Color.parseColor("#F44336"); // Red
        if (priority >= 4) return Color.parseColor("#FF9800"); // Orange
        if (priority >= 3) return Color.parseColor("#FFC107"); // Amber
        if (priority >= 2) return Color.parseColor("#4CAF50"); // Green
        return Color.parseColor("#9E9E9E"); // Grey
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvDuration, tvDifficulty, tvPriority;
        Button btnStart;
        
        ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tv_title);
            tvDescription = view.findViewById(R.id.tv_description);
            tvDuration = view.findViewById(R.id.tv_duration);
            tvDifficulty = view.findViewById(R.id.tv_difficulty);
            tvPriority = view.findViewById(R.id.tv_priority);
            btnStart = view.findViewById(R.id.btn_start);
        }
    }
}
