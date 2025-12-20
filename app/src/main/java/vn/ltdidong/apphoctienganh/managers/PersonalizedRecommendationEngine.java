package vn.ltdidong.apphoctienganh.managers;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import vn.ltdidong.apphoctienganh.database.AppDatabase;
import vn.ltdidong.apphoctienganh.models.LearningSession;
import vn.ltdidong.apphoctienganh.models.SkillProgress;
import vn.ltdidong.apphoctienganh.models.SkillType;
import vn.ltdidong.apphoctienganh.models.StudyHabit;
import vn.ltdidong.apphoctienganh.models.StudySchedule;

/**
 * Engine đề xuất học tập cá nhân hóa dựa trên:
 * - Phân tích điểm mạnh/yếu
 * - Thói quen học tập
 * - Lịch sử học tập
 * - Mục tiêu cá nhân
 */
public class PersonalizedRecommendationEngine {
    
    private static final String TAG = "RecommendationEngine";
    
    private final Context context;
    private final AppDatabase database;
    private final LearningAnalyzer analyzer;
    
    public PersonalizedRecommendationEngine(Context context) {
        this.context = context;
        this.database = AppDatabase.getDatabase(context);
        this.analyzer = new LearningAnalyzer(context);
    }
    
    /**
     * Model cho một gợi ý học tập
     */
    public static class Recommendation {
        public String title;
        public String description;
        public String skillType;
        public String actionType; // PRACTICE, REVIEW, TEST, LEARN_NEW
        public int priority; // 1-5, cao hơn = quan trọng hơn
        public String difficulty; // EASY, MEDIUM, HARD
        public int estimatedMinutes;
        
        public Recommendation(String title, String description, String skillType, 
                            String actionType, int priority, String difficulty, int estimatedMinutes) {
            this.title = title;
            this.description = description;
            this.skillType = skillType;
            this.actionType = actionType;
            this.priority = priority;
            this.difficulty = difficulty;
            this.estimatedMinutes = estimatedMinutes;
        }
    }
    
    /**
     * Lấy danh sách gợi ý học tập cá nhân hóa
     */
    public List<Recommendation> getPersonalizedRecommendations(String userId) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        try {
            // Lấy dữ liệu phân tích
            StudyHabit habit = database.studyHabitDao().getByUser(userId);
            List<SkillProgress> allProgress = database.skillProgressDao().getAllByUser(userId);
            List<SkillProgress> weakSkills = analyzer.getWeakSkills(userId);
            List<SkillProgress> strongSkills = analyzer.getStrongSkills(userId);
            List<StudySchedule> pendingSchedules = database.studyScheduleDao().getPendingSchedules(userId);
            
            // 1. Gợi ý dựa trên lịch học đã tạo
            recommendations.addAll(getScheduleBasedRecommendations(pendingSchedules));
            
            // 2. Gợi ý cải thiện kỹ năng yếu
            recommendations.addAll(getWeakSkillRecommendations(weakSkills));
            
            // 3. Gợi ý duy trì kỹ năng mạnh
            recommendations.addAll(getStrongSkillRecommendations(strongSkills));
            
            // 4. Gợi ý dựa trên streak
            if (habit != null) {
                recommendations.addAll(getStreakRecommendations(userId, habit));
            }
            
            // 5. Gợi ý dựa trên thời gian không học
            recommendations.addAll(getInactivityRecommendations(userId, habit));
            
            // 6. Gợi ý kiểm tra tiến độ
            recommendations.addAll(getProgressTestRecommendations(userId, allProgress));
            
            // Sắp xếp theo priority (cao nhất trước)
            recommendations.sort((a, b) -> Integer.compare(b.priority, a.priority));
            
            // Giới hạn số lượng gợi ý (top 10)
            if (recommendations.size() > 10) {
                recommendations = recommendations.subList(0, 10);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating recommendations", e);
        }
        
        return recommendations;
    }
    
    /**
     * Gợi ý từ lịch học đã tạo
     */
    private List<Recommendation> getScheduleBasedRecommendations(List<StudySchedule> schedules) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // Lấy tối đa 3 lịch ưu tiên cao nhất hôm nay/ngày mai
        long now = System.currentTimeMillis();
        long tomorrow = now + (24 * 60 * 60 * 1000);
        
        int count = 0;
        for (StudySchedule schedule : schedules) {
            if (schedule.getScheduledDate() <= tomorrow && count < 3) {
                String title = String.format("Lịch học %s hôm nay", 
                    getSkillDisplayName(schedule.getSkillType()));
                
                recommendations.add(new Recommendation(
                    title,
                    schedule.getReason(),
                    schedule.getSkillType(),
                    "PRACTICE",
                    schedule.getPriority(),
                    schedule.getRecommendedDifficulty(),
                    schedule.getRecommendedDurationMinutes()
                ));
                
                count++;
            }
        }
        
