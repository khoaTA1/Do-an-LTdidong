package vn.ltdidong.apphoctienganh;

import android.app.Application;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.FirebaseApp;

import java.util.concurrent.TimeUnit;

import vn.ltdidong.apphoctienganh.functions.DBHelper;
import vn.ltdidong.apphoctienganh.workers.DailyAnalysisWorker;

public class MyApplication extends Application {
    
    private static final String TAG = "MyApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        Log.d(TAG, "Application started");

        try {
            FirebaseApp.initializeApp(this);
            Log.d("App", "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e("App", "Failed to initialize Firebase: " + e.getMessage());
        }

        // Xóa cache SQLite khi app khởi động
        try {
            DBHelper sqlite = new DBHelper(this);
            sqlite.clearAllTables();
            Log.d("App", "SQLite cache cleared on app start");
        } catch (Exception e) {
            Log.e("App", "Failed to clear SQLite cache", e);
        }
        
        // Schedule daily background work
        scheduleDailyWork();
    }
    
    /**
     * Schedule daily analysis work
     * This will run once per day when device is connected to network
     */
    private void scheduleDailyWork() {
        try {
            // Create constraints for the work
            Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Need network for Firestore sync
                .setRequiresBatteryNotLow(true) // Don't run when battery is low
                .build();
            
            // Create periodic work request (runs once per day)
            PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                DailyAnalysisWorker.class,
                24, TimeUnit.HOURS, // Repeat interval
                15, TimeUnit.MINUTES // Flex interval
            )
            .setConstraints(constraints)
            .build();
            
            // Enqueue the work (KEEP means don't replace if already scheduled)
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "daily_analysis",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            );
            
            Log.d(TAG, "Daily analysis work scheduled successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling daily work", e);
        }
    }
}
