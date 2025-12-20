package vn.ltdidong.apphoctienganh.managers;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.ltdidong.apphoctienganh.database.AppDatabase;
import vn.ltdidong.apphoctienganh.models.LearningSession;
import vn.ltdidong.apphoctienganh.models.SkillProgress;
import vn.ltdidong.apphoctienganh.models.SkillType;
import vn.ltdidong.apphoctienganh.models.StudyHabit;

/**
 * Engine phân tích dữ liệu học tập
 * - Phân tích điểm mạnh/yếu theo từng kỹ năng
 * - Phân tích thói quen học tập (thời gian, tần suất)
 * - Tính toán xu hướng tiến bộ
 * - Cập nhật SkillProgress và StudyHabit
 */
public class LearningAnalyzer {
    
    private static final String TAG = "LearningAnalyzer";
    
    private final Context context;
    private final AppDatabase database;
    
    // Ngưỡng đánh giá
    private static final float STRONG_THRESHOLD = 75.0f;  // >= 75% là mạnh
    private static final float WEAK_THRESHOLD = 60.0f;    // < 60% là yếu
    
    public LearningAnalyzer(Context context) {
        this.context = context;
        this.database = AppDatabase.getDatabase(context);
    }
    
    /**
     * Callback cho analysis operations
     */
    public interface AnalysisCallback {
        void onAnalysisComplete(String message);
        void onAnalysisError(String error);
    }
    
