package document;

import error.Logger;
import rendering.TextRenderer;

import java.util.*;

public class HtmlParser {
    private static final String[] SELF_CLOSING_TAGS = new String[] { "area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "param", "source", "track", "wbr" };
    private static final String[] HEAD_TAGS = new String[] { "base", "basefont", "bgsound", "noscript", "link", "meta", "title", "style", "script" };

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
                    if (buffer.toString().startsWith("!--") && !buffer.toString().endsWith("--")) {
                        buffer.append(c);

                        continue;
                    }
                    else if (!buffer.toString().startsWith("!--")) {
                        this.addElementNode(buffer.toString());
                    }


                    inTag = false;
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

        this.insertImplicitTags(null);

        HtmlNode parent = this.unfinishedNodes.peek();
        HtmlNode textNode = new HtmlText(text, parent);
        parent.appendChildren(textNode);
    }

    private void addElementNode(String tag) {
        if (tag.startsWith("!")) return;

        this.insertImplicitTags(tag);

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
        if (this.unfinishedNodes.isEmpty()) {
            this.insertImplicitTags(null);
        }

        while (this.unfinishedNodes.size() > 1) {
            HtmlNode node = this.unfinishedNodes.pop();
            HtmlNode parent = this.unfinishedNodes.peek();
            parent.appendChildren(node);
        }

        return this.unfinishedNodes.pop();
    }

    private void insertImplicitTags(String tag) {
        final String[] headBodyHtmlTags = new String[] { "head", "body", "/html" };
        final ArrayList<String> headTags = new ArrayList<>(List.of(HEAD_TAGS));
        headTags.add("/head");

        while (true) {
            String[] openTags = this.unfinishedNodes.stream().map(HtmlNode::toString).toArray(String[]::new);

            if (openTags.length == 0 && !Objects.equals(tag, "html")) {
                this.addElementNode("html");
            }
            else if (openTags.length == 1 && openTags[0].equals("html") && !Arrays.asList(headBodyHtmlTags).contains(tag)) {
                if (Arrays.asList(HEAD_TAGS).contains(tag)) {
                    this.addElementNode("head");
                }
                else {
                    this.addElementNode("body");
                }
            }
            else if (openTags.length == 2 && openTags[0].equals("html") && openTags[1].equals("head") && !headTags.contains(tag)) {
                this.addElementNode("/head");
            }
            else if (openTags.length > 0 && openTags[openTags.length - 1].equals("p") && Objects.equals(tag, "p")) {
                this.addElementNode("/p");
            }
            else {
                break;
            }
        }
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
