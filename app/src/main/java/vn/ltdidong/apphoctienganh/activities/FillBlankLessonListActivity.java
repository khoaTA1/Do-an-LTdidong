package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.FillBlankLessonAdapter;
import vn.ltdidong.apphoctienganh.models.FillBlankLesson;

/**
 * Activity hiển thị danh sách bài học Fill Blank
 * User chọn bài học từ danh sách này
 */
public class FillBlankLessonListActivity extends AppCompatActivity {

    private static final String TAG = "FillBlankLessonList";
    private static final String COLLECTION_LESSONS = "fill_blank_lesson_listening";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private FillBlankLessonAdapter adapter;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_blank_lesson_list);

        db = FirebaseFirestore.getInstance();

        initViews();
        loadLessonsFromFirebase();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewLessons);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Setup adapter
        adapter = new FillBlankLessonAdapter(lesson -> {
            // When user clicks on a lesson, open FillBlankActivity
            Intent intent = new Intent(FillBlankLessonListActivity.this, FillBlankActivity.class);
            intent.putExtra("lesson_id", lesson.getId());
            intent.putExtra("lesson_title", lesson.getTitle());
            // Không cần truyền audio_url vì mỗi câu hỏi có audioUrl riêng
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }

    private void loadLessonsFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        db.collection(COLLECTION_LESSONS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<FillBlankLesson> lessons = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        FillBlankLesson lesson = new FillBlankLesson();
                        lesson.setId(doc.getId());
                        lesson.setTitle(doc.getString("title"));
                        lesson.setQuestionCount(0); // Default value

                        lessons.add(lesson); // Add immediately to list
                        
                        // Count questions in subcollection asynchronously
                        countQuestionsForLesson(lesson);
                    }

                    progressBar.setVisibility(View.GONE);
                    
                    if (lessons.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setLessons(lessons);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading lessons", e);
                    progressBar.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                });
    }

    private void countQuestionsForLesson(FillBlankLesson lesson) {
        db.collection(COLLECTION_LESSONS)
                .document(lesson.getId())
                .collection("questions")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    lesson.setQuestionCount(querySnapshot.size());
                    // Update adapter to reflect the new count
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error counting questions for lesson " + lesson.getId(), e);
                    lesson.setQuestionCount(0);
                });
    }
}
