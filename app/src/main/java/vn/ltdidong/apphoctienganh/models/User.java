package vn.ltdidong.apphoctienganh.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private long id;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("phone")
    private String phone;

    @SerializedName("address")
    private String address;

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("created_at")
    private long createdAt;

    @SerializedName("role")
    private String role; // "user" hoặc "admin"

    @SerializedName("is_active")
    private Boolean isActive = true; // Trạng thái tài khoản: true = đang hoạt động, false = đã khóa

    @SerializedName("total_xp")
    private int totalXP = 0; // Tổng kinh nghiệm của user

    @SerializedName("current_level")
    private int currentLevel = 1; // Level hiện tại

    @SerializedName("xp_to_next_level")
    private int xpToNextLevel = 100; // XP cần để lên level tiếp theo

    @SerializedName("current_level_xp")
    private int currentLevelXP = 0; // XP hiện tại trong level này

    @SerializedName("skill_scores")
    private java.util.Map<String, Double> skillScores; // Điểm đánh giá kỹ năng: writing, reading, listening, speaking

    @SerializedName("last_practice_time")
    private java.util.Map<String, Long> lastPracticeTime; // Thời gian luyện tập gần nhất của từng kỹ năng

    // Constructors
    public User() {
        this.role = "user"; // Mặc định là user
        this.skillScores = new java.util.HashMap<>();
        this.lastPracticeTime = new java.util.HashMap<>();

        // Initialize default scores
        this.skillScores.put("writing", 5.0);
        this.skillScores.put("reading", 5.0);
        this.skillScores.put("listening", 5.0);
        this.skillScores.put("speaking", 5.0);
    }

    public User(String email, String password, String fullName, String phone) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public int getTotalXP() {
        return totalXP;
    }

    public void setTotalXP(int totalXP) {
        this.totalXP = totalXP;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public int getXpToNextLevel() {
        return xpToNextLevel;
    }

    public void setXpToNextLevel(int xpToNextLevel) {
        this.xpToNextLevel = xpToNextLevel;
    }

    public int getCurrentLevelXP() {
        return currentLevelXP;
    }

    public void setCurrentLevelXP(int currentLevelXP) {
        this.currentLevelXP = currentLevelXP;
    }

    public java.util.Map<String, Double> getSkillScores() {
        if (skillScores == null) {
            skillScores = new java.util.HashMap<>();
            skillScores.put("writing", 5.0);
            skillScores.put("reading", 5.0);
            skillScores.put("listening", 5.0);
            skillScores.put("speaking", 5.0);
        }
        return skillScores;
    }

    public void setSkillScores(java.util.Map<String, Double> skillScores) {
        this.skillScores = skillScores;
    }

    public java.util.Map<String, Long> getLastPracticeTime() {
        if (lastPracticeTime == null) {
            lastPracticeTime = new java.util.HashMap<>();
        }
        return lastPracticeTime;
    }

    public void setLastPracticeTime(java.util.Map<String, Long> lastPracticeTime) {
        this.lastPracticeTime = lastPracticeTime;
    }

    /**
     * Thêm XP vào tài khoản user
     * 
     * @param xp Số XP cần thêm
     * @return true nếu level up, false nếu không
     */
    public boolean addXP(int xp) {
        this.totalXP += xp;
        this.currentLevelXP += xp;

        // Kiểm tra level up
        boolean leveledUp = false;
        while (this.currentLevelXP >= this.xpToNextLevel) {
            leveledUp = true;
            this.currentLevelXP -= this.xpToNextLevel;
            this.currentLevel++;

            // Tính XP cần cho level tiếp theo (tăng 50 XP mỗi level)
            // Level 1->2: 100 XP
            // Level 2->3: 150 XP
            // Level 3->4: 200 XP
            this.xpToNextLevel = 100 + (this.currentLevel - 1) * 50;
        }

        return leveledUp;
    }

    /**
     * Tính phần trăm progress trong level hiện tại
     * 
     * @return Giá trị từ 0-100
     */
    public int getLevelProgress() {
        if (xpToNextLevel <= 0)
            return 0;
        return (int) ((currentLevelXP * 100.0f) / xpToNextLevel);
    }
}
