package vn.ltdidong.apphoctienganh.repositories;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import vn.ltdidong.apphoctienganh.functions.FirestoreCallBack;
import vn.ltdidong.apphoctienganh.models.QuestionAnswer;

public class QuestionAnswerRepo {
    private static final String QUESTION_A_COLLECTION_NAME = "question";
    private static final String ANSWER_A_COLLECTION_NAME = "answer";

    private static final String QUESTION_B_COLLECTION_NAME = "questionB";
    private static final String ANSWER_B_COLLECTION_NAME = "answerB";

    private final FirebaseFirestore firestore;
    private int correctAnswer;
    private int passageId;
    private String questionContent;

    public QuestionAnswerRepo() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void getQuestionAnswerByPassageId(long pid, int lvl, FirestoreCallBack callback) {
        String QUESTION_COLLECTION_NAME = QUESTION_A_COLLECTION_NAME;
        if (lvl == 1) QUESTION_COLLECTION_NAME = QUESTION_B_COLLECTION_NAME;

        firestore.collection(QUESTION_COLLECTION_NAME).whereEqualTo("passageId", pid)
                .get().addOnSuccessListener(q_snapshots -> {
                    if (!q_snapshots.isEmpty()) {
                        List<QuestionAnswer> questionList = new ArrayList<>();
                        int totalQuestions = q_snapshots.size();

                        CountDownLatch latch = new CountDownLatch(totalQuestions);

                        // lấy lần lượt câu hỏi liên quan đến đoạn văn bản
                        for (DocumentSnapshot q_snap : q_snapshots.getDocuments()) {
                            // parse sang model QuestionAnswer
                            QuestionAnswer QA = q_snap.toObject(QuestionAnswer.class);
                            Log.d(">>> QuestionAnswer Repo ^1", "Nội dung câu hỏi: " + QA.getQuestionContent());

                            // set id
                            long questionId = Long.valueOf(q_snap.getId());
                            QA.setId(questionId);

                            // lấy các answer liên quan đến câu hỏi hiện tại
                            getAnswerByQuestionId(questionId, lvl, result -> {
                                if (result != null) {
                                    // set Map<> answer
                                    QA.setAnswers((Map<Integer, String>) result);
                                } else QA.setAnswers(null);

                                questionList.add(QA);

                                latch.countDown();
                            });
                        }

                        // kiểm tra nếu questionList đã được nạp đầy đủ câu hỏi, bằng count down
                        // thì mới kết thúc và trả về kết quả
                        new Thread(() -> {
                            try {
                                // đợi answer load xong
                                latch.await();
                                Log.d(">>> QuestionAnswer Repo ^1", "Tìm được danh sách câu hỏi, số lượng" + questionList.size());
                                callback.returnResult(questionList);
                            } catch (InterruptedException e) {
                                Log.e("!!! QuestionAnswer Repo ^1", "CountDownLatch lỗi", e);
                                callback.returnResult(null);
                            }
                        }).start();
                    } else {
                        callback.returnResult(null);
                    }
                }).addOnFailureListener(e -> {
                    Log.e("!!! QuestionAnswer Repo ^1", "Lỗi: ", e);
                    callback.returnResult(null);
                });
    }

    private void getAnswerByQuestionId(long questionId, int lvl, FirestoreCallBack callback) {
            String ANSWER_COLLECTION_NAME = ANSWER_A_COLLECTION_NAME;
            if (lvl == 1) ANSWER_COLLECTION_NAME = ANSWER_B_COLLECTION_NAME;

            Log.d(">>> QuestionAnswer Repo ^2", "Tìm danh sách câu trả lời cho question id: " + questionId);
            firestore.collection(ANSWER_COLLECTION_NAME).whereEqualTo("questionId", questionId)
                    .get().addOnSuccessListener(a_snapshots -> {
                        if (!a_snapshots.isEmpty()) {
                            Map<Integer, String> answers = new HashMap<>();

                            for (DocumentSnapshot a_snap : a_snapshots.getDocuments()) {
                                Long dedicatedId = a_snap.getLong("dedicatedId");
                                Log.d(">>> QuestionAnswer Repo ^2", "DEBUG: dedicatedId = " + dedicatedId);
                                //int dedicatedId = ((Number) obj).intValue();
                                answers.put(dedicatedId.intValue(), a_snap.getString("answerDetail"));
                            }
                            Log.d(">>> QuestionAnswer Repo ^2", "Tìm được danh sách câu trả lời, số lượng" + answers.size());

                            callback.returnResult(answers);
                        } else {
                            Log.e("!!! QuestionAnswer Repo ^2", "Không tìm được danh sách câu trả lời");
                            callback.returnResult(null);
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("!!! QuestionAnswer Repo ^2", "Lỗi: ", e);
                        callback.returnResult(null);
                    });
    }
}
