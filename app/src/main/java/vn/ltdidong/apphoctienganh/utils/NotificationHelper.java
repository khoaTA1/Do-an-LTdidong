package vn.ltdidong.apphoctienganh.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.activities.LearningDashboardActivity;

public class NotificationHelper {
    
    private static final String CHANNEL_ID = "learning_channel";
    private static final String CHANNEL_NAME = "Nhắc học";
    private static final int NOTIFICATION_ID = 1001;
    
    private final Context context;
    private final NotificationManager notificationManager;
    
    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager) 
            context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        createNotificationChannel();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Nhắc nhở học tiếng Anh hàng ngày");
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    /**
     * Hiển thị notification nhắc giữ streak
     */
    public void showStreakReminder(int currentStreak) {
        Intent intent = new Intent(context, LearningDashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🔥 Giữ streak " + currentStreak + " ngày!")
            .setContentText("Học ít nhất 15 phút hôm nay để tiếp tục streak của bạn")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
    
    /**
     * Hiển thị notification nhắc lịch học
     */
    public void showScheduleReminder(String skillName, int durationMinutes) {
        Intent intent = new Intent(context, LearningDashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("📅 Đến giờ học rồi!")
            .setContentText("Học " + skillName + " (" + durationMinutes + " phút)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        notificationManager.notify(NOTIFICATION_ID + 1, builder.build());
    }
    
    /**
     * Hiển thị notification milestone
     */
    public void showMilestoneNotification(String title, String message) {
        Intent intent = new Intent(context, LearningDashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        notificationManager.notify(NOTIFICATION_ID + 2, builder.build());
    }
    
    /**
     * Hiển thị notification tổng hợp cuối ngày
     */
    public void showDailySummaryNotification(int sessionsCompleted, int minutesLearned) {
        Intent intent = new Intent(context, LearningDashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        String message = "Hôm nay bạn đã hoàn thành " + sessionsCompleted + 
                        " bài học và học được " + minutesLearned + " phút!";
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("📊 Tổng kết hôm nay")
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        notificationManager.notify(NOTIFICATION_ID + 3, builder.build());
    }
}
