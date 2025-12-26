package vn.ltdidong.apphoctienganh.repositories;

import android.util.Log;

import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

import vn.ltdidong.apphoctienganh.functions.FirestoreCallBack;
import vn.ltdidong.apphoctienganh.models.ReadingPassage;

public class ReadingPassageRepo {
    private static final String COLLECTION_NAME_RP_A = "ReadingPassage";
    private static final String COLLECTION_NAME_RP_B = "ReadingPassageB";

    private final FirebaseFirestore firestore;

    private DocumentSnapshot lastPassage = null;

    public ReadingPassageRepo() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void getRandomPassage(int lvl, FirestoreCallBack callback) {
        firestore.collection("pref")
                .document("trackLastPassageId")
                .get().addOnSuccessListener(snap -> {
                    long maxId = 0;
                    try {
                        maxId = snap.getLong("lastPassageId");
                        Log.d(">>> RP Repo", "Max Id: " + maxId);

                        long randomId = 0;
                        do {
                            randomId = 1 + (long) (Math.random() * maxId);
                        } while (randomId == maxId);

                        Log.d(">>> RP Repo", "Kết quả random: " + randomId);

                        String COLLECTION_NAME = COLLECTION_NAME_RP_A;
                        if (lvl == 1)  COLLECTION_NAME = COLLECTION_NAME_RP_B;
                        // lấy danh sách Reading Passage bắt đầu từ id được random
                        firestore.collection(COLLECTION_NAME).orderBy(FieldPath.documentId())
                                .startAt(String.valueOf(randomId))
                                .limit(2)
                                .get()
                                .addOnSuccessListener(snap2 -> {
                                    List<ReadingPassage> list = new ArrayList<>();

                                    for (DocumentSnapshot DS : snap2) {
                                        // parse object
                                        ReadingPassage RP = DS.toObject(ReadingPassage.class);

                                        RP.setId(Long.parseLong(DS.getId()));

                                        Log.d(">>> RP Repo", "Parse obj id: " + DS.getId());
                                        list.add(RP);
                                    }

                                    callback.returnResult(list);
                                }).addOnFailureListener(e -> {
                                    Log.e("!!! RP Repo", "Lỗi: " + e);
                                });
                    } catch (NullPointerException e) {
                        Log.e("!!! RP Repo", "Null pointer excep");
                        callback.returnResult(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("!!! RP Repo", "Lỗi: " + e);
                });
    }

    public void getTotalRP(int lvl, FirestoreCallBack cb) {
        firestore.collection("pref").document("totalReadingPassageData").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (lvl == 1) {
                        cb.returnResult(documentSnapshot.getLong("totalB"));
                    } else {
                        cb.returnResult(documentSnapshot.getLong("totalA"));
                    }
                }).addOnFailureListener(e -> {
                    Log.e("!!! RP Repo", "Lỗi: " + e);
                });
    }
}
