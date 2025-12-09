package vn.ltdidong.apphoctienganh.functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Kmeans {

    private int k;          // số cụm
    private int maxIter;    // số vòng lặp tối đa

    public Kmeans(int k, int maxIter) {
        this.k = k;
        this.maxIter = maxIter;
    }

    public List<List<double[]>> fit(List<double[]> data) {
        List<double[]> centroids = initCentroids(data);
        List<List<double[]>> clusters = null;

        for (int iter = 0; iter < maxIter; iter++) {
            clusters = assignClusters(data, centroids);
            List<double[]> newCentroids = computeCentroids(clusters);

            if (isConverged(centroids, newCentroids)) break;
            centroids = newCentroids;
        }

        return clusters;
    }

    private List<double[]> initCentroids(List<double[]> data) {
        List<double[]> centroids = new ArrayList<>();
        Collections.shuffle(data);
        for (int i = 0; i < k; i++) centroids.add(data.get(i));
        return centroids;
    }

    private List<List<double[]>> assignClusters(List<double[]> data, List<double[]> centroids) {
        List<List<double[]>> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) clusters.add(new ArrayList<>());

        for (double[] point : data) {
            int bestCluster = 0;
            double bestDist = distance(point, centroids.get(0));

            for (int i = 1; i < k; i++) {
                double dist = distance(point, centroids.get(i));
                if (dist < bestDist) {
                    bestDist = dist;
                    bestCluster = i;
                }
            }
            clusters.get(bestCluster).add(point);
        }

        return clusters;
    }

    private List<double[]> computeCentroids(List<List<double[]>> clusters) {
        List<double[]> newCentroids = new ArrayList<>();

        for (List<double[]> cluster : clusters) {
            int dim = cluster.get(0).length;
            double[] mean = new double[dim];

            for (double[] p : cluster) {
                for (int i = 0; i < dim; i++) {
                    mean[i] += p[i];
                }
            }

            for (int i = 0; i < dim; i++) mean[i] /= cluster.size();
            newCentroids.add(mean);
        }

        return newCentroids;
    }

    private boolean isConverged(List<double[]> oldC, List<double[]> newC) {
        for (int i = 0; i < k; i++) {
            if (distance(oldC.get(i), newC.get(i)) > 0.001) return false;
        }
        return true;
    }

    private double distance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++)
            sum += Math.pow(a[i] - b[i], 2);
        return Math.sqrt(sum);
    }
}
