package vn.ltdidong.apphoctienganh.managers;

import java.util.Map;
import vn.ltdidong.apphoctienganh.models.User;

public class SkillManager {

    private static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L;
    private static final int INACTIVITY_THRESHOLD_DAYS = 3;
    private static final double DECAY_RATE_PER_DAY = 0.05;

    public static final String SKILL_WRITING = "writing";
    public static final String SKILL_READING = "reading";
    public static final String SKILL_LISTENING = "listening";
    public static final String SKILL_SPEAKING = "speaking";

    /**
     * Calculates the new skill score based on the current score and the lesson result.
     *
     * @param currentScore The user's current score (0.0 - 10.0)
     * @param lessonScore  The score obtained in the lesson (0.0 - 10.0)
     * @return The new updated score
     */
    public static double calculateNewScore(double currentScore, double lessonScore) {
        double adjustment = 0.0;

        if (lessonScore < 3.0) {
            adjustment = -0.2;
        } else if (lessonScore < 4.0) {
            adjustment = -0.15;
        } else if (lessonScore < 5.0) {
            adjustment = -0.1;
        } else if (lessonScore < 6.0) {
            adjustment = 0.1;
        } else if (lessonScore < 7.0) {
            adjustment = 0.2;
        } else if (lessonScore < 8.0) {
            adjustment = 0.3;
        } else if (lessonScore < 9.0) {
            adjustment = 0.4;
        } else {
            adjustment = 0.5;
        }

        double newScore = currentScore + adjustment;

        // Clamp score between 0.0 and 10.0
        return Math.max(0.0, Math.min(10.0, newScore));
    }

    /**
     * Applies time decay to the skill score if the user has been inactive for too long.
     *
     * @param currentScore     The user's current score
     * @param lastPracticeTime Timestamp of the last practice
     * @return The score after applying decay (if any)
     */
    public static double applyTimeDecay(double currentScore, long lastPracticeTime) {
        if (lastPracticeTime == 0) return currentScore;

        long diffMillis = System.currentTimeMillis() - lastPracticeTime;
        long daysInactive = diffMillis / ONE_DAY_MILLIS;

        if (daysInactive > INACTIVITY_THRESHOLD_DAYS) {
            long daysToDecay = daysInactive - INACTIVITY_THRESHOLD_DAYS;
            double decayAmount = daysToDecay * DECAY_RATE_PER_DAY;
            double newScore = currentScore - decayAmount;
            return Math.max(0.0, newScore);
        }

        return currentScore;
    }

    /**
     * Helper to get the strongest skill (highest score)
     */
    public static String getStrongestSkill(User user) {
        Map<String, Double> scores = user.getSkillScores();
        String strongest = SKILL_WRITING; // Default
        double maxScore = -1.0;

        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                strongest = entry.getKey();
            }
        }
        return strongest;
    }

    /**
     * Helper to get the weakest skill (lowest score)
     */
    public static String getWeakestSkill(User user) {
        Map<String, Double> scores = user.getSkillScores();
        String weakest = SKILL_WRITING; // Default
        double minScore = 11.0;

        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            if (entry.getValue() < minScore) {
                minScore = entry.getValue();
                weakest = entry.getKey();
            }
        }
        return weakest;
    }
}
