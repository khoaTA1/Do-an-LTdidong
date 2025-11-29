package vn.ltdidong.apphoctienganh.repositories;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import vn.ltdidong.apphoctienganh.functions.FirestoreCallBack;
import vn.ltdidong.apphoctienganh.models.ClozeTestQA;

public class ClozeTestQARepo {
    private static final String CT_QA_COLLECTION_NAME = "clozetestQA";
    private FirebaseFirestore firestore;
    private DocumentSnapshot lastQA = null;

    public ClozeTestQARepo() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void getClozeTestQAList(int pageSize, FirestoreCallBack callback) {
        CollectionReference Ref = firestore.collection(CT_QA_COLLECTION_NAME);
        Query query;

        if (lastQA == null) {
            // trang đầu
            query = Ref.orderBy(FieldPath.documentId()).limit(pageSize);
        } else {
            // trang tiếp theo
            query = Ref.orderBy(FieldPath.documentId()).startAfter(lastQA.getId()).limit(pageSize);
        }

        query.get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        lastQA = snapshot.getDocuments()
                                .get(snapshot.size() - 1); // lưu doc cuối trang
                    }

                    // map mỗi document thành cloze test QA
                    List<ClozeTestQA> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        ClozeTestQA qa = doc.toObject(ClozeTestQA.class);
                        // gán document id vào model nếu cần
                        qa.setId(Long.valueOf(doc.getId()));
                        list.add(qa);
                    }

                    callback.returnResult(list);
                    Log.d(">>> Cloze test QA Repo", "Đã tải về 1 số lượng câu hỏi điền khuyết: " + list.size());
                })
                .addOnFailureListener(e -> Log.e("!!! Cloze test QA Repo", "Lỗi: ", e));
    }

    // Reset pagination
    public void resetPagination() {
        lastQA = null;
    }
}
