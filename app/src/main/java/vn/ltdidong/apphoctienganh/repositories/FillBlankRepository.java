package vn.ltdidong.apphoctienganh.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import vn.ltdidong.apphoctienganh.models.FillBlankQuestion;

/**
 * Firebase Repository cho Fill Blank Questions
 * Load dữ liệu từ Firebase Firestore
 * 
 * Collection structure:
 * - listening_lessons (collection)
 *   - lessonId (document)
 *     - fill_blank_questions (subcollection)
 *       - questionId (document)
 *         - sentenceWithBlanks, correctAnswers, hint, orderIndex, audioTimestamp
 */
public class FillBlankRepository {

    private static final String TAG = "FillBlankRepository";
    private static final String COLLECTION_FILL_BLANK_LESSONS = "fill_blank_lesson_listening";
    private static final String COLLECTION_QUESTIONS = "questions";

    private final FirebaseFirestore db;

    public FillBlankRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Lấy tất cả câu hỏi Fill Blank của một bài học
     * @param lessonId ID của bài học
     * @return LiveData chứa danh sách câu hỏi
     */
    public LiveData<List<FillBlankQuestion>> getFillBlankQuestionsByLesson(int lessonId) {
        MutableLiveData<List<FillBlankQuestion>> questionsLiveData = new MutableLiveData<>();

        // Tìm document của lesson - document ID là String của lessonId
        db.collection(COLLECTION_FILL_BLANK_LESSONS)
            .document(String.valueOf(lessonId))
            .get()
            .addOnSuccessListener(lessonDoc -> {
                if (lessonDoc.exists()) {
                    // Load subcollection questions
                    lessonDoc.getReference()
                        .collection(COLLECTION_QUESTIONS)
                        .orderBy("orderIndex", Query.Direction.ASCENDING)
                        .get()
                        .addOnSuccessListener(questionsSnapshot -> {
                            List<FillBlankQuestion> questions = new ArrayList<>();

                            for (QueryDocumentSnapshot doc : questionsSnapshot) {
                                FillBlankQuestion question = documentToQuestion(doc, lessonId);
                                if (question != null) {
                                    questions.add(question);
                                }
                            }

                            questionsLiveData.setValue(questions);
                            Log.d(TAG, "Loaded " + questions.size() + " fill blank questions for lesson " + lessonId);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error loading fill blank questions from Firebase", e);
                            questionsLiveData.setValue(new ArrayList<>());
                        });
                } else {
                    Log.w(TAG, "Lesson document " + lessonId + " not found in Firebase");
                    questionsLiveData.setValue(new ArrayList<>());
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading lesson document from Firebase", e);
                questionsLiveData.setValue(new ArrayList<>());
            });

        return questionsLiveData;
    }

    /**
     * Convert Firestore document thành FillBlankQuestion object
     */
    private FillBlankQuestion documentToQuestion(QueryDocumentSnapshot document, int lessonId) {
        try {
            FillBlankQuestion question = new FillBlankQuestion();
            question.setLessonId(lessonId);
            question.setSentenceWithBlanks(document.getString("sentenceWithBlanks"));
            question.setCorrectAnswers(document.getString("correctAnswers"));
            question.setHint(document.getString("hint"));
            question.setAudioUrl(document.getString("audioUrl")); // Load audioUrl từ Firebase

            // Handle Long to int conversion - với default value nếu không có
            Long orderIndexLong = document.getLong("orderIndex");
            question.setOrderIndex(orderIndexLong != null ? orderIndexLong.intValue() : 0);

            return question;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing fill blank question document", e);
            return null;
        }
    }
}
