package vn.ltdidong.apphoctienganh.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import vn.ltdidong.apphoctienganh.functions.Kmeans;


public class WordSuggester {

    private Vectorizer vectorizer;
    private Kmeans kmeans;

    public WordSuggester() {
        this.vectorizer = new Vectorizer();
        this.kmeans = new Kmeans(3, 30); // 3 cụm, chạy tối đa 30 iteration
    }

    /**
     * Gợi ý từ dựa trên lịch sử tìm kiếm
     *
     * @param recent  danh sách Word user đã tìm gần đây (5 - 100 từ)
     * @param dictionary danh sách từ đầy đủ (hoặc một phần)
     * @param limit  số lượng từ muốn gợi ý
     */
    public List<String> suggest(List<Word> recent, List<Word> dictionary, int limit) {

        if (recent == null || recent.size() < 5) {
            return new ArrayList<>();
        }

        // 1. Vector hóa lịch sử
        List<double[]> vectors = vectorizer.makeVectors(recent);

        // 2. Chạy KMeans
        List<List<double[]>> clusters = kmeans.fit(vectors);

        // 3. Tìm cụm lớn nhất (cluster đại diện interest của user)
        int bestCluster = findLargestCluster(clusters);
        List<double[]> chosenCluster = clusters.get(bestCluster);

        // 4. Tính centroid của cụm
        double[] centroid = computeCentroid(chosenCluster);

        // 5. Tìm từ gần centroid nhất (trừ từ đã tìm)
        List<ScoredWord> scoredList = new ArrayList<>();

        for (Word w : dictionary) {

            if (containsWord(recent, w.getWord())) continue; // bỏ từ user đã tìm

            double[] v = vectorizer.vectorize(w);

            double dist = euclidean(v, centroid);

            scoredList.add(new ScoredWord(w.getWord(), dist));
        }

        // 6. Sắp xếp khoảng cách tăng dần → càng gần centroid càng phù hợp
        Collections.sort(scoredList, Comparator.comparingDouble(a -> a.score));

        // 7. Lấy top N
        List<String> out = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, scoredList.size()); i++) {
            out.add(scoredList.get(i).word);
        }

        return out;
    }


    // ================= HELPER FUNCTIONS =====================

    private boolean containsWord(List<Word> list, String word) {
        for (Word w : list) {
            if (w.getWord().equalsIgnoreCase(word)) return true;
        }
        return false;
    }

    private int findLargestCluster(List<List<double[]>> clusters) {
        int maxSize = 0;
        int index = 0;

        for (int i = 0; i < clusters.size(); i++) {
            if (clusters.get(i).size() > maxSize) {
                maxSize = clusters.get(i).size();
                index = i;
            }
        }
        return index;
    }

    private double[] computeCentroid(List<double[]> cluster) {
        int dim = cluster.get(0).length;
        double[] c = new double[dim];

        for (double[] v : cluster) {
            for (int i = 0; i < dim; i++) {
                c[i] += v[i];
            }
        }

        for (int i = 0; i < dim; i++) {
            c[i] /= cluster.size();
        }

        return c;
    }

    private double euclidean(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }


    // Struct để lưu thông tin gợi ý
    private static class ScoredWord {
        String word;
        double score;

        ScoredWord(String word, double score) {
            this.word = word;
            this.score = score;
        }
    }
}
