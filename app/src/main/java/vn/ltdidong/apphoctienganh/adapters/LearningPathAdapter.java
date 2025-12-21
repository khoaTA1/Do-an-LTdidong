package vn.ltdidong.apphoctienganh.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.LearningPathStep;

/**
 * Adapter cho Learning Path Steps
 */
public class LearningPathAdapter extends RecyclerView.Adapter<LearningPathAdapter.ViewHolder> {
    
    private Context context;
    private List<LearningPathStep> steps;
    private OnStepClickListener listener;
    
    public interface OnStepClickListener {
        void onStepClick(LearningPathStep step);
    }
    
    public LearningPathAdapter(Context context, List<LearningPathStep> steps) {
        this.context = context;
        this.steps = steps;
    }
    
    public void setOnStepClickListener(OnStepClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_learning_path_step, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LearningPathStep step = steps.get(position);
        
        holder.tvStepNumber.setText("Step " + step.getStepNumber());
        holder.tvTitle.setText(step.getTitle());
        holder.tvSkill.setText(getSkillIcon(step.getSkillType()) + " " + step.getSkillType());
        holder.tvDifficulty.setText(step.getDifficulty());
        holder.tvTime.setText(step.getEstimatedMinutes() + " min");
        holder.tvReason.setText(step.getReason());
        
        // Styling based on difficulty
        int difficultyColor = getDifficultyColor(step.getDifficulty());
        holder.tvDifficulty.setTextColor(difficultyColor);
        
        // Completed status
        if (step.isCompleted()) {
            holder.btnStart.setText("✓ Completed");
            holder.btnStart.setEnabled(false);
            holder.itemView.setAlpha(0.6f);
        } else {
            holder.btnStart.setText("Start");
            holder.btnStart.setEnabled(true);
            holder.itemView.setAlpha(1.0f);
        }
        
        holder.btnStart.setOnClickListener(v -> {
            if (listener != null && !step.isCompleted()) {
                listener.onStepClick(step);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return steps.size();
    }
    
    private String getSkillIcon(String skill) {
        switch (skill) {
            case "LISTENING": return "👂";
            case "READING": return "📖";
            case "WRITING": return "✍️";
            case "SPEAKING": return "🗣️";
            default: return "📚";
        }
    }
    
    private int getDifficultyColor(String difficulty) {
        switch (difficulty) {
            case "EASY": return 0xFF4CAF50; // Green
            case "MEDIUM": return 0xFFFF9800; // Orange
            case "HARD": return 0xFFF44336; // Red
            default: return 0xFF757575; // Gray
        }
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStepNumber, tvTitle, tvSkill, tvDifficulty, tvTime, tvReason;
        Button btnStart;
        
        ViewHolder(View view) {
            super(view);
            tvStepNumber = view.findViewById(R.id.tvStepNumber);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvSkill = view.findViewById(R.id.tvSkill);
            tvDifficulty = view.findViewById(R.id.tvDifficulty);
            tvTime = view.findViewById(R.id.tvTime);
            tvReason = view.findViewById(R.id.tvReason);
            btnStart = view.findViewById(R.id.btnStart);
        }
    }
}
