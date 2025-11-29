package vn.ltdidong.apphoctienganh.repositories;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import vn.ltdidong.apphoctienganh.functions.FirestoreCallBack;

public class AnswerRepo {
    private static final String QUESTION_COLLECTION_NAME = "answer";

    private FirebaseFirestore firestore;

    public AnswerRepo() {
        firestore = FirebaseFirestore.getInstance();
    }

    /*
    public void getAnswerByQuestionId(long qid, FirestoreCallBack callback) {
        firestore.collection(COLLECTION_NAME).whereEqualTo("questionId", qid)
                .get().addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        List<Answer>
                    }
                })
    }*/
}
