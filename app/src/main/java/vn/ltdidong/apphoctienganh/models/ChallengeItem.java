package vn.ltdidong.apphoctienganh.models;

/**
 * Model đại diện cho một challenge item trong danh sách
 */
public class ChallengeItem {
    private String title;
    private String description;
    private int xpReward;
    private String type;
    private boolean completed;

    public ChallengeItem(String title, String description, int xpReward, String type, boolean completed) {
        this.title = title;
        this.description = description;
        this.xpReward = xpReward;
        this.type = type;
        this.completed = completed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
