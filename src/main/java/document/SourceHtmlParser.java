package document;

import error.Logger;

public class SourceHtmlParser extends HtmlParser {
    public SourceHtmlParser(HtmlDocument document) {
        super(document);
    }

    @Override
    public HtmlNode parse() {
        Logger.verbose("Parsing source HTML Tree...");

        boolean inTag = false;
        boolean inQuotedAttribute = false;

        StringBuilder buffer = new StringBuilder();

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
                    this.addElementNode("/b");
                    this.addTextNode(buffer.toString());
                }

                buffer.setLength(0);
            } else if (c == '>') {
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
                    this.addTextNode("<" + buffer + ">");
                    this.addElementNode("b");
                }
                else {
                    this.addElementNode("i");
                    this.addTextNode("<" + buffer + ">");
                    this.addElementNode("/i");
                    this.addElementNode("b");
                }


                inTag = false;
                buffer.setLength(0);
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
