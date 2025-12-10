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

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.functions.DBHelper;
import vn.ltdidong.apphoctienganh.models.Word;
import vn.ltdidong.apphoctienganh.models.WordSuggester;

public class NewWordRecommendService extends Service {

    private DBHelper sqlite;
    private static final String CHANNEL_ID = "suggest_service";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Đang phân tích từ vựng")
                .setContentText("Gợi ý từ mới đang được cập nhật...")
                .setSmallIcon(R.drawable.ic_notification)
                .build();

        //startForeground(1, notification);

        // chạy thuật toán trong background thread
        new Thread(() -> {
            runSuggestionEngine();
            stopSelf(); // chạy xong thì tự tắt
        }).start();

        return START_NOT_STICKY;
    }

    private void runSuggestionEngine() {
        sqlite = new DBHelper(this);

        // Lấy 50–100 từ người dùng đã tìm gần đây
        List<Word> recent = sqlite.getRecentWords(100);

        if (recent == null || recent.size() < 5) {
            return;
        }

        // Lấy từ điển đã cache trong SQLite
        /*
        List<Word> dictionary = sqlite.getDictionaryWords();

        if (dictionary == null || dictionary.isEmpty()) {
            return;
        }*/

        // Chạy WordSuggester
        WordSuggester suggester = new WordSuggester();
        //List<String> suggestions = suggester.suggest(recent, dictionary, 20);

        // Lưu vào SharedPreferences
        //saveSuggestions(suggestions);
    }

    private void saveSuggestions(List<String> list) {
        SharedPreferences prefs = getSharedPreferences("suggestions", MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();

        // lưu dưới dạng chuỗi JSON đơn giản
        ed.putString("latest", String.join(",", list));
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
    public IBinder onBind(Intent intent) { return null; }
}
