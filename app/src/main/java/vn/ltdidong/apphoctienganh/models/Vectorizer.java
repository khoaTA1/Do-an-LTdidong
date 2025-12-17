package vn.ltdidong.apphoctienganh.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Vectorizer {

    private static final Map<String, double[]> POS_VECTOR = new HashMap<>();

    static {
        // các vector
        POS_VECTOR.put("noun",  new double[]{1, 0, 0, 0});
        POS_VECTOR.put("verb",  new double[]{0, 1, 0, 0});
        POS_VECTOR.put("adj",   new double[]{0, 0, 1, 0});
        POS_VECTOR.put("adv",   new double[]{0, 0, 0, 1});
    }

    // tạo vector ngữ nghĩa đơn giản từ POS + synonym
    public double[] vectorize(Word w) {

        double[] v = new double[8];  // 4 chiều POS + 4 chiều synonyms count

        // ---- 1. POS vector (4 chiều đầu tiên) ----
        for (String pos : w.getPos()) {
            if (POS_VECTOR.containsKey(pos)) {

                double[] pv = POS_VECTOR.get(pos);

                for (int i = 0; i < 4; i++) {
                    v[i] += pv[i];
                }
            }
        }

        // ---- 2. Synonyms count (4 chiều phía sau) ----
        int synCount = w.getSyn().size();
        v[4] = synCount;
        v[5] = synCount * 0.5;
        v[6] = synCount / 2.0;
        v[7] = synCount > 0 ? 1 : 0;

        return v;
    }

    // tạo vector cho danh sách từ
    public List<double[]> makeVectors(List<Word> list) {
        List<double[]> out = new ArrayList<>();
        for (Word w : list) {
            out.add(vectorize(w));
        }
        return out;
    }
}
