package vn.ltdidong.apphoctienganh.managers;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import vn.ltdidong.apphoctienganh.database.AppDatabase;
import vn.ltdidong.apphoctienganh.models.LearningSession;
import vn.ltdidong.apphoctienganh.models.SkillProgress;
import vn.ltdidong.apphoctienganh.models.StudyHabit;
import vn.ltdidong.apphoctienganh.models.StudySchedule;

/**
 * Manager chịu trách nhiệm đồng bộ dữ liệu giữa SQLite (local) và Firestore (cloud)
 * Hỗ trợ:
 * - Upload dữ liệu local lên Firestore khi có kết nối
 * - Download dữ liệu từ Firestore khi đăng nhập
 * - Xử lý conflict resolution (ưu tiên dữ liệu mới nhất)
 */
public class DataSyncManager {
    
    private static final String TAG = "DataSyncManager";
    
    // Firestore collections
    private static final String COLLECTION_LEARNING_SESSIONS = "learning_sessions";
    private static final String COLLECTION_SKILL_PROGRESS = "skill_progress";
    private static final String COLLECTION_STUDY_HABITS = "study_habits";
    private static final String COLLECTION_STUDY_SCHEDULES = "study_schedules";
    
    private final Context context;
    private final AppDatabase database;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    
    public DataSyncManager(Context context) {
        this.context = context;
        this.database = AppDatabase.getDatabase(context);
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }
    
    /**
     * Interface callback cho sync operations
     */
    public interface SyncCallback {
        void onSyncSuccess(String message);
        void onSyncError(String error);
        void onSyncProgress(int current, int total);
    }
    
    /**
     * Đồng bộ toàn bộ dữ liệu (upload + download)
     */
    public void syncAll(SyncCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onSyncError("User not logged in");
            return;
        }
        
        String userId = user.getUid();
        
