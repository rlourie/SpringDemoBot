package local.springdemobot.enums;

public enum TypeUserCommands {
    UPLOAD("/upload"),
    VIEW("/view"),
    DELETE("/delete"),
    START("/start"),
    AUTH("auth"),
    CREATE("/create"),
    OTHER("");
    private final String title;

    TypeUserCommands(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
