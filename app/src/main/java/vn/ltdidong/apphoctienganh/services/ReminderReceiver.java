package vn.ltdidong.apphoctienganh.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import vn.ltdidong.apphoctienganh.database.AppDatabase;
import vn.ltdidong.apphoctienganh.models.StudyHabit;
import vn.ltdidong.apphoctienganh.utils.NotificationHelper;

public class ReminderReceiver extends BroadcastReceiver {
    
    private static final String TAG = "ReminderReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "ReminderReceiver triggered");
        
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "No user logged in, skipping reminder");
            return;
        }
        
        String userId = currentUser.getUid();
        AppDatabase db = AppDatabase.getDatabase(context);
        
        // Run in background thread
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                StudyHabit habit = db.studyHabitDao().getByUser(userId);
                
                if (habit != null && habit.getCurrentStreak() > 0) {
                    long hoursSinceLastStudy = 
                        (System.currentTimeMillis() - habit.getLastStudyDate()) / (60 * 60 * 1000);
                    
                    Log.d(TAG, "Hours since last study: " + hoursSinceLastStudy);
                    
                    // If more than 20 hours since last study, send reminder
                    if (hoursSinceLastStudy >= 20) {
                        NotificationHelper notificationHelper = new NotificationHelper(context);
                        notificationHelper.showStreakReminder(habit.getCurrentStreak());
                        Log.d(TAG, "Streak reminder sent for " + habit.getCurrentStreak() + " days");
                    }
                } else {
                    Log.d(TAG, "No habit data or streak is 0");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in ReminderReceiver", e);
            }
        });
    }
}