    /**
     * Phân tích toàn bộ dữ liệu học tập của user
     * Cập nhật SkillProgress và StudyHabit
     */
    public void analyzeAllData(String userId, AnalysisCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Phân tích từng kỹ năng
                for (SkillType skillType : SkillType.values()) {
                    analyzeSkillProgress(userId, skillType.name());
                }
                
                // Phân tích thói quen học tập
                analyzeStudyHabit(userId);
                
                callback.onAnalysisComplete("Analysis completed successfully");
                
            } catch (Exception e) {
                Log.e(TAG, "Analysis error", e);
                callback.onAnalysisError(e.getMessage());
            }
        });
    }
    
    /**
     * Phân tích tiến độ của một kỹ năng cụ thể
     */
    public void analyzeSkillProgress(String userId, String skillType) {
        // Lấy tất cả phiên học của kỹ năng này
        List<LearningSession> sessions = 
            database.learningSessionDao().getBySkillType(userId, skillType);
        
        if (sessions.isEmpty()) {
            return; // Chưa có dữ liệu
        }
        
        // Tính toán các metrics
        int totalSessions = sessions.size();
        int completedSessions = 0;
        float totalScore = 0;
        float totalAccuracy = 0;
        long totalTime = 0;
        float highestScore = 0;
        float lowestScore = 100;
        
        for (LearningSession session : sessions) {
            if (session.isCompleted()) {
                completedSessions++;
                totalScore += session.getScore();
                totalAccuracy += session.getAccuracy();
                totalTime += session.getDurationSeconds();
                
                if (session.getScore() > highestScore) {
                    highestScore = session.getScore();
                }
                if (session.getScore() < lowestScore) {
                    lowestScore = session.getScore();
                }
            }
        }
        
        float averageScore = completedSessions > 0 ? totalScore / completedSessions : 0;
        float averageAccuracy = completedSessions > 0 ? totalAccuracy / completedSessions : 0;
        
        // Đếm số phiên trong 7 và 30 ngày gần nhất
        long now = System.currentTimeMillis();
        long sevenDaysAgo = now - (7L * 24 * 60 * 60 * 1000);
        long thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000);
        
        int practicesLast7Days = database.learningSessionDao()
            .countSessionsSince(userId, skillType, sevenDaysAgo);
        int practicesLast30Days = database.learningSessionDao()
            .countSessionsSince(userId, skillType, thirtyDaysAgo);
        
        // Tính xu hướng (so sánh 7 ngày gần nhất với 7 ngày trước đó)
        long fourteenDaysAgo = now - (14L * 24 * 60 * 60 * 1000);
        List<LearningSession> recentSessions = database.learningSessionDao()
            .getBySkillTypeAndTimeRange(userId, skillType, sevenDaysAgo, now);
        List<LearningSession> previousSessions = database.learningSessionDao()
            .getBySkillTypeAndTimeRange(userId, skillType, fourteenDaysAgo, sevenDaysAgo);
        
        String trend = calculateTrend(recentSessions, previousSessions);
        
        // Xác định mức độ mạnh/yếu
        String strengthLevel = determineStrengthLevel(averageScore);
        
        // Tính level và progress
        int level = calculateLevel(completedSessions, averageScore);
        float progressToNextLevel = calculateProgressToNextLevel(completedSessions, averageScore, level);
        
        // Cập nhật hoặc tạo mới SkillProgress
        SkillProgress progress = database.skillProgressDao().getBySkillType(userId, skillType);
        if (progress == null) {
            progress = new SkillProgress();
            progress.setUserId(userId);
            progress.setSkillType(skillType);
        }
        
        progress.setTotalSessions(totalSessions);
        progress.setCompletedSessions(completedSessions);
        progress.setAverageScore(averageScore);
        progress.setAverageAccuracy(averageAccuracy);
        progress.setTotalTimeSeconds(totalTime);
        progress.setHighestScore(highestScore);
        progress.setLowestScore(lowestScore);
        progress.setPracticesLast7Days(practicesLast7Days);
        progress.setPracticesLast30Days(practicesLast30Days);
        progress.setTrend(trend);
        progress.setLevel(level);
        progress.setProgressToNextLevel(progressToNextLevel);
        progress.setStrengthLevel(strengthLevel);
        progress.setLastUpdated(System.currentTimeMillis());
        progress.setSynced(false);
        
        database.skillProgressDao().insert(progress);
        
        Log.d(TAG, "Analyzed " + skillType + ": score=" + averageScore + 
                   ", level=" + level + ", trend=" + trend + ", strength=" + strengthLevel);
    }
    
    /**
     * Phân tích thói quen học tập
     */
    public void analyzeStudyHabit(String userId) {
        List<LearningSession> allSessions = 
            database.learningSessionDao().getAllByUser(userId);
        
        if (allSessions.isEmpty()) {
            return;
        }
        
        // Khởi tạo hoặc lấy StudyHabit hiện tại
        StudyHabit habit = database.studyHabitDao().getByUser(userId);
        if (habit == null) {
            habit = new StudyHabit();
            habit.setUserId(userId);
        }
        
        // Phân tích ngày và giờ ưa thích
        Map<Integer, Integer> dayFrequency = new HashMap<>();
        Map<Integer, Integer> hourFrequency = new HashMap<>();
        
        long totalDuration = 0;
        int completedCount = 0;
        
        for (LearningSession session : allSessions) {
            if (!session.isCompleted()) continue;
            
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(session.getStartTime());
            
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0-6
            int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
            
            dayFrequency.put(dayOfWeek, dayFrequency.getOrDefault(dayOfWeek, 0) + 1);
            hourFrequency.put(hourOfDay, hourFrequency.getOrDefault(hourOfDay, 0) + 1);
            
            totalDuration += session.getDurationSeconds();
            completedCount++;
        }
        
        // Tìm ngày và giờ ưa thích (cao nhất)
        int preferredDay = findMaxKey(dayFrequency);
        int preferredHour = findMaxKey(hourFrequency);
        
        // Tính thời lượng trung bình
        int avgSessionMinutes = completedCount > 0 ? 
            (int) (totalDuration / completedCount / 60) : 0;
        
        // Tính tần suất học
        float weeklyFrequency = calculateWeeklyFrequency(allSessions);
        float monthlyFrequency = calculateMonthlyFrequency(allSessions);
        
        // Tính streak
        int[] streaks = calculateStreaks(allSessions);
        int currentStreak = streaks[0];
        int longestStreak = streaks[1];
        int totalStudyDays = streaks[2];
        
        // Tìm kỹ năng luyện nhiều/ít nhất
        SkillProgress mostPracticed = database.skillProgressDao().getMostPracticedSkill(userId);
        SkillProgress leastPracticed = database.skillProgressDao().getLeastPracticedSkill(userId);
        
        String mostPracticedSkill = mostPracticed != null ? mostPracticed.getSkillType() : null;
        String leastPracticedSkill = leastPracticed != null ? leastPracticed.getSkillType() : null;
        
        // Xác định thời gian tốt nhất trong ngày
        String bestTimeOfDay = determineBestTimeOfDay(preferredHour);
        
        // Lấy ngày học cuối cùng
        LearningSession latestSession = database.learningSessionDao().getLatestSession(userId);
        long lastStudyDate = latestSession != null ? latestSession.getStartTime() : 0;
        
        // Cập nhật StudyHabit
        habit.setPreferredDayOfWeek(preferredDay);
        habit.setPreferredHourOfDay(preferredHour);
        habit.setAverageSessionMinutes(avgSessionMinutes);
        habit.setWeeklyFrequency(weeklyFrequency);
        habit.setMonthlyFrequency(monthlyFrequency);
        habit.setCurrentStreak(currentStreak);
        habit.setLongestStreak(longestStreak);
        habit.setTotalStudyDays(totalStudyDays);
        habit.setMostPracticedSkill(mostPracticedSkill);
        habit.setLeastPracticedSkill(leastPracticedSkill);
        habit.setBestTimeOfDay(bestTimeOfDay);
        habit.setLastStudyDate(lastStudyDate);
        habit.setLastUpdated(System.currentTimeMillis());
        habit.setSynced(false);
        
        database.studyHabitDao().insert(habit);
        
        Log.d(TAG, "Analyzed study habits: streak=" + currentStreak + 
                   ", avgMinutes=" + avgSessionMinutes + ", bestTime=" + bestTimeOfDay);
    }
    
    /**
     * Tính xu hướng bằng cách so sánh điểm trung bình 2 khoảng thời gian
     */
    private String calculateTrend(List<LearningSession> recent, List<LearningSession> previous) {
        if (recent.isEmpty() || previous.isEmpty()) {
            return "STABLE";
        }
        
        float recentAvg = 0;
        int recentCount = 0;
        for (LearningSession s : recent) {
            if (s.isCompleted()) {
                recentAvg += s.getScore();
                recentCount++;
            }
        }
        recentAvg = recentCount > 0 ? recentAvg / recentCount : 0;
        
        float previousAvg = 0;
        int previousCount = 0;
        for (LearningSession s : previous) {
            if (s.isCompleted()) {
                previousAvg += s.getScore();
                previousCount++;
            }
        }
        previousAvg = previousCount > 0 ? previousAvg / previousCount : 0;
        
        float diff = recentAvg - previousAvg;
        
        if (diff > 5) {
            return "IMPROVING";
        } else if (diff < -5) {
            return "DECLINING";
        } else {
            return "STABLE";
        }
    }
    
    /**
     * Xác định mức độ mạnh/yếu dựa trên điểm trung bình
     */
    private String determineStrengthLevel(float averageScore) {
        if (averageScore >= STRONG_THRESHOLD) {
            return "STRONG";
        } else if (averageScore < WEAK_THRESHOLD) {
            return "WEAK";
        } else {
            return "MEDIUM";
        }
    }
    
    /**
     * Tính level dựa trên số phiên hoàn thành và điểm số
     */
    private int calculateLevel(int completedSessions, float averageScore) {
        // Công thức: level = sqrt(completedSessions) * (averageScore/100) + 1
        // Tối đa level 10
        int baseLevel = (int) Math.sqrt(completedSessions);
        float scoreMultiplier = averageScore / 100.0f;
        int level = (int) (baseLevel * scoreMultiplier) + 1;
        return Math.min(level, 10);
    }
    
    /**
     * Tính % tiến độ đến level tiếp theo
     */
    private float calculateProgressToNextLevel(int completedSessions, float averageScore, int currentLevel) {
        int nextLevelRequired = (int) Math.pow(currentLevel, 2);
        if (completedSessions >= nextLevelRequired) {
            return 100.0f;
        }
        return (completedSessions * 100.0f) / nextLevelRequired;
    }
    
    /**
     * Tính tần suất học hàng tuần (số phiên/tuần)
     */
    private float calculateWeeklyFrequency(List<LearningSession> sessions) {
        if (sessions.isEmpty()) return 0;
        
        long firstSession = Long.MAX_VALUE;
        long lastSession = 0;
        int count = 0;
        
        for (LearningSession s : sessions) {
            if (s.isCompleted()) {
                if (s.getStartTime() < firstSession) firstSession = s.getStartTime();
                if (s.getStartTime() > lastSession) lastSession = s.getStartTime();
                count++;
            }
        }
        
        if (count == 0) return 0;
        
        long durationMillis = lastSession - firstSession;
        float weeks = durationMillis / (7.0f * 24 * 60 * 60 * 1000);
        
        if (weeks < 1) weeks = 1;
        
        return count / weeks;
    }
    
    /**
     * Tính tần suất học hàng tháng (số phiên/tháng)
     */
    private float calculateMonthlyFrequency(List<LearningSession> sessions) {
        if (sessions.isEmpty()) return 0;
        
        long firstSession = Long.MAX_VALUE;
        long lastSession = 0;
        int count = 0;
        
        for (LearningSession s : sessions) {
            if (s.isCompleted()) {
                if (s.getStartTime() < firstSession) firstSession = s.getStartTime();
                if (s.getStartTime() > lastSession) lastSession = s.getStartTime();
                count++;
            }
        }
        
        if (count == 0) return 0;
        
        long durationMillis = lastSession - firstSession;
        float months = durationMillis / (30.0f * 24 * 60 * 60 * 1000);
        
        if (months < 1) months = 1;
        
        return count / months;
    }
    
    /**
     * Tính current streak, longest streak và total study days
     * @return int[3]: [currentStreak, longestStreak, totalStudyDays]
     */
    private int[] calculateStreaks(List<LearningSession> sessions) {
        if (sessions.isEmpty()) {
            return new int[]{0, 0, 0};
        }
        
        // Tạo set các ngày đã học (chỉ lấy ngày, bỏ giờ)
        List<Long> studyDates = new ArrayList<>();
        for (LearningSession s : sessions) {
            if (s.isCompleted()) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(s.getStartTime());
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                
                long dayTimestamp = cal.getTimeInMillis();
                if (!studyDates.contains(dayTimestamp)) {
                    studyDates.add(dayTimestamp);
                }
            }
        }
        
        // Sort dates
        studyDates.sort((a, b) -> Long.compare(b, a)); // Descending
        
        int totalStudyDays = studyDates.size();
        if (totalStudyDays == 0) {
            return new int[]{0, 0, 0};
        }
        
        // Tính current streak
        int currentStreak = 1;
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long todayTimestamp = today.getTimeInMillis();
        
        // Kiểm tra có học hôm nay hoặc hôm qua không
        if (studyDates.get(0) == todayTimestamp || 
            studyDates.get(0) == todayTimestamp - (24 * 60 * 60 * 1000)) {
            
            for (int i = 1; i < studyDates.size(); i++) {
                long diff = studyDates.get(i-1) - studyDates.get(i);
                long oneDayMillis = 24 * 60 * 60 * 1000;
                
                if (diff == oneDayMillis) {
                    currentStreak++;
                } else {
                    break;
                }
            }
        } else {
            currentStreak = 0; // Streak bị gián đoạn
        }
        
        // Tính longest streak
        int longestStreak = 1;
        int tempStreak = 1;
        
        for (int i = 1; i < studyDates.size(); i++) {
            long diff = studyDates.get(i-1) - studyDates.get(i);
            long oneDayMillis = 24 * 60 * 60 * 1000;
            
            if (diff == oneDayMillis) {
                tempStreak++;
                if (tempStreak > longestStreak) {
                    longestStreak = tempStreak;
                }
            } else {
                tempStreak = 1;
            }
        }
        
        return new int[]{currentStreak, longestStreak, totalStudyDays};
    }
    
    /**
     * Xác định thời gian tốt nhất trong ngày
     */
    private String determineBestTimeOfDay(int hour) {
        if (hour >= 5 && hour < 12) {
            return "MORNING";
        } else if (hour >= 12 && hour < 17) {
            return "AFTERNOON";
        } else if (hour >= 17 && hour < 21) {
            return "EVENING";
        } else {
            return "NIGHT";
        }
    }
    
    /**
     * Tìm key có value cao nhất trong map
     */
    private int findMaxKey(Map<Integer, Integer> map) {
        if (map.isEmpty()) return -1;
        
        int maxKey = -1;
        int maxValue = 0;
        
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getValue() > maxValue) {
                maxValue = entry.getValue();
                maxKey = entry.getKey();
            }
        }
        
        return maxKey;
    }
    
    /**
     * Lấy danh sách kỹ năng yếu cần cải thiện
     */
    public List<SkillProgress> getWeakSkills(String userId) {
        return database.skillProgressDao().getWeakSkills(userId, WEAK_THRESHOLD);
    }
    
    /**
     * Lấy danh sách kỹ năng mạnh
     */
    public List<SkillProgress> getStrongSkills(String userId) {
        return database.skillProgressDao().getStrongSkills(userId, STRONG_THRESHOLD);
    }
    
    /**
     * Lấy thói quen học tập
     */
    public StudyHabit getStudyHabit(String userId) {
        return database.studyHabitDao().getByUser(userId);
    }
}
