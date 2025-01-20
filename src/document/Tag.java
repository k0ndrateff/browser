package document;

public class Tag extends Entity {
    private final String name;

    public Tag(String name) {
        this.name = name;
    }

    public String getContent() {
        return name;
    }
}
