package vn.ltdidong.apphoctienganh.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.functions.DBHelper;
import vn.ltdidong.apphoctienganh.functions.Kmeans;
import vn.ltdidong.apphoctienganh.models.Vectorizer;
import vn.ltdidong.apphoctienganh.models.Word;
import vn.ltdidong.apphoctienganh.models.WordSuggester;

public class NewWordRecommendService extends Service {

    private static final String CHANNEL_ID = "suggest_service";
    private DBHelper sqlite;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Đang phân tích từ vựng")
                .setContentText("Hệ thống đang gợi ý từ mới...")
                .setSmallIcon(R.drawable.ic_notification)
                .build();

        // Nếu sau này cần foreground thì bật lại
        // startForeground(1, notification);

        new Thread(this::runSuggestionEngine).start();

        return START_NOT_STICKY;
    }

    private void runSuggestionEngine() {

        sqlite = new DBHelper(this);

        // Lấy từ đã search
        List<Word> recentWords = sqlite.getRecentWords(100);

        if (recentWords == null || recentWords.size() < 5) {
            stopSelf();
            return;
        }

        // Vectorize
        Vectorizer vectorizer = new Vectorizer();
        List<double[]> vectors = vectorizer.makeVectors(recentWords);

        // Chạy KMeans
        int K = Math.min(3, recentWords.size() / 2);
        Kmeans kmeans = new Kmeans(K, 50);
        List<List<double[]>> clusters = kmeans.fit(vectors);

        // Map vector → word index
        Map<double[], Word> vectorWordMap = new HashMap<>();
        for (int i = 0; i < vectors.size(); i++) {
            vectorWordMap.put(vectors.get(i), recentWords.get(i));
        }

        // Chọn cluster lớn nhất
        List<Word> mainCluster = new ArrayList<>();
        int maxSize = 0;

        for (List<double[]> cluster : clusters) {
            if (cluster.size() > maxSize) {
                maxSize = cluster.size();
                mainCluster.clear();
                for (double[] v : cluster) {
                    mainCluster.add(vectorWordMap.get(v));
                }
            }
        }

        if (mainCluster.isEmpty()) {
            stopSelf();
            return;
        }

        // Sinh gợi ý
        WordSuggester suggester = new WordSuggester();
        List<String> suggestions = suggester.suggest(mainCluster, 20);

        // Lưu kết quả
        saveSuggestions(suggestions);

        stopSelf();
    }

    private void saveSuggestions(List<String> list) {
        SharedPreferences prefs = getSharedPreferences("suggestions", MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();

        ed.putString("latest", String.join(",", list));
        Log.d(">>> Save Suggestions", "List: " + list);
        ed.putLong("timestamp", System.currentTimeMillis());
        ed.apply();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    "Word Suggest Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(ch);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