        // Upload local data first
        uploadAllData(userId, new SyncCallback() {
            @Override
            public void onSyncSuccess(String message) {
                // Then download cloud data
                downloadAllData(userId, callback);
            }
            
            @Override
            public void onSyncError(String error) {
                callback.onSyncError("Upload error: " + error);
            }
            
            @Override
            public void onSyncProgress(int current, int total) {
                callback.onSyncProgress(current, total);
            }
        });
    }
    
    /**
     * Upload toàn bộ dữ liệu local chưa đồng bộ lên Firestore
     */
    public void uploadAllData(String userId, SyncCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Upload Learning Sessions
                List<LearningSession> unsyncedSessions = 
                    database.learningSessionDao().getUnsyncedSessions(userId);
                uploadLearningSessions(userId, unsyncedSessions, callback);
                
                // Upload Skill Progress
                List<SkillProgress> unsyncedProgress = 
                    database.skillProgressDao().getUnsyncedProgress(userId);
                uploadSkillProgress(userId, unsyncedProgress, callback);
                
                // Upload Study Habit
                StudyHabit unsyncedHabit = 
                    database.studyHabitDao().getUnsyncedHabit(userId);
                if (unsyncedHabit != null) {
                    uploadStudyHabit(userId, unsyncedHabit, callback);
                }
                
                // Upload Study Schedules
                List<StudySchedule> unsyncedSchedules = 
                    database.studyScheduleDao().getUnsyncedSchedules(userId);
                uploadStudySchedules(userId, unsyncedSchedules, callback);
                
                callback.onSyncSuccess("Upload completed");
                
            } catch (Exception e) {
                Log.e(TAG, "Upload error", e);
                callback.onSyncError(e.getMessage());
            }
        });
    }
    
    /**
     * Upload Learning Sessions lên Firestore
     */
    private void uploadLearningSessions(String userId, List<LearningSession> sessions, SyncCallback callback) {
        if (sessions.isEmpty()) return;
        
        WriteBatch batch = firestore.batch();
        int count = 0;
        
        for (LearningSession session : sessions) {
            DocumentReference docRef = firestore.collection(COLLECTION_LEARNING_SESSIONS).document();
            Map<String, Object> data = convertSessionToMap(session);
            batch.set(docRef, data);
            
            // Update local record với Firestore ID
            String firestoreId = docRef.getId();
            AppDatabase.databaseWriteExecutor.execute(() -> 
                database.learningSessionDao().markAsSynced(session.getId(), firestoreId)
            );
            
            count++;
            if (count % 50 == 0) { // Batch limit in Firestore
                final int progress = count;
                batch.commit().addOnSuccessListener(aVoid -> 
                    callback.onSyncProgress(progress, sessions.size())
                );
                batch = firestore.batch();
            }
        }
        
        // Commit remaining
        if (count % 50 != 0) {
            final int finalCount = count;
            batch.commit().addOnSuccessListener(aVoid -> 
                callback.onSyncProgress(finalCount, sessions.size())
            );
        }
    }
    
    /**
     * Upload Skill Progress lên Firestore
     */
    private void uploadSkillProgress(String userId, List<SkillProgress> progressList, SyncCallback callback) {
        if (progressList.isEmpty()) return;
        
        for (SkillProgress progress : progressList) {
            String docId = userId + "_" + progress.getSkillType();
            Map<String, Object> data = convertProgressToMap(progress);
            
            firestore.collection(COLLECTION_SKILL_PROGRESS)
                .document(docId)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> 
                        database.skillProgressDao().markAsSynced(progress.getId())
                    );
                })
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Error uploading skill progress", e)
                );
        }
    }
    
    /**
     * Upload Study Habit lên Firestore
     */
    private void uploadStudyHabit(String userId, StudyHabit habit, SyncCallback callback) {
        Map<String, Object> data = convertHabitToMap(habit);
        
        firestore.collection(COLLECTION_STUDY_HABITS)
            .document(userId)
            .set(data)
            .addOnSuccessListener(aVoid -> {
                AppDatabase.databaseWriteExecutor.execute(() -> 
                    database.studyHabitDao().markAsSynced(habit.getId())
                );
            })
            .addOnFailureListener(e -> 
                Log.e(TAG, "Error uploading study habit", e)
            );
    }
    
    /**
     * Upload Study Schedules lên Firestore
     */
    private void uploadStudySchedules(String userId, List<StudySchedule> schedules, SyncCallback callback) {
        if (schedules.isEmpty()) return;
        
        WriteBatch batch = firestore.batch();
        int count = 0;
        
        for (StudySchedule schedule : schedules) {
            DocumentReference docRef;
            if (schedule.getFirestoreId() != null) {
                docRef = firestore.collection(COLLECTION_STUDY_SCHEDULES)
                    .document(schedule.getFirestoreId());
            } else {
                docRef = firestore.collection(COLLECTION_STUDY_SCHEDULES).document();
            }
            
            Map<String, Object> data = convertScheduleToMap(schedule);
            batch.set(docRef, data);
            
            String firestoreId = docRef.getId();
            AppDatabase.databaseWriteExecutor.execute(() -> 
                database.studyScheduleDao().markAsSynced(schedule.getId(), firestoreId)
            );
            
            count++;
            if (count % 50 == 0) {
                batch.commit();
                batch = firestore.batch();
            }
        }
        
        if (count % 50 != 0) {
            batch.commit();
        }
    }
    
    /**
     * Download toàn bộ dữ liệu từ Firestore về local
     */
    public void downloadAllData(String userId, SyncCallback callback) {
        // Download Learning Sessions
        downloadLearningSessions(userId, new SyncCallback() {
            @Override
            public void onSyncSuccess(String message) {
                // Download Skill Progress
                downloadSkillProgress(userId, new SyncCallback() {
                    @Override
                    public void onSyncSuccess(String message) {
                        // Download Study Habit
                        downloadStudyHabit(userId, new SyncCallback() {
                            @Override
                            public void onSyncSuccess(String message) {
                                // Download Study Schedules
                                downloadStudySchedules(userId, callback);
                            }
                            
                            @Override
                            public void onSyncError(String error) {
                                callback.onSyncError(error);
                            }
                            
                            @Override
                            public void onSyncProgress(int current, int total) {
                                callback.onSyncProgress(current, total);
                            }
                        });
                    }
                    
                    @Override
                    public void onSyncError(String error) {
                        callback.onSyncError(error);
                    }
                    
                    @Override
                    public void onSyncProgress(int current, int total) {
                        callback.onSyncProgress(current, total);
                    }
                });
            }
            
            @Override
            public void onSyncError(String error) {
                callback.onSyncError(error);
            }
            
            @Override
            public void onSyncProgress(int current, int total) {
                callback.onSyncProgress(current, total);
            }
        });
    }
    
    /**
     * Download Learning Sessions từ Firestore
     */
    private void downloadLearningSessions(String userId, SyncCallback callback) {
        firestore.collection(COLLECTION_LEARNING_SESSIONS)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<LearningSession> sessions = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    LearningSession session = convertMapToSession(doc.getData());
                    session.setFirestoreId(doc.getId());
                    session.setSynced(true);
                    sessions.add(session);
                }
                
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    database.learningSessionDao().insertAll(sessions);
                    callback.onSyncSuccess("Downloaded " + sessions.size() + " sessions");
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error downloading sessions", e);
                callback.onSyncError(e.getMessage());
            });
    }
    
    /**
     * Download Skill Progress từ Firestore
     */
    private void downloadSkillProgress(String userId, SyncCallback callback) {
        firestore.collection(COLLECTION_SKILL_PROGRESS)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<SkillProgress> progressList = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    SkillProgress progress = convertMapToProgress(doc.getData());
                    progress.setSynced(true);
                    progressList.add(progress);
                }
                
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    database.skillProgressDao().insertAll(progressList);
                    callback.onSyncSuccess("Downloaded " + progressList.size() + " progress records");
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error downloading skill progress", e);
                callback.onSyncError(e.getMessage());
            });
    }
    
    /**
     * Download Study Habit từ Firestore
     */
    private void downloadStudyHabit(String userId, SyncCallback callback) {
        firestore.collection(COLLECTION_STUDY_HABITS)
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    StudyHabit habit = convertMapToHabit(documentSnapshot.getData());
                    habit.setSynced(true);
                    
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        database.studyHabitDao().insert(habit);
                        callback.onSyncSuccess("Downloaded study habit");
                    });
                } else {
                    callback.onSyncSuccess("No study habit found");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error downloading study habit", e);
                callback.onSyncError(e.getMessage());
            });
    }
    
    /**
     * Download Study Schedules từ Firestore
     */
    private void downloadStudySchedules(String userId, SyncCallback callback) {
        firestore.collection(COLLECTION_STUDY_SCHEDULES)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<StudySchedule> schedules = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    StudySchedule schedule = convertMapToSchedule(doc.getData());
                    schedule.setFirestoreId(doc.getId());
                    schedule.setSynced(true);
                    schedules.add(schedule);
                }
                
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    database.studyScheduleDao().insertAll(schedules);
                    callback.onSyncSuccess("Download completed. Total " + schedules.size() + " schedules");
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error downloading schedules", e);
                callback.onSyncError(e.getMessage());
            });
    }
    
    // === Conversion Methods: Model to Map (for Firestore) ===
    
    private Map<String, Object> convertSessionToMap(LearningSession session) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", session.getUserId());
        map.put("skillType", session.getSkillType());
        map.put("lessonId", session.getLessonId());
        map.put("lessonName", session.getLessonName());
        map.put("score", session.getScore());
        map.put("correctAnswers", session.getCorrectAnswers());
        map.put("totalQuestions", session.getTotalQuestions());
        map.put("accuracy", session.getAccuracy());
        map.put("startTime", session.getStartTime());
        map.put("endTime", session.getEndTime());
        map.put("durationSeconds", session.getDurationSeconds());
        map.put("difficulty", session.getDifficulty());
        map.put("completed", session.isCompleted());
        map.put("createdAt", session.getCreatedAt());
        return map;
    }
    
    private Map<String, Object> convertProgressToMap(SkillProgress progress) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", progress.getUserId());
        map.put("skillType", progress.getSkillType());
        map.put("totalSessions", progress.getTotalSessions());
        map.put("completedSessions", progress.getCompletedSessions());
        map.put("averageScore", progress.getAverageScore());
        map.put("averageAccuracy", progress.getAverageAccuracy());
        map.put("totalTimeSeconds", progress.getTotalTimeSeconds());
        map.put("highestScore", progress.getHighestScore());
        map.put("lowestScore", progress.getLowestScore());
        map.put("practicesLast7Days", progress.getPracticesLast7Days());
        map.put("practicesLast30Days", progress.getPracticesLast30Days());
        map.put("trend", progress.getTrend());
        map.put("level", progress.getLevel());
        map.put("progressToNextLevel", progress.getProgressToNextLevel());
        map.put("strengthLevel", progress.getStrengthLevel());
        map.put("lastUpdated", progress.getLastUpdated());
        return map;
    }
    
    private Map<String, Object> convertHabitToMap(StudyHabit habit) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", habit.getUserId());
        map.put("preferredDayOfWeek", habit.getPreferredDayOfWeek());
        map.put("preferredHourOfDay", habit.getPreferredHourOfDay());
        map.put("averageSessionMinutes", habit.getAverageSessionMinutes());
        map.put("weeklyFrequency", habit.getWeeklyFrequency());
        map.put("monthlyFrequency", habit.getMonthlyFrequency());
        map.put("currentStreak", habit.getCurrentStreak());
        map.put("longestStreak", habit.getLongestStreak());
        map.put("totalStudyDays", habit.getTotalStudyDays());
        map.put("mostPracticedSkill", habit.getMostPracticedSkill());
        map.put("leastPracticedSkill", habit.getLeastPracticedSkill());
        map.put("bestTimeOfDay", habit.getBestTimeOfDay());
        map.put("lastStudyDate", habit.getLastStudyDate());
        map.put("lastUpdated", habit.getLastUpdated());
        return map;
    }
    
    private Map<String, Object> convertScheduleToMap(StudySchedule schedule) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", schedule.getUserId());
        map.put("scheduledDate", schedule.getScheduledDate());
        map.put("skillType", schedule.getSkillType());
        map.put("recommendedDurationMinutes", schedule.getRecommendedDurationMinutes());
        map.put("recommendedDifficulty", schedule.getRecommendedDifficulty());
        map.put("recommendedStartHour", schedule.getRecommendedStartHour());
        map.put("reason", schedule.getReason());
        map.put("priority", schedule.getPriority());
        map.put("status", schedule.getStatus());
        map.put("actualCompletedAt", schedule.getActualCompletedAt());
        map.put("actualScore", schedule.getActualScore());
        map.put("actualDurationMinutes", schedule.getActualDurationMinutes());
        map.put("createdAt", schedule.getCreatedAt());
        map.put("lastUpdated", schedule.getLastUpdated());
        map.put("notificationSent", schedule.isNotificationSent());
        return map;
    }
    
    // === Conversion Methods: Map to Model (from Firestore) ===
    
    private LearningSession convertMapToSession(Map<String, Object> map) {
        LearningSession session = new LearningSession();
        session.setUserId((String) map.get("userId"));
        session.setSkillType((String) map.get("skillType"));
        
        Object lessonId = map.get("lessonId");
        if (lessonId != null) {
            session.setLessonId(((Number) lessonId).intValue());
        }
        
        session.setLessonName((String) map.get("lessonName"));
        session.setScore(((Number) map.get("score")).floatValue());
        session.setCorrectAnswers(((Number) map.get("correctAnswers")).intValue());
        session.setTotalQuestions(((Number) map.get("totalQuestions")).intValue());
        session.setAccuracy(((Number) map.get("accuracy")).floatValue());
        session.setStartTime(((Number) map.get("startTime")).longValue());
        session.setEndTime(((Number) map.get("endTime")).longValue());
        session.setDurationSeconds(((Number) map.get("durationSeconds")).intValue());
        session.setDifficulty((String) map.get("difficulty"));
        session.setCompleted((Boolean) map.get("completed"));
        session.setCreatedAt(((Number) map.get("createdAt")).longValue());
        
        return session;
    }
    
    private SkillProgress convertMapToProgress(Map<String, Object> map) {
        SkillProgress progress = new SkillProgress();
        progress.setUserId((String) map.get("userId"));
        progress.setSkillType((String) map.get("skillType"));
        progress.setTotalSessions(((Number) map.get("totalSessions")).intValue());
        progress.setCompletedSessions(((Number) map.get("completedSessions")).intValue());
        progress.setAverageScore(((Number) map.get("averageScore")).floatValue());
        progress.setAverageAccuracy(((Number) map.get("averageAccuracy")).floatValue());
        progress.setTotalTimeSeconds(((Number) map.get("totalTimeSeconds")).longValue());
        progress.setHighestScore(((Number) map.get("highestScore")).floatValue());
        progress.setLowestScore(((Number) map.get("lowestScore")).floatValue());
        progress.setPracticesLast7Days(((Number) map.get("practicesLast7Days")).intValue());
        progress.setPracticesLast30Days(((Number) map.get("practicesLast30Days")).intValue());
        progress.setTrend((String) map.get("trend"));
        progress.setLevel(((Number) map.get("level")).intValue());
        progress.setProgressToNextLevel(((Number) map.get("progressToNextLevel")).floatValue());
        progress.setStrengthLevel((String) map.get("strengthLevel"));
        progress.setLastUpdated(((Number) map.get("lastUpdated")).longValue());
        
        return progress;
    }
    
    private StudyHabit convertMapToHabit(Map<String, Object> map) {
        StudyHabit habit = new StudyHabit();
        habit.setUserId((String) map.get("userId"));
        habit.setPreferredDayOfWeek(((Number) map.get("preferredDayOfWeek")).intValue());
        habit.setPreferredHourOfDay(((Number) map.get("preferredHourOfDay")).intValue());
        habit.setAverageSessionMinutes(((Number) map.get("averageSessionMinutes")).intValue());
        habit.setWeeklyFrequency(((Number) map.get("weeklyFrequency")).floatValue());
        habit.setMonthlyFrequency(((Number) map.get("monthlyFrequency")).floatValue());
        habit.setCurrentStreak(((Number) map.get("currentStreak")).intValue());
        habit.setLongestStreak(((Number) map.get("longestStreak")).intValue());
        habit.setTotalStudyDays(((Number) map.get("totalStudyDays")).intValue());
        habit.setMostPracticedSkill((String) map.get("mostPracticedSkill"));
        habit.setLeastPracticedSkill((String) map.get("leastPracticedSkill"));
        habit.setBestTimeOfDay((String) map.get("bestTimeOfDay"));
        habit.setLastStudyDate(((Number) map.get("lastStudyDate")).longValue());
        habit.setLastUpdated(((Number) map.get("lastUpdated")).longValue());
        
        return habit;
    }
    
    private StudySchedule convertMapToSchedule(Map<String, Object> map) {
        StudySchedule schedule = new StudySchedule();
        schedule.setUserId((String) map.get("userId"));
        schedule.setScheduledDate(((Number) map.get("scheduledDate")).longValue());
        schedule.setSkillType((String) map.get("skillType"));
        schedule.setRecommendedDurationMinutes(((Number) map.get("recommendedDurationMinutes")).intValue());
        schedule.setRecommendedDifficulty((String) map.get("recommendedDifficulty"));
        schedule.setRecommendedStartHour(((Number) map.get("recommendedStartHour")).intValue());
        schedule.setReason((String) map.get("reason"));
        schedule.setPriority(((Number) map.get("priority")).intValue());
        schedule.setStatus((String) map.get("status"));
        
        Object actualCompletedAt = map.get("actualCompletedAt");
        if (actualCompletedAt != null) {
            schedule.setActualCompletedAt(((Number) actualCompletedAt).longValue());
        }
        
        Object actualScore = map.get("actualScore");
        if (actualScore != null) {
            schedule.setActualScore(((Number) actualScore).floatValue());
        }
        
        Object actualDuration = map.get("actualDurationMinutes");
        if (actualDuration != null) {
            schedule.setActualDurationMinutes(((Number) actualDuration).intValue());
        }
        
        schedule.setCreatedAt(((Number) map.get("createdAt")).longValue());
        schedule.setLastUpdated(((Number) map.get("lastUpdated")).longValue());
        schedule.setNotificationSent((Boolean) map.get("notificationSent"));
        
        return schedule;
    }
}
