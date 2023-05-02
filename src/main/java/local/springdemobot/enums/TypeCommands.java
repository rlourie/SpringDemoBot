package local.springdemobot.enums;

public enum TypeCommands {
    UPLOAD("/upload"),
    VIEW("/view"),
    DELETE("/delete"),
    START("/start"),
    AUTH("auth"),
    OTHER("");
    private final String title;

    TypeCommands(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
