package vn.ltdidong.apphoctienganh.repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.ltdidong.apphoctienganh.models.ListeningLesson;
import vn.ltdidong.apphoctienganh.models.Question;

/**
 * Firebase Repository cho Listening Lessons
 * Load dữ liệu từ Firebase Firestore
 * Collection structure:
 * - listening_lessons (collection)
 *   - lessonId (document)
 *     - title, description, difficulty, audioUrl, duration, transcript, imageUrl, questionCount
 *     - questions (subcollection)
 *       - questionId (document)
 *         - questionText, optionA, optionB, optionC, optionD, correctAnswer, explanation, orderIndex
 */
public class FirebaseListeningRepo {
    
    private static final String TAG = "FirebaseListeningRepo";
    private static final String COLLECTION_LESSONS = "listening_lessons";
    private static final String COLLECTION_QUESTIONS = "questions";
    
    private final FirebaseFirestore db;
    
    /**
     * Constructor
     */
    public FirebaseListeningRepo() {
        this.db = FirebaseFirestore.getInstance();
    }
    
    // ============= LISTENING LESSON METHODS =============
    
    /**
     * Lấy tất cả bài học từ Firebase
     * @return LiveData chứa danh sách bài học
     */
    public LiveData<List<ListeningLesson>> getAllLessons() {
        MutableLiveData<List<ListeningLesson>> lessonsLiveData = new MutableLiveData<>();
        
        db.collection(COLLECTION_LESSONS)
            .orderBy("id", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<ListeningLesson> lessons = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    ListeningLesson lesson = documentToLesson(document);
                    if (lesson != null) {
                        lessons.add(lesson);
                    }
                }
                lessonsLiveData.setValue(lessons);
                Log.d(TAG, "Loaded " + lessons.size() + " lessons from Firebase");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading lessons from Firebase", e);
                lessonsLiveData.setValue(new ArrayList<>());
            });
        
        return lessonsLiveData;
    }
    
    /**
     * Lấy bài học theo độ khó
     * @param difficulty Độ khó: EASY, MEDIUM, HARD
     * @return LiveData chứa danh sách bài học
     */
    public LiveData<List<ListeningLesson>> getLessonsByDifficulty(String difficulty) {
        MutableLiveData<List<ListeningLesson>> lessonsLiveData = new MutableLiveData<>();
        
        db.collection(COLLECTION_LESSONS)
            .whereEqualTo("difficulty", difficulty)
            .orderBy("id", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<ListeningLesson> lessons = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    ListeningLesson lesson = documentToLesson(document);
                    if (lesson != null) {
                        lessons.add(lesson);
                    }
                }
                lessonsLiveData.setValue(lessons);
                Log.d(TAG, "Loaded " + lessons.size() + " " + difficulty + " lessons from Firebase");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading lessons by difficulty from Firebase", e);
                lessonsLiveData.setValue(new ArrayList<>());
            });
        
        return lessonsLiveData;
    }
    
    /**
     * Lấy một bài học cụ thể theo ID
     * @param lessonId ID của bài học
     * @return LiveData chứa bài học
     */
    public LiveData<ListeningLesson> getLessonById(int lessonId) {
        MutableLiveData<ListeningLesson> lessonLiveData = new MutableLiveData<>();
        
        db.collection(COLLECTION_LESSONS)
            .whereEqualTo("id", lessonId)
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                    ListeningLesson lesson = documentToLesson(document);
                    lessonLiveData.setValue(lesson);
                    Log.d(TAG, "Loaded lesson " + lessonId + " from Firebase");
                } else {
                    lessonLiveData.setValue(null);
                    Log.w(TAG, "Lesson " + lessonId + " not found in Firebase");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading lesson " + lessonId + " from Firebase", e);
                lessonLiveData.setValue(null);
            });
        
        return lessonLiveData;
    }
    
    /**
     * Thêm bài học mới lên Firebase
     * @param lesson Bài học cần thêm
     * @param callback Callback khi hoàn thành
     */
    public void insertLesson(ListeningLesson lesson, OnCompleteCallback callback) {
        Map<String, Object> lessonMap = lessonToMap(lesson);
        
        db.collection(COLLECTION_LESSONS)
            .add(lessonMap)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Lesson added to Firebase with ID: " + documentReference.getId());
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error adding lesson to Firebase", e);
                if (callback != null) callback.onFailure(e);
            });
    }
    
    // ============= QUESTION METHODS =============
    
    /**
     * Lấy tất cả câu hỏi của một bài học
     * @param lessonId ID của bài học
     * @return LiveData chứa danh sách câu hỏi
     */
    public LiveData<List<Question>> getQuestionsByLesson(int lessonId) {
        MutableLiveData<List<Question>> questionsLiveData = new MutableLiveData<>();
        
        // Tìm document của lesson trước
        db.collection(COLLECTION_LESSONS)
            .whereEqualTo("id", lessonId)
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    String lessonDocId = queryDocumentSnapshots.getDocuments().get(0).getId();
                    
                    // Lấy questions từ subcollection
                    db.collection(COLLECTION_LESSONS)
                        .document(lessonDocId)
                        .collection(COLLECTION_QUESTIONS)
                        .orderBy("orderIndex", Query.Direction.ASCENDING)
                        .get()
                        .addOnSuccessListener(questionSnapshots -> {
                            List<Question> questions = new ArrayList<>();
                            for (QueryDocumentSnapshot document : questionSnapshots) {
                                Question question = documentToQuestion(document, lessonId);
                                if (question != null) {
                                    questions.add(question);
                                }
                            }
                            questionsLiveData.setValue(questions);
                            Log.d(TAG, "Loaded " + questions.size() + " questions for lesson " + lessonId);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error loading questions from Firebase", e);
                            questionsLiveData.setValue(new ArrayList<>());
                        });
                } else {
                    Log.w(TAG, "Lesson " + lessonId + " not found");
                    questionsLiveData.setValue(new ArrayList<>());
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error finding lesson document", e);
                questionsLiveData.setValue(new ArrayList<>());
            });
        
        return questionsLiveData;
    }
    
    /**
     * Thêm câu hỏi mới cho một bài học
     * @param lessonId ID của bài học
     * @param question Câu hỏi cần thêm
     * @param callback Callback khi hoàn thành
     */
    public void insertQuestion(int lessonId, Question question, OnCompleteCallback callback) {
        // Tìm document của lesson trước
        db.collection(COLLECTION_LESSONS)
            .whereEqualTo("id", lessonId)
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    String lessonDocId = queryDocumentSnapshots.getDocuments().get(0).getId();
                    Map<String, Object> questionMap = questionToMap(question);
                    
                    // Thêm vào subcollection
                    db.collection(COLLECTION_LESSONS)
                        .document(lessonDocId)
                        .collection(COLLECTION_QUESTIONS)
                        .add(questionMap)
                        .addOnSuccessListener(documentReference -> {
                            Log.d(TAG, "Question added to Firebase");
                            if (callback != null) callback.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error adding question to Firebase", e);
                            if (callback != null) callback.onFailure(e);
                        });
                } else {
                    Log.w(TAG, "Lesson " + lessonId + " not found");
                    if (callback != null) callback.onFailure(new Exception("Lesson not found"));
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error finding lesson document", e);
                if (callback != null) callback.onFailure(e);
            });
    }
    
    // ============= HELPER METHODS =============
    
    /**
     * Convert Firebase DocumentSnapshot thành ListeningLesson object
     */
    private ListeningLesson documentToLesson(DocumentSnapshot document) {
        try {
            ListeningLesson lesson = new ListeningLesson();
            
            // Xử lý ID - có thể là String hoặc Number trong Firebase
            Object idObj = document.get("id");
            int id;
            if (idObj instanceof String) {
                id = Integer.parseInt((String) idObj);
            } else if (idObj instanceof Long) {
                id = ((Long) idObj).intValue();
            } else if (idObj instanceof Number) {
                id = ((Number) idObj).intValue();
            } else {
                Log.e(TAG, "Invalid id type in document: " + document.getId());
                return null;
            }
            lesson.setId(id);
            
            lesson.setTitle(document.getString("title"));
            lesson.setDescription(document.getString("description"));
            lesson.setDifficulty(document.getString("difficulty"));
            lesson.setAudioUrl(document.getString("audioUrl"));
            
            // Xử lý duration - có thể là String hoặc Number
            Object durationObj = document.get("duration");
            if (durationObj instanceof String) {
                lesson.setDuration(Integer.parseInt((String) durationObj));
            } else if (durationObj instanceof Number) {
                lesson.setDuration(((Number) durationObj).intValue());
            } else {
                lesson.setDuration(0);
            }
            
            lesson.setTranscript(document.getString("transcript"));
            lesson.setImageUrl(document.getString("imageUrl"));
            
            // Xử lý questionCount - có thể là String hoặc Number
            Object qCountObj = document.get("questionCount");
            if (qCountObj instanceof String) {
                lesson.setQuestionCount(Integer.parseInt((String) qCountObj));
            } else if (qCountObj instanceof Number) {
                lesson.setQuestionCount(((Number) qCountObj).intValue());
            } else {
                lesson.setQuestionCount(0);
            }
            
            return lesson;
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to lesson: " + document.getId(), e);
            return null;
        }
    }
    
    /**
     * Convert ListeningLesson object thành Map để lưu vào Firebase
     */
    private Map<String, Object> lessonToMap(ListeningLesson lesson) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", lesson.getId());
        map.put("title", lesson.getTitle());
        map.put("description", lesson.getDescription());
        map.put("difficulty", lesson.getDifficulty());
        map.put("audioUrl", lesson.getAudioUrl());
        map.put("duration", lesson.getDuration());
        map.put("transcript", lesson.getTranscript());
        map.put("imageUrl", lesson.getImageUrl());
        map.put("questionCount", lesson.getQuestionCount());
        return map;
    }
    
    /**
     * Convert Firebase DocumentSnapshot thành Question object
     */
    private Question documentToQuestion(DocumentSnapshot document, int lessonId) {
        try {
            Question question = new Question();
            question.setLessonId(lessonId);
            question.setQuestionText(document.getString("questionText"));
            question.setOptionA(document.getString("optionA"));
            question.setOptionB(document.getString("optionB"));
            question.setOptionC(document.getString("optionC"));
            question.setOptionD(document.getString("optionD"));
            question.setCorrectAnswer(document.getString("correctAnswer"));
            question.setExplanation(document.getString("explanation"));
            question.setOrderIndex(document.getLong("orderIndex").intValue());
            return question;
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to question", e);
            return null;
        }
    }
    
    /**
     * Convert Question object thành Map để lưu vào Firebase
     */
    private Map<String, Object> questionToMap(Question question) {
        Map<String, Object> map = new HashMap<>();
        map.put("questionText", question.getQuestionText());
        map.put("optionA", question.getOptionA());
        map.put("optionB", question.getOptionB());
        map.put("optionC", question.getOptionC());
        map.put("optionD", question.getOptionD());
        map.put("correctAnswer", question.getCorrectAnswer());
        map.put("explanation", question.getExplanation());
        map.put("orderIndex", question.getOrderIndex());
        return map;
    }
    
    // ============= CALLBACK INTERFACE =============
    
    /**
     * Interface cho callback khi hoàn thành thao tác Firebase
     */
    public interface OnCompleteCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}
