package vn.ltdidong.apphoctienganh.repositories;

import android.util.Log;

import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

import vn.ltdidong.apphoctienganh.functions.FirestoreCallBack;
import vn.ltdidong.apphoctienganh.models.ReadingPassage;

public class ReadingPassageRepo {
    private static final String COLLECTION_NAME = "ReadingPassage";

    private final FirebaseFirestore firestore;

    private DocumentSnapshot lastPassage = null;

    public ReadingPassageRepo() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void getReadingPassagePagination(int pageSize, FirestoreCallBack callback) {
        CollectionReference Ref = firestore.collection(COLLECTION_NAME);
        Query query;

        if (lastPassage == null) {
            // trang đầu
            query = Ref.orderBy(FieldPath.documentId()).limit(pageSize);
        } else {
            // trang tiếp theo
            query = Ref.orderBy(FieldPath.documentId()).startAfter(lastPassage.getId()).limit(pageSize);
        }

        query.get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        lastPassage = snapshot.getDocuments()
                                .get(snapshot.size() - 1); // lưu doc cuối trang
                    }

                    // map mỗi document thành ReadingPassage
                    List<ReadingPassage> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        ReadingPassage rp = doc.toObject(ReadingPassage.class);
                        // gán document id vào model nếu cần
                        rp.setId(Long.valueOf(doc.getId()));
                        list.add(rp);
                    }

                    callback.returnResult(list);
                    Log.d(">>> RP Repo", "Đã tải về 1 số lượng đoạn văn: " + list.size());
                })
                .addOnFailureListener(e -> Log.e("!!! RP Repo", "Lỗi: ", e));
    }

    // Reset pagination
    public void resetPagination() {
        lastPassage = null;
    }
}
