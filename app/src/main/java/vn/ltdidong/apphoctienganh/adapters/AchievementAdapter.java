package vn.ltdidong.apphoctienganh.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.Achievement;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

    private Context context;
    private List<Achievement> achievementList;

    public AchievementAdapter(Context context, List<Achievement> achievementList) {
        this.context = context;
        this.achievementList = achievementList;
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievementList.get(position);
        
        holder.tvIcon.setText(achievement.getIcon());
        holder.tvTitle.setText(achievement.getTitle());
        holder.tvDescription.setText(achievement.getDescription());
        
        if (achievement.isUnlocked()) {
            // Achievement đã mở khóa
            holder.cardView.setAlpha(1.0f);
            holder.tvIcon.setAlpha(1.0f);
            holder.tvTitle.setAlpha(1.0f);
            holder.tvDescription.setAlpha(1.0f);
            holder.tvStatus.setText("✓ Đã đạt");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.primary));
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.cardView.setOnClickListener(null);
        } else {
            // Achievement chưa mở khóa
            holder.cardView.setAlpha(0.5f);
            holder.tvIcon.setAlpha(0.3f);
            holder.tvTitle.setAlpha(0.6f);
            holder.tvDescription.setAlpha(0.6f);
            holder.tvStatus.setVisibility(View.GONE);
            
            // Thêm click listener để hiển thị điều kiện
            holder.cardView.setOnClickListener(v -> showRequirementDialog(achievement));
        }
    }
    
    private void showRequirementDialog(Achievement achievement) {
        String message = "Điều kiện: " + achievement.getDescription() + "\n\n";
        
        if (achievement.getTitle().contains("Level") || 
            achievement.getTitle().contains("Người mới") ||
            achievement.getTitle().contains("Học viên") ||
            achievement.getTitle().contains("Chuyên gia") ||
            achievement.getTitle().contains("Bậc thầy")) {
            message += "Bạn cần đạt Level " + achievement.getRequirement() + " để mở khóa thành tích này.";
        } else if (achievement.getTitle().contains("XP")) {
            message += "Bạn cần tích lũy " + achievement.getRequirement() + " XP để mở khóa thành tích này.";
        } else if (achievement.getTitle().contains("Nhất quán") || 
                   achievement.getTitle().contains("Kiên trì") ||
                   achievement.getTitle().contains("Huyền thoại")) {
            message += "Bạn cần học " + achievement.getRequirement() + " ngày liên tiếp để mở khóa thành tích này.";
        } else {
            message += "Bạn cần hoàn thành " + achievement.getRequirement() + " bài học để mở khóa thành tích này.";
        }
        
        new AlertDialog.Builder(context)
            .setTitle(achievement.getIcon() + " " + achievement.getTitle())
            .setMessage(message)
            .setPositiveButton("Đã hiểu", null)
            .show();
    }

    @Override
    public int getItemCount() {
        return achievementList.size();
    }

    public static class AchievementViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvIcon;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvStatus;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvIcon = itemView.findViewById(R.id.tvAchievementIcon);
            tvTitle = itemView.findViewById(R.id.tvAchievementTitle);
            tvDescription = itemView.findViewById(R.id.tvAchievementDescription);
            tvStatus = itemView.findViewById(R.id.tvAchievementStatus);
        }
    }
}
