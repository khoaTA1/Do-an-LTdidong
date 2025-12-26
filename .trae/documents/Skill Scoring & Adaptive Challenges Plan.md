# Implementation Plan: Skill Scoring & Adaptive Challenges

## Overview
This plan implements a skill scoring system stored in Firebase Firestore. It focuses on the **Writing** skill first, as requested. The system includes:
1.  **Scoring Logic:** Adjusts scores (+/-) based on performance (0-10 scale).
2.  **Time Decay:** Reduces scores if inactive.
3.  **Adaptive Challenges:** Customizes daily challenges based on skill strengths/weaknesses.

## Phase 1: Data Model Updates (User.java)
We need to update the `User` model to store skill scores and practice timestamps.

*   **Add Fields:**
    *   `Map<String, Double> skillScores`: Stores "writing", "reading", "listening", "speaking". Default score: 5.0.
    *   `Map<String, Long> lastPracticeTime`: Stores timestamps for decay calculation.
*   **Getters/Setters:** Standard accessors for these new fields.

## Phase 2: Logic Centralization (SkillManager)
Create a helper class `vn.ltdidong.apphoctienganh.managers.SkillManager` to handle the math. This ensures all team members use the same logic.

*   **`calculateNewScore(currentScore, lessonScore)`**:
    *   Lesson Score (scaled to 0-10):
        *   < 3: -0.2
        *   3 - 4: -0.1
        *   4 - 5: -0.05
        *   5 - 6: +0.1
        *   6 - 7: +0.2
        *   7 - 8: +0.3
        *   > 8: +0.4
    *   Clamps result between 0.0 and 10.0.
*   **`applyTimeDecay(currentScore, lastPracticeTime)`**:
    *   If `(now - lastPracticeTime) > 3 days`: Reduce score by `0.1 * (days_inactive - 3)`.
*   **`getStrongestSkill(user)` / `getWeakestSkill(user)`**: Helper methods to identify skills.

## Phase 3: Writing Integration (WritingActivity)
Update `WritingActivity.java` to use `SkillManager`.

*   **Hook Point:** Inside `updateUserXPForWriting` (or a new dedicated method `updateWritingScore`).
*   **Logic:**
    1.  Convert essay score (0-10 from AI) to the new score using `SkillManager`.
    2.  Update `writingScore` in Firestore.
    3.  Update `lastPracticeTime` for "writing".
    4.  Display the new skill score to the user (e.g., "Writing Skill: 5.2 (+0.2)").

## Phase 4: Adaptive Challenge Logic (DailyChallengeActivity)
Update `DailyChallengeActivity` to customize challenges.

*   **Logic Change:** Instead of hardcoded challenges, generate them dynamically.
*   **Implementation:**
    1.  Fetch `User` data (with scores) from Firestore.
    2.  Identify **Weakest Skill**: Add 2 "Basic" tasks for this skill.
    3.  Identify **Strongest Skill**: Add 1 "Advanced" task for this skill.
    4.  Fill the rest with random/standard tasks.

## Step-by-Step Execution Plan for Writing Team
1.  **Modify `User.java`**: Add `skillScores` and `lastPracticeTime`.
2.  **Create `SkillManager.java`**: Implement the scoring matrix and decay logic.
3.  **Update `WritingActivity.java`**:
    *   Receive the 0-10 score from the AI response.
    *   Call `SkillManager` to update the Firestore user document.
4.  **Verify**: Run the app, submit an essay, and check Firestore for the new `skillScores` field.

*Note: This plan focuses on setting up the architecture and implementing the Writing portion immediately.*