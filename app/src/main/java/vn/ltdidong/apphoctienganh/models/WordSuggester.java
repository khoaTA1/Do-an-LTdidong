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
        this.kmeans = new Kmeans(3, 30);
    }

    public List<String> suggest(List<Word> recent, int limit) {

        if (recent == null || recent.size() < 5) {
            return new ArrayList<>();
        }

        // Vectorize
        List<double[]> vectors = vectorizer.makeVectors(recent);

        // KMeans
        List<List<double[]>> clusters = kmeans.fit(vectors);

        // Chọn cluster lớn nhất
        int idx = findLargestCluster(clusters);
        List<double[]> mainCluster = clusters.get(idx);

        // Lấy Word tương ứng cluster
        List<Word> clusterWords = new ArrayList<>();
        for (double[] v : mainCluster) {
            int i = vectors.indexOf(v);
            if (i >= 0) clusterWords.add(recent.get(i));
        }

        // Gom synonyms + POS mở rộng
        return generateSuggestions(clusterWords, recent, limit);
    }

    // =========================

    private List<String> generateSuggestions(
            List<Word> cluster,
            List<Word> recent,
            int limit) {

        List<String> out = new ArrayList<>();

        for (Word w : cluster) {
            for (String syn : w.getSyn()) {

                if (!containsWord(recent, syn) && !out.contains(syn)) {
                    out.add(syn);
                }

                if (out.size() >= limit) break;
            }
            if (out.size() >= limit) break;
        }

        return out;
    }

    private boolean containsWord(List<Word> list, String word) {
        for (Word w : list) {
            if (w.getWord().equalsIgnoreCase(word)) return true;
        }
        return false;
    }

    private int findLargestCluster(List<List<double[]>> clusters) {
        int max = 0, idx = 0;
        for (int i = 0; i < clusters.size(); i++) {
            if (clusters.get(i).size() > max) {
                max = clusters.get(i).size();
                idx = i;
            }
        }
        return idx;
    }
}
