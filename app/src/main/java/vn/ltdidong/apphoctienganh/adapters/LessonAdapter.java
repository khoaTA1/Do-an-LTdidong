package vn.ltdidong.apphoctienganh.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.ListeningLesson;
import vn.ltdidong.apphoctienganh.models.UserProgress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter cho RecyclerView hiển thị danh sách bài học listening
 * Quản lý việc bind data vào các ViewHolder
 */
public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {
    
    private List<ListeningLesson> lessons = new ArrayList<>();
    private Map<Integer, UserProgress> progressMap = new HashMap<>();
    private OnLessonClickListener listener;
    
    /**
     * Interface để handle click event
     */
    public interface OnLessonClickListener {
        void onLessonClick(ListeningLesson lesson);
    }
    
    /**
     * Constructor
     * @param listener Listener để handle click events
     */
    public LessonAdapter(OnLessonClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout cho mỗi item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson_card, parent, false);
        return new LessonViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        // Bind data cho ViewHolder
        ListeningLesson lesson = lessons.get(position);
        UserProgress progress = progressMap.get(lesson.getId());
        holder.bind(lesson, progress);
    }
    
    @Override
    public int getItemCount() {
        return lessons.size();
    }
    
    /**
     * Cập nhật danh sách bài học
     * @param newLessons Danh sách bài học mới
     */
    public void setLessons(List<ListeningLesson> newLessons) {
        this.lessons = newLessons;
        notifyDataSetChanged();
    }
    
    /**
     * Cập nhật tiến độ học tập
     * @param progressList Danh sách tiến độ
     */
    public void setProgress(List<UserProgress> progressList) {
        progressMap.clear();
        if (progressList != null) {
            for (UserProgress progress : progressList) {
                progressMap.put(progress.getLessonId(), progress);
            }
        }
        notifyDataSetChanged();
    }
    
    /**
     * ViewHolder cho mỗi item trong RecyclerView
     */
    class LessonViewHolder extends RecyclerView.ViewHolder {
        
        private ImageView ivLessonImage;
        private TextView tvLessonTitle;
        private TextView tvLessonDescription;
        private Chip chipDifficulty;
        private TextView tvDuration;
        private TextView tvQuestionCount;
        private TextView tvStatus;
        private TextView tvBestScore;
        
        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // Khởi tạo các views
            ivLessonImage = itemView.findViewById(R.id.ivLessonImage);
            tvLessonTitle = itemView.findViewById(R.id.tvLessonTitle);
            tvLessonDescription = itemView.findViewById(R.id.tvLessonDescription);
            chipDifficulty = itemView.findViewById(R.id.chipDifficulty);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvQuestionCount = itemView.findViewById(R.id.tvQuestionCount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvBestScore = itemView.findViewById(R.id.tvBestScore);
        }
        
        /**
         * Bind data vào views
         * @param lesson Bài học cần hiển thị
         * @param progress Tiến độ của bài học (có thể null)
         */
        public void bind(ListeningLesson lesson, UserProgress progress) {
            // Set lesson info
            tvLessonTitle.setText(lesson.getTitle());
            tvLessonDescription.setText(lesson.getDescription());
            tvDuration.setText(lesson.getFormattedDuration());
            tvQuestionCount.setText(lesson.getQuestionCount() + " câu hỏi");
            
            // Set difficulty chip
            chipDifficulty.setText(lesson.getDifficulty());
            int difficultyColor = getDifficultyColor(lesson.getDifficulty());
            chipDifficulty.setChipBackgroundColorResource(difficultyColor);
            
            // Set progress info nếu có
            if (progress != null) {
                tvStatus.setVisibility(View.VISIBLE);
                tvStatus.setText(getStatusText(progress.getStatus()));
                tvStatus.setBackgroundColor(itemView.getContext()
                        .getColor(getStatusColor(progress.getStatus())));
                
                if (progress.getStatus().equals("COMPLETED")) {
                    tvBestScore.setVisibility(View.VISIBLE);
                    tvBestScore.setText(String.format("Tốt nhất: %.0f%%", progress.getBestScore()));
                } else {
                    tvBestScore.setVisibility(View.GONE);
                }
            } else {
                tvStatus.setVisibility(View.GONE);
                tvBestScore.setVisibility(View.GONE);
            }
            
            // Handle click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLessonClick(lesson);
                }
            });
        }
        
        /**
         * Lấy màu theo độ khó
         */
        private int getDifficultyColor(String difficulty) {
            switch (difficulty.toUpperCase()) {
                case "EASY":
                    return R.color.difficulty_easy;
                case "MEDIUM":
                    return R.color.difficulty_medium;
                case "HARD":
                    return R.color.difficulty_hard;
                default:
                    return android.R.color.holo_blue_light;
            }
        }
        
        /**
         * Lấy text hiển thị cho status
         */
        private String getStatusText(String status) {
            switch (status) {
                case "COMPLETED":
                    return "Hoàn thành";
                case "IN_PROGRESS":
                    return "Đang học";
                default:
                    return "Chưa bắt đầu";
            }
        }
        
        /**
         * Lấy màu cho status badge
         */
        private int getStatusColor(String status) {
            switch (status) {
                case "COMPLETED":
                    return android.R.color.holo_green_light;
                case "IN_PROGRESS":
                    return android.R.color.holo_orange_light;
                default:
                    return android.R.color.darker_gray;
            }
        }
    }
}
