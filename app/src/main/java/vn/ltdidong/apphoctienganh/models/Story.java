package vn.ltdidong.apphoctienganh.models;

public class Story {
    private String id;
    private String title;
    private String description;
    private String category;
    private String level; // beginner, intermediate, advanced
    private String imageUrl;
    private int duration; // minutes to read
    private boolean isLocked;
    private int chaptersCount;
    
    public Story() {
    }
    
    public Story(String id, String title, String description, String category, 
                 String level, String imageUrl, int duration, boolean isLocked, int chaptersCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.level = level;
        this.imageUrl = imageUrl;
        this.duration = duration;
        this.isLocked = isLocked;
        this.chaptersCount = chaptersCount;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    
    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }
    
    public int getChaptersCount() { return chaptersCount; }
    public void setChaptersCount(int chaptersCount) { this.chaptersCount = chaptersCount; }
}
