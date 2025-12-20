package vn.ltdidong.apphoctienganh.models;

/**
 * Enum định nghĩa các kỹ năng học tiếng Anh
 */
public enum SkillType {
    LISTENING("Nghe"),
    SPEAKING("Nói"),
    READING("Đọc"),
    WRITING("Viết"),
    VOCABULARY("Từ vựng"),
    GRAMMAR("Ngữ pháp");

    private final String displayName;

    SkillType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SkillType fromString(String text) {
        for (SkillType skill : SkillType.values()) {
            if (skill.name().equalsIgnoreCase(text)) {
                return skill;
            }
        }
        return null;
    }
}
