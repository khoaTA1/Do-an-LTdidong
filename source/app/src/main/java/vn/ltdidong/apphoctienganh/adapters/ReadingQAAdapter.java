package vn.ltdidong.apphoctienganh.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.QuestionAnswer;

public class ReadingQAAdapter extends RecyclerView.Adapter<ReadingQAAdapter.QuestionViewHolder> {
    private List<QuestionAnswer> questionList;

    public ReadingQAAdapter(List<QuestionAnswer> questionList) {
        this.questionList = questionList;
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion;
        RadioGroup rgOptions;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.Question);
            rgOptions = itemView.findViewById(R.id.rgOptions);
        }
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reading_qa_sublayout, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        QuestionAnswer item = questionList.get(position);

        holder.tvQuestion.setText(item.getQuestion());

        // Clear RadioButtons trước khi thêm mới
        holder.rgOptions.removeAllViews();

        // Tạo RadioButtons từ Map<Integer, String>
        for (Map.Entry<Integer, String> entry : item.getAnswers().entrySet()) {
            int key = entry.getKey();         // số thứ tự đáp án
            String answerText = entry.getValue();

            RadioButton rb = new RadioButton(holder.itemView.getContext());
            rb.setText(answerText);
            rb.setId(View.generateViewId());

            // Nếu người dùng đã chọn -> đánh dấu lại
            if (key == item.getUserAnswer()) {
                rb.setChecked(true);
            }

            // Lưu lại đáp án người dùng chọn
            rb.setOnClickListener(v -> item.setUserAnswer(key));

            holder.rgOptions.addView(rb);
        }
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }
}
