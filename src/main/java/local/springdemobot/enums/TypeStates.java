package local.springdemobot.enums;

public enum TypeStates {
    DONE("done"),
    PROCESSING("processing");
    private final String title;

    TypeStates(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
