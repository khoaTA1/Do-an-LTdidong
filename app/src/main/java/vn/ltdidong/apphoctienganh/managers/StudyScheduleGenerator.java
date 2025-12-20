package vn.ltdidong.apphoctienganh.managers;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import vn.ltdidong.apphoctienganh.database.AppDatabase;
import vn.ltdidong.apphoctienganh.models.SkillProgress;
import vn.ltdidong.apphoctienganh.models.SkillType;
import vn.ltdidong.apphoctienganh.models.StudyHabit;
import vn.ltdidong.apphoctienganh.models.StudySchedule;

/**
 * Generator tự động tạo lịch học dựa trên:
 * - Thói quen học tập (thời gian ưa thích, tần suất)
 * - Điểm mạnh/yếu (ưu tiên kỹ năng yếu)
 * - Mục tiêu học tập
 */
public class StudyScheduleGenerator {
    
    private static final String TAG = "StudyScheduleGenerator";
    
    private final Context context;
    private final AppDatabase database;
    private final LearningAnalyzer analyzer;
    
    // Cấu hình mặc định
    private static final int DEFAULT_SESSION_MINUTES = 30;
    private static final int MAX_SESSIONS_PER_DAY = 3;
    
    public StudyScheduleGenerator(Context context) {
        this.context = context;
        this.database = AppDatabase.getDatabase(context);
        this.analyzer = new LearningAnalyzer(context);
    }
    
    /**
     * Callback cho schedule generation
     */
    public interface ScheduleCallback {
        void onScheduleGenerated(List<StudySchedule> schedules);
        void onScheduleError(String error);
    }
    
    /**
     * Tạo lịch học cho N ngày tới
     * @param userId ID người dùng
     * @param daysAhead Số ngày muốn tạo lịch
     * @param callback Callback kết quả
     */
    public void generateSchedule(String userId, int daysAhead, ScheduleCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Lấy thói quen và tiến độ
                StudyHabit habit = database.studyHabitDao().getByUser(userId);
                List<SkillProgress> allProgress = database.skillProgressDao().getAllByUser(userId);
                
                // Nếu chưa có dữ liệu, phân tích trước
                if (habit == null || allProgress.isEmpty()) {
                    analyzer.analyzeAllData(userId, new LearningAnalyzer.AnalysisCallback() {
                        @Override
                        public void onAnalysisComplete(String message) {
                            generateSchedule(userId, daysAhead, callback);
                        }
                        
                        @Override
                        public void onAnalysisError(String error) {
                            callback.onScheduleError("Analysis error: " + error);
                        }
                    });
                    return;
                }
                
                List<StudySchedule> schedules = new ArrayList<>();
                
                // Lấy kỹ năng yếu để ưu tiên
                List<SkillProgress> weakSkills = analyzer.getWeakSkills(userId);
                List<SkillProgress> strongSkills = analyzer.getStrongSkills(userId);
                
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, 1); // Bắt đầu từ ngày mai
                
                // Tạo lịch cho từng ngày
                for (int day = 0; day < daysAhead; day++) {
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                    
                    // Xác định số phiên học trong ngày (dựa trên thói quen)
                    int sessionsPerDay = calculateSessionsPerDay(habit, dayOfWeek);
                    
                    // Tạo các phiên học trong ngày
                    for (int session = 0; session < sessionsPerDay; session++) {
                        StudySchedule schedule = createScheduleForSession(
                            userId, calendar, habit, weakSkills, strongSkills, session
                        );
                        
                        if (schedule != null) {
                            schedules.add(schedule);
                        }
                    }
                    
                    // Chuyển sang ngày tiếp theo
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }
                
                // Lưu vào database
                database.studyScheduleDao().insertAll(schedules);
                
                callback.onScheduleGenerated(schedules);
                
