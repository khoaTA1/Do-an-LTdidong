package vn.ltdidong.apphoctienganh.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import vn.ltdidong.apphoctienganh.managers.LearningAnalyzer;
import vn.ltdidong.apphoctienganh.utils.LearningSystemHelper;

public class DailyAnalysisWorker extends Worker {
    
    private static final String TAG = "DailyAnalysisWorker";
    
    public DailyAnalysisWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "DailyAnalysisWorker started");
        
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "No user logged in, skipping analysis");
            return Result.success();
        }
        
        String userId = currentUser.getUid();
        LearningSystemHelper helper = LearningSystemHelper.getInstance(getApplicationContext());
        
        // Use CountDownLatch to wait for async operations
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};
        
        try {
            // Analyze all learning data
            helper.analyzeAll(new LearningAnalyzer.AnalysisCallback() {
                @Override
                public void onAnalysisComplete(String message) {
                    Log.d(TAG, "Analysis complete: " + message);
                    
                    // Expire old schedules
                    helper.expireOldSchedules();
                    
                    // Delete very old schedules (older than 30 days)
                    helper.deleteOldSchedules(30);
                    
                    success[0] = true;
                    latch.countDown();
                }
                
                @Override
                public void onAnalysisError(String error) {
                    Log.e(TAG, "Analysis error: " + error);
                    success[0] = false;
                    latch.countDown();
                }
            });
            
            // Wait for completion (max 60 seconds)
            boolean completed = latch.await(60, TimeUnit.SECONDS);
            
            if (completed && success[0]) {
                Log.d(TAG, "DailyAnalysisWorker completed successfully");
                return Result.success();
            } else {
                Log.e(TAG, "DailyAnalysisWorker failed or timed out");
                return Result.retry();
            }
            
        } catch (InterruptedException e) {
            Log.e(TAG, "DailyAnalysisWorker interrupted", e);
            return Result.retry();
        } catch (Exception e) {
            Log.e(TAG, "Error in DailyAnalysisWorker", e);
            return Result.failure();
        }
    }
}
