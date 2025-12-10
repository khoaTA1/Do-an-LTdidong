package vn.ltdidong.apphoctienganh.models;

public class Achievement {
    private String icon;
    private String title;
    private String description;
    private boolean unlocked;
    private int requirement;

    public Achievement() {
    }

    public Achievement(String icon, String title, String description, boolean unlocked, int requirement) {
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.unlocked = unlocked;
        this.requirement = requirement;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public int getRequirement() {
        return requirement;
    }

    public void setRequirement(int requirement) {
        this.requirement = requirement;
    }
}
