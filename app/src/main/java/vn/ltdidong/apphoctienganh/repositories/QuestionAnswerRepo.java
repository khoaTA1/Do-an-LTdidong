package vn.ltdidong.apphoctienganh.repositories;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.ltdidong.apphoctienganh.functions.FirestoreCallBack;
import vn.ltdidong.apphoctienganh.models.QuestionAnswer;

public class QuestionAnswerRepo {
    private static final String QUESTION_COLLECTION_NAME = "question";
    private static final String ANSWER_COLLECTION_NAME = "question";

    private final FirebaseFirestore firestore;
    private int correctAnswer;
    private int passageId;
    private String questionContent;

    public QuestionAnswerRepo() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void getQuestionAnswerByPassageId(long pid, FirestoreCallBack callback) {
        firestore.collection(QUESTION_COLLECTION_NAME).whereEqualTo("passageId", pid)
                .get().addOnSuccessListener(q_snapshots -> {
                    if (!q_snapshots.isEmpty()) {
                        List<QuestionAnswer> questionList = new ArrayList<>();

                        // lấy lần lượt câu hỏi liên quan đến đoạn văn bản
                        for (DocumentSnapshot q_snap : q_snapshots.getDocuments()) {
                            // parse sang model QuestionAnswer
                            QuestionAnswer QA = q_snap.toObject(QuestionAnswer.class);

                            // set id
                            long questionId = Long.valueOf(q_snap.getId());
                            QA.setId(questionId);

                            // lấy các answer liên quan đến câu hỏi hiện tại
                            getAnswerByQuestionId(questionId, result -> {
                                if (result != null) {
                                    // set Map<> answer
                                    QA.setAnswers((Map<Integer, String>) result);
                                } else QA.setAnswers(null);

                                questionList.add(QA);
                            });
                        }

                        // kiểm tra nếu questionList đã được nạp đầy đủ câu hỏi (bằng q_snapshots)
                        // thì mới kết thúc và trả về kết quả
                        if (questionList.size() == q_snapshots.size()) {
                            Log.d(">>> QuestionAnswer Repo ^1", "Tìm được danh sách câu hỏi, số lượng" + questionList.size());
                            callback.returnResult(questionList);
                        }
                    } else {
                        callback.returnResult(null);
                    }
                }).addOnFailureListener(e -> {
                    Log.e("!!! QuestionAnswer Repo ^1", "Lỗi: ", e);
                    callback.returnResult(null);
                });
    }

    private void getAnswerByQuestionId(long questionId, FirestoreCallBack callback) {
            firestore.collection(ANSWER_COLLECTION_NAME).whereEqualTo("questionId", questionId)
                    .get().addOnSuccessListener(a_snapshots -> {
                        if (!a_snapshots.isEmpty()) {
                            Map<Integer, String> answers = new HashMap<>();

                            for (DocumentSnapshot a_snap : a_snapshots.getDocuments()) {
                                answers.put((Integer) a_snap.get("dedicatedId"), a_snap.get("answerDetail").toString());
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
