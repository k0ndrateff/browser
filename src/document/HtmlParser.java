package document;

import error.Logger;
import rendering.layout.BlockLayout;
import utility.TreeUtil;

import java.util.*;

public class HtmlParser {
    protected static final String[] SELF_CLOSING_TAGS = new String[] { "area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "param", "source", "track", "wbr" };
    protected static final String[] HEAD_TAGS = new String[] { "base", "basefont", "bgsound", "noscript", "link", "meta", "title", "style", "script" };

    protected final HtmlDocument document;
    protected final Stack<HtmlNode> unfinishedNodes;

    public HtmlParser(HtmlDocument document) {
        this.document = document;
        this.unfinishedNodes = new Stack<>();
    }

    public static HtmlParser create(HtmlDocument document) {
        if (document.isViewSource()) {
            return new SourceHtmlParser(document);
        }
        else {
            return new HtmlParser(document);
        }
    }

    public HtmlDocument getDocument() {
        return document;
    }

    public static ArrayList<String> getStylesheetLinkUrls(HtmlNode node) {
        ArrayList<String> urls = new ArrayList<>();
        ArrayList<HtmlNode> nodeList = new TreeUtil<HtmlNode>().toList(node, new ArrayList<>());

        for (HtmlNode child : nodeList) {
            if (child instanceof HtmlElement && Objects.equals(child.toString(), "link") && Objects.equals(((HtmlElement) child).getAttributes().get("rel"), "stylesheet") && ((HtmlElement) child).getAttributes().containsKey("href")) {
                urls.add(((HtmlElement) child).getAttributes().get("href"));
            }
        }

        Logger.verbose("Found " + urls.size() + " external stylesheet links");

        return urls;
    }

    public HtmlNode parse() {
        Logger.verbose("Parsing HTML Tree...");

        boolean inTag = false;
        boolean inQuotedAttribute = false;
        boolean inEntity = false;

        StringBuilder buffer = new StringBuilder();
        StringBuilder htmlEntityBuilder = new StringBuilder();

        for (char c : document.getContent().toCharArray()) {
            if (c == '"' && inTag) {
                inQuotedAttribute = !inQuotedAttribute;
            }

            if (c == '<') {
                if (inTag) {
                    buffer.append(c);

                    continue;
                }

                inTag = true;

                if (!buffer.isEmpty()) {
                    this.addTextNode(buffer.toString());
                }

                buffer.setLength(0);
            }
            else if (c == '>') {
                if (inQuotedAttribute) {
                    buffer.append(c);

                    continue;
                }
                if (buffer.toString().startsWith("!--") && !buffer.toString().endsWith("--")) {
                    buffer.append(c);

                    continue;
                }
                else if (buffer.toString().startsWith("script") && !buffer.toString().endsWith("/") && !buffer.toString().endsWith("/script")) {
                    buffer.append(c);

                    continue;
                }
                else if (!buffer.toString().startsWith("!--")) {
                    this.addElementNode(buffer.toString());
                }


                inTag = false;
                buffer.setLength(0);
            }
            else if (!inTag) {
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

    protected void addTextNode(String text) {
        if (text.trim().isEmpty()) return;

        this.insertImplicitTags(null);

        HtmlNode parent = this.unfinishedNodes.peek();
        HtmlNode textNode = new HtmlText(text, parent);
        parent.appendChildren(textNode);
    }

    protected void addElementNode(String tag) {
        if (tag.startsWith("!")) return;

        String tagName = HtmlElement.getTagName(tag);

        this.insertImplicitTags(tagName);

        if (tagName.startsWith("/")) {
            if (this.unfinishedNodes.size() == 1) return;

            HtmlNode elementNode = this.unfinishedNodes.pop();
            HtmlNode parent = this.unfinishedNodes.peek();
            parent.appendChildren(elementNode);
        }
        else if (Arrays.asList(SELF_CLOSING_TAGS).contains(tagName)) {
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

    protected HtmlNode finishTree() {
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
            case "shy" -> BlockLayout.SOFT_HYPHEN_STRING;
            default -> "";
        };
    }
}
