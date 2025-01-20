package document;

public class Text extends Entity {
    private final String text;

    public Text(String text) {
        this.text = text;
    }

    public String getContent() {
        return text;
    }
}