                Log.d(TAG, "Generated " + schedules.size() + " schedules for " + daysAhead + " days");
                
            } catch (Exception e) {
                Log.e(TAG, "Schedule generation error", e);
                callback.onScheduleError(e.getMessage());
            }
        });
    }
    
    /**
     * Tạo một schedule cho một phiên học cụ thể
     */
    private StudySchedule createScheduleForSession(
            String userId,
            Calendar date,
            StudyHabit habit,
            List<SkillProgress> weakSkills,
            List<SkillProgress> strongSkills,
            int sessionIndex) {
        
        StudySchedule schedule = new StudySchedule();
        schedule.setUserId(userId);
        
        // Set ngày
        Calendar scheduledTime = (Calendar) date.clone();
        
        // Xác định giờ học (dựa trên thói quen hoặc mặc định)
        int startHour = determineStartHour(habit, sessionIndex);
        scheduledTime.set(Calendar.HOUR_OF_DAY, startHour);
        scheduledTime.set(Calendar.MINUTE, 0);
        scheduledTime.set(Calendar.SECOND, 0);
        
        schedule.setScheduledDate(scheduledTime.getTimeInMillis());
        schedule.setRecommendedStartHour(startHour);
        
        // Chọn kỹ năng cần học (ưu tiên kỹ năng yếu)
        String skillType = selectSkillForSession(weakSkills, strongSkills, sessionIndex);
        schedule.setSkillType(skillType);
        
        // Xác định thời lượng (dựa trên thói quen hoặc mặc định)
        int duration = habit != null && habit.getAverageSessionMinutes() > 0 
            ? habit.getAverageSessionMinutes() 
            : DEFAULT_SESSION_MINUTES;
        schedule.setRecommendedDurationMinutes(duration);
        
        // Xác định độ khó (dựa trên tiến độ kỹ năng)
        String difficulty = determineDifficulty(weakSkills, strongSkills, skillType);
        schedule.setRecommendedDifficulty(difficulty);
        
        // Tạo lý do đề xuất
        String reason = generateReason(skillType, weakSkills, strongSkills);
        schedule.setReason(reason);
        
        // Xác định độ ưu tiên
        int priority = calculatePriority(skillType, weakSkills);
        schedule.setPriority(priority);
        
        return schedule;
    }
    
    /**
     * Tính số phiên học trong ngày dựa trên thói quen
     */
    private int calculateSessionsPerDay(StudyHabit habit, int dayOfWeek) {
        if (habit == null) {
            return 2; // Mặc định 2 phiên/ngày
        }
        
        // Nếu là ngày ưa thích, học nhiều hơn
        if (habit.getPreferredDayOfWeek() == dayOfWeek) {
            return Math.min(MAX_SESSIONS_PER_DAY, 3);
        }
        
        // Dựa trên tần suất hàng tuần
        float weeklyFreq = habit.getWeeklyFrequency();
        if (weeklyFreq >= 7) {
            return 3; // Học mỗi ngày, nhiều phiên
        } else if (weeklyFreq >= 5) {
            return 2; // Học thường xuyên
        } else {
            return 1; // Học ít
        }
    }
    
    /**
     * Xác định giờ bắt đầu cho phiên học
     */
    private int determineStartHour(StudyHabit habit, int sessionIndex) {
        if (habit != null && habit.getPreferredHourOfDay() >= 0) {
            // Dựa trên giờ ưa thích
            int baseHour = habit.getPreferredHourOfDay();
            
            if (sessionIndex == 0) {
                return baseHour;
            } else if (sessionIndex == 1) {
                return (baseHour + 4) % 24; // Cách 4 tiếng
            } else {
                return (baseHour + 8) % 24; // Cách 8 tiếng
            }
        } else {
            // Mặc định: sáng (8h), chiều (14h), tối (19h)
            int[] defaultHours = {8, 14, 19};
            return defaultHours[Math.min(sessionIndex, 2)];
        }
    }
    
    /**
     * Chọn kỹ năng cho phiên học (ưu tiên yếu, xen kẽ mạnh)
     */
    private String selectSkillForSession(
            List<SkillProgress> weakSkills,
            List<SkillProgress> strongSkills,
            int sessionIndex) {
        
        // 70% ưu tiên kỹ năng yếu, 30% kỹ năng mạnh để duy trì
        if (!weakSkills.isEmpty() && (sessionIndex % 3 != 2)) {
            // Chọn kỹ năng yếu
            int index = sessionIndex % weakSkills.size();
            return weakSkills.get(index).getSkillType();
        } else if (!strongSkills.isEmpty()) {
            // Chọn kỹ năng mạnh
            int index = sessionIndex % strongSkills.size();
            return strongSkills.get(index).getSkillType();
        } else {
            // Nếu chưa có dữ liệu, chọn random
            SkillType[] skills = SkillType.values();
            return skills[sessionIndex % skills.length].name();
        }
    }
    
    /**
     * Xác định độ khó phù hợp
     */
    private String determineDifficulty(
            List<SkillProgress> weakSkills,
            List<SkillProgress> strongSkills,
            String skillType) {
        
        // Tìm progress của skill này
        SkillProgress progress = null;
        for (SkillProgress p : weakSkills) {
            if (p.getSkillType().equals(skillType)) {
                progress = p;
                break;
            }
        }
        if (progress == null) {
            for (SkillProgress p : strongSkills) {
                if (p.getSkillType().equals(skillType)) {
                    progress = p;
                    break;
                }
            }
        }
        
        if (progress == null) {
            return "EASY"; // Mặc định
        }
        
        // Dựa trên level và strength
        if (progress.getStrengthLevel().equals("WEAK")) {
            return "EASY"; // Kỹ năng yếu nên học dễ
        } else if (progress.getStrengthLevel().equals("STRONG")) {
            return progress.getLevel() >= 7 ? "HARD" : "MEDIUM";
        } else {
            return "MEDIUM";
        }
    }
    
    /**
     * Tạo lý do đề xuất
     */
    private String generateReason(
            String skillType,
            List<SkillProgress> weakSkills,
            List<SkillProgress> strongSkills) {
        
        boolean isWeak = false;
        SkillProgress progress = null;
        
        for (SkillProgress p : weakSkills) {
            if (p.getSkillType().equals(skillType)) {
                isWeak = true;
                progress = p;
                break;
            }
        }
        
        if (isWeak && progress != null) {
            return String.format(
                "Kỹ năng %s cần cải thiện (điểm TB: %.1f). Luyện tập thường xuyên sẽ giúp bạn tiến bộ nhanh hơn.",
                getSkillDisplayName(skillType),
                progress.getAverageScore()
            );
        }
        
        for (SkillProgress p : strongSkills) {
            if (p.getSkillType().equals(skillType)) {
                progress = p;
                break;
            }
        }
        
        if (progress != null) {
            return String.format(
                "Duy trì kỹ năng %s (điểm TB: %.1f). Tiếp tục luyện tập để giữ vững thành tích.",
                getSkillDisplayName(skillType),
                progress.getAverageScore()
            );
        }
        
        return String.format(
            "Luyện tập kỹ năng %s để nâng cao trình độ tiếng Anh tổng thể.",
            getSkillDisplayName(skillType)
        );
    }
    
    /**
     * Tính độ ưu tiên (1-5, cao hơn = quan trọng hơn)
     */
    private int calculatePriority(String skillType, List<SkillProgress> weakSkills) {
        // Kỹ năng yếu có priority cao hơn
        for (int i = 0; i < weakSkills.size(); i++) {
            if (weakSkills.get(i).getSkillType().equals(skillType)) {
                // Yếu nhất = priority 5, yếu thứ 2 = 4, ...
                return Math.max(5 - i, 3);
            }
        }
        
        return 2; // Kỹ năng không yếu = priority 2
    }
    
    /**
     * Lấy tên hiển thị của kỹ năng
     */
    private String getSkillDisplayName(String skillType) {
        try {
            SkillType skill = SkillType.valueOf(skillType);
            return skill.getDisplayName();
        } catch (Exception e) {
            return skillType;
        }
    }
    
    /**
     * Cập nhật lịch khi user hoàn thành
     */
    public void markScheduleCompleted(long scheduleId, float score, int durationMinutes) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            database.studyScheduleDao().markAsCompleted(
                scheduleId, 
                now, 
                score, 
                durationMinutes, 
                now
            );
        });
    }
    
    /**
     * Đánh dấu lịch bị bỏ qua
     */
    public void markScheduleSkipped(long scheduleId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            database.studyScheduleDao().markAsSkipped(scheduleId, System.currentTimeMillis());
        });
    }
    
    /**
     * Làm hết hạn các lịch cũ
     */
    public void expireOldSchedules(String userId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            long oneDayAgo = now - (24 * 60 * 60 * 1000);
            database.studyScheduleDao().expireOldSchedules(userId, oneDayAgo, now);
        });
    }
    
    /**
     * Xóa các lịch cũ đã hoàn thành/hết hạn (data cleanup)
     */
    public void cleanupOldSchedules(String userId, int daysToKeep) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long threshold = System.currentTimeMillis() - (daysToKeep * 24L * 60 * 60 * 1000);
            database.studyScheduleDao().deleteOldSchedules(userId, threshold);
        });
    }
    
    /**
     * Xóa các lịch học cũ (wrapper method cho compatibility)
     */
    public void deleteOldSchedules(String userId, int daysOld) {
        cleanupOldSchedules(userId, daysOld);
    }
}
