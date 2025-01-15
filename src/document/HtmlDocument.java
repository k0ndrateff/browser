package document;

public class HtmlDocument {
    private final String content;

    public HtmlDocument(String content) {
        this.content = content;
    }

    public String getContent() {
        boolean inTag = false;

        StringBuilder contentBuilder = new StringBuilder();

        for (char c : content.toCharArray()) {
            if (c == '<') {
                inTag = true;
            } else if (c == '>') {
                inTag = false;
            } else if (!inTag) {
                contentBuilder.append(c);
            }
        }

        return contentBuilder.toString();
    }
}
