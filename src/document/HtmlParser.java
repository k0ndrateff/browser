package document;

import error.Logger;
import rendering.TextRenderer;

import java.util.Arrays;
import java.util.Stack;

public class HtmlParser {
    private static final String[] SELF_CLOSING_TAGS = new String[] { "area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "param", "source", "track", "wbr" };

    private final HtmlDocument document;
    private final Stack<HtmlNode> unfinishedNodes;

    public HtmlParser(HtmlDocument document) {
        this.document = document;
        this.unfinishedNodes = new Stack<>();
    }

    public HtmlDocument getDocument() {
        return document;
    }

    public HtmlNode parse() {
        Logger.verbose("Parsing HTML Tree...");

        if (document.isViewSource()) {
            return new HtmlText(document.getContent(), null);
        }
        else {
            boolean inTag = false;
            boolean inEntity = false;

            StringBuilder buffer = new StringBuilder();
            StringBuilder htmlEntityBuilder = new StringBuilder();

            for (char c : document.getContent().toCharArray()) {
                if (c == '<') {
                    inTag = true;

                    if (!buffer.isEmpty()) {
                        this.addTextNode(buffer.toString());
                    }

                    buffer.setLength(0);
                } else if (c == '>') {
                    inTag = false;

                    this.addElementNode(buffer.toString());

                    buffer.setLength(0);
                } else if (!inTag) {
                    if (c == '&') {
                        inEntity = true;
                    }
                    else if (c == ';' && inEntity) {
                        inEntity = false;
                        buffer.append(resolveEntity(htmlEntityBuilder.toString()));
                        htmlEntityBuilder.setLength(0);
                    }
                    else if (inEntity) {
                        htmlEntityBuilder.append(c);
                    }
                    else {
                        buffer.append(c);
                    }
                }
                else {
                    buffer.append(c);
                }
            }

            if (!buffer.isEmpty() && !inTag) {
                this.addTextNode(buffer.toString());
            }

            return this.finishTree();
        }
    }

    private void addTextNode(String text) {
        if (text.trim().isEmpty()) return;

        HtmlNode parent = this.unfinishedNodes.peek();
        HtmlNode textNode = new HtmlText(text, parent);
        parent.appendChildren(textNode);
    }

    private void addElementNode(String tag) {
        if (tag.startsWith("!")) return;

        if (tag.startsWith("/")) {
            if (this.unfinishedNodes.size() == 1) return;

            HtmlNode elementNode = this.unfinishedNodes.pop();
            HtmlNode parent = this.unfinishedNodes.peek();
            parent.appendChildren(elementNode);
        }
        else if (Arrays.asList(SELF_CLOSING_TAGS).contains(tag)) {
            HtmlNode parent = this.unfinishedNodes.peek();
            HtmlNode elementNode = HtmlElement.create(tag, parent);
            parent.appendChildren(elementNode);
        }
        else {
            HtmlNode parent;

            if (this.unfinishedNodes.isEmpty()) {
                parent = null;
            }
            else {
                parent = this.unfinishedNodes.peek();
            }

            HtmlNode elementNode = HtmlElement.create(tag, parent);
            this.unfinishedNodes.push(elementNode);
        }
    }

    private HtmlNode finishTree() {
        while (this.unfinishedNodes.size() > 1) {
            HtmlNode node = this.unfinishedNodes.pop();
            HtmlNode parent = this.unfinishedNodes.peek();
            parent.appendChildren(node);
        }

        return this.unfinishedNodes.pop();
    }

    private String resolveEntity(String entity) {
        Logger.verbose("Resolving HTML entity: " + entity);

        return switch (entity) {
            case "lt" -> "<";
            case "gt" -> ">";
            case "amp" -> " &";
            case "quot" -> "\"";
            case "apos" -> "'";
            case "nbsp" -> " ";
            case "shy" -> TextRenderer.SOFT_HYPHEN_STRING;
            default -> "";
        };
    }
}
