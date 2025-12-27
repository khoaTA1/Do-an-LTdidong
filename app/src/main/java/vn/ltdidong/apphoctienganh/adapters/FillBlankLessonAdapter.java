package vn.ltdidong.apphoctienganh.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.FillBlankLesson;

public class FillBlankLessonAdapter extends RecyclerView.Adapter<FillBlankLessonAdapter.ViewHolder> {

    private List<FillBlankLesson> lessons = new ArrayList<>();
    private OnLessonClickListener listener;

    public interface OnLessonClickListener {
        void onLessonClick(FillBlankLesson lesson);
    }

    public FillBlankLessonAdapter(OnLessonClickListener listener) {
        this.listener = listener;
    }

    public void setLessons(List<FillBlankLesson> lessons) {
        this.lessons = lessons;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fill_blank_lesson, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FillBlankLesson lesson = lessons.get(position);
        holder.bind(lesson);
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLessonTitle;
        TextView tvQuestionCount;

        ViewHolder(View itemView) {
            super(itemView);
            tvLessonTitle = itemView.findViewById(R.id.tvLessonTitle);
            tvQuestionCount = itemView.findViewById(R.id.tvQuestionCount);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onLessonClick(lessons.get(position));
                }
            });
        }

        void bind(FillBlankLesson lesson) {
            tvLessonTitle.setText(lesson.getTitle());
            tvQuestionCount.setText(lesson.getQuestionCount() + " câu hỏi");
        }
    }
}