        return recommendations;
    }
    
    /**
     * Gợi ý cải thiện kỹ năng yếu
     */
    private List<Recommendation> getWeakSkillRecommendations(List<SkillProgress> weakSkills) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        for (SkillProgress skill : weakSkills) {
            if (recommendations.size() >= 3) break; // Tối đa 3 gợi ý
            
            String skillName = getSkillDisplayName(skill.getSkillType());
            String title = String.format("Cải thiện %s", skillName);
            
            String description;
            if (skill.getTrend().equals("DECLINING")) {
                description = String.format(
                    "Kỹ năng %s đang giảm sút (điểm TB: %.1f). Cần luyện tập ngay để cải thiện.",
                    skillName, skill.getAverageScore()
                );
            } else {
                description = String.format(
                    "Kỹ năng %s còn yếu (điểm TB: %.1f). Luyện tập %d phút mỗi ngày sẽ giúp bạn tiến bộ nhanh.",
                    skillName, skill.getAverageScore(), 
                    calculateRecommendedDuration(skill)
                );
            }
            
            recommendations.add(new Recommendation(
                title,
                description,
                skill.getSkillType(),
                "PRACTICE",
                5, // Priority cao nhất
                "EASY", // Bắt đầu từ dễ
                calculateRecommendedDuration(skill)
            ));
        }
        
        return recommendations;
    }
    
    /**
     * Gợi ý duy trì kỹ năng mạnh
     */
    private List<Recommendation> getStrongSkillRecommendations(List<SkillProgress> strongSkills) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // Chỉ gợi ý 1-2 kỹ năng mạnh để duy trì
        int count = 0;
        for (SkillProgress skill : strongSkills) {
            if (count >= 2) break;
            
            // Chỉ gợi ý nếu đã lâu không luyện (> 3 ngày)
            long threeDaysAgo = System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000);
            int recentPractices = database.learningSessionDao()
                .countSessionsSince(skill.getUserId(), skill.getSkillType(), threeDaysAgo);
            
            if (recentPractices == 0) {
                String skillName = getSkillDisplayName(skill.getSkillType());
                String title = String.format("Duy trì %s", skillName);
                String description = String.format(
                    "Bạn đang rất giỏi %s (điểm TB: %.1f). Luyện tập định kỳ để giữ vững kỹ năng.",
                    skillName, skill.getAverageScore()
                );
                
                recommendations.add(new Recommendation(
                    title,
                    description,
                    skill.getSkillType(),
                    "REVIEW",
                    2, // Priority thấp hơn weak skills
                    skill.getLevel() >= 7 ? "HARD" : "MEDIUM",
                    20 // Thời gian ngắn hơn
                ));
                
                count++;
            }
        }
        
        return recommendations;
    }
    
    /**
     * Gợi ý dựa trên streak (chuỗi ngày học)
     */
    private List<Recommendation> getStreakRecommendations(String userId, StudyHabit habit) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        long now = System.currentTimeMillis();
        long lastStudy = habit.getLastStudyDate();
        long hoursSinceLastStudy = (now - lastStudy) / (60 * 60 * 1000);
        
        // Nếu streak > 0 và sắp mất streak (> 20 giờ chưa học)
        if (habit.getCurrentStreak() > 0 && hoursSinceLastStudy > 20) {
            String title = "Giữ streak " + habit.getCurrentStreak() + " ngày!";
            String description = String.format(
                "Bạn đã học liên tục %d ngày. Hãy tiếp tục streak bằng cách học ít nhất 15 phút hôm nay!",
                habit.getCurrentStreak()
            );
            
            // Chọn kỹ năng luyện nhiều nhất để dễ dàng
            String skillType = habit.getMostPracticedSkill();
            if (skillType == null) {
                skillType = SkillType.LISTENING.name();
            }
            
            recommendations.add(new Recommendation(
                title,
                description,
                skillType,
                "PRACTICE",
                5, // Priority rất cao
                "EASY",
                15 // Thời gian ngắn
            ));
        }
        
        // Nếu đạt milestone streak (7, 14, 30, 60, 100 ngày)
        int streak = habit.getCurrentStreak();
        int[] milestones = {7, 14, 30, 60, 100};
        for (int milestone : milestones) {
            if (streak == milestone) {
                String title = "🎉 Chúc mừng " + milestone + " ngày streak!";
                String description = String.format(
                    "Bạn thật tuyệt vời! Hãy thử thách bản thân với bài tập khó hơn để kiểm tra tiến bộ."
                );
                
                recommendations.add(new Recommendation(
                    title,
                    description,
                    SkillType.LISTENING.name(), // Any skill
                    "TEST",
                    4,
                    "HARD",
                    30
                ));
                break;
            }
        }
        
        return recommendations;
    }
    
    /**
     * Gợi ý khi không hoạt động lâu
     */
    private List<Recommendation> getInactivityRecommendations(String userId, StudyHabit habit) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        if (habit == null) return recommendations;
        
        long now = System.currentTimeMillis();
        long lastStudy = habit.getLastStudyDate();
        long daysSinceLastStudy = (now - lastStudy) / (24 * 60 * 60 * 1000);
        
        // Nếu không học > 3 ngày
        if (daysSinceLastStudy >= 3) {
            String title = "Đã lâu không gặp bạn!";
            String description = String.format(
                "Bạn đã không học %d ngày rồi. Hãy quay lại với bài học nhẹ nhàng để làm quen lại nhé!",
                daysSinceLastStudy
            );
            
            // Chọn kỹ năng yêu thích
            String skillType = habit.getMostPracticedSkill();
            if (skillType == null) {
                skillType = SkillType.LISTENING.name();
            }
            
            recommendations.add(new Recommendation(
                title,
                description,
                skillType,
                "PRACTICE",
                5, // Priority cao
                "EASY", // Bắt đầu lại từ dễ
                20
            ));
        }
        
        return recommendations;
    }
    
    /**
     * Gợi ý kiểm tra tiến độ
     */
    private List<Recommendation> getProgressTestRecommendations(String userId, List<SkillProgress> allProgress) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // Đề xuất test sau mỗi 10 phiên học
        for (SkillProgress progress : allProgress) {
            if (progress.getCompletedSessions() > 0 && 
                progress.getCompletedSessions() % 10 == 0) {
                
                // Kiểm tra xem có test gần đây không
                long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
                int recentTests = database.learningSessionDao()
                    .countSessionsSince(userId, progress.getSkillType(), sevenDaysAgo);
                
                if (recentTests < 2) { // Chưa test nhiều gần đây
                    String skillName = getSkillDisplayName(progress.getSkillType());
                    String title = String.format("Kiểm tra %s", skillName);
                    String description = String.format(
                        "Bạn đã hoàn thành %d phiên học %s. Hãy làm bài kiểm tra để đánh giá tiến bộ!",
                        progress.getCompletedSessions(), skillName
                    );
                    
                    recommendations.add(new Recommendation(
                        title,
                        description,
                        progress.getSkillType(),
                        "TEST",
                        3,
                        "MEDIUM",
                        30
                    ));
                }
            }
        }
        
        return recommendations;
    }
    
    /**
     * Tính thời lượng học đề xuất dựa trên tiến độ
     */
    private int calculateRecommendedDuration(SkillProgress progress) {
        // Yếu hơn = học lâu hơn
        if (progress.getAverageScore() < 50) {
            return 40; // 40 phút
        } else if (progress.getAverageScore() < 60) {
            return 30; // 30 phút
        } else {
            return 20; // 20 phút
        }
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
     * Lấy gợi ý nhanh cho hôm nay
     */
    public Recommendation getQuickRecommendation(String userId) {
        List<Recommendation> all = getPersonalizedRecommendations(userId);
        return all.isEmpty() ? getDefaultRecommendation() : all.get(0);
    }
    
    /**
     * Gợi ý mặc định khi chưa có dữ liệu
     */
    private Recommendation getDefaultRecommendation() {
        return new Recommendation(
            "Bắt đầu học hôm nay",
            "Hãy bắt đầu hành trình học tiếng Anh với bài học Nghe đầu tiên!",
            SkillType.LISTENING.name(),
            "LEARN_NEW",
            3,
            "EASY",
            30
        );
    }
    
    /**
     * Lấy thống kê tổng quan cho dashboard
     */
    public String getDailySummary(String userId) {
        try {
            StudyHabit habit = database.studyHabitDao().getByUser(userId);
            List<SkillProgress> weakSkills = analyzer.getWeakSkills(userId);
            List<StudySchedule> todaySchedules = getTodaySchedules(userId);
            
            StringBuilder summary = new StringBuilder();
            
            if (habit != null && habit.getCurrentStreak() > 0) {
                summary.append(String.format("🔥 Streak: %d ngày\n", habit.getCurrentStreak()));
            }
            
            if (!todaySchedules.isEmpty()) {
                summary.append(String.format("📅 Hôm nay: %d lịch học\n", todaySchedules.size()));
            }
            
            if (!weakSkills.isEmpty()) {
                summary.append(String.format("💪 Cần cải thiện: %d kỹ năng\n", weakSkills.size()));
            }
            
            if (summary.length() == 0) {
                summary.append("Bắt đầu học để xem thống kê của bạn!");
            }
            
            return summary.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating summary", e);
            return "Chào mừng bạn đến với app học tiếng Anh!";
        }
    }
    
    /**
     * Lấy lịch học hôm nay
     */
    private List<StudySchedule> getTodaySchedules(String userId) {
        long now = System.currentTimeMillis();
        return database.studyScheduleDao().getByDate(userId, now);
    }
}
