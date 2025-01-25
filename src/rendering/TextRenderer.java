package rendering;

import document.HtmlNode;
import document.HtmlElement;
import document.HtmlText;
import error.Logger;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TextRenderer {
    public static final String SOFT_HYPHEN_STRING = Character.toString(0x00AD);
    public static final FontRenderContext FRC = new FontRenderContext(new AffineTransform(), true, false);
    private static final int DEFAULT_LINE_HEIGHT = 20;
    private static final String DEFAULT_FONT = "SF Pro";
    private static final String MONOSPACE_FONT = "Courier New";

    private final boolean isRtl;
    private final RenderingContext ctx;

    private final Deque<RenderingComponent> displayList = new ArrayDeque<>();
    private final Deque<Text> lineBuffer = new ArrayDeque<>();

    private int cursorX;
    private int cursorY;
    private int fontStyle = Font.PLAIN;
    private int fontSize = 16;
    private boolean isLineCentered = false;
    private String fontName = DEFAULT_FONT;
    private TextRenderingProperty property = TextRenderingProperty.PLAIN;

    public TextRenderer(RenderingContext ctx, boolean isRtl) {
        this.ctx = ctx;
        this.isRtl = isRtl;
    }

    public void render(HtmlNode node) {
        Logger.verbose("Rendering text...");

        cursorX = ctx.getBaseTextPosition().x;
        cursorY = ctx.getBaseTextPosition().y;

        this.traverseTree(node);

        this.flushLineBuffer();
    }

    private void traverseTree(HtmlNode node) {
        if (node instanceof HtmlElement) {
            this.processHtmlOpenTag((HtmlElement) node);

            for (HtmlNode child : node.getChildren()) {
                this.traverseTree(child);
            }

            this.processHtmlCloseTag((HtmlElement) node);
        }
        else if (node instanceof HtmlText) {
            processHtmlText((HtmlText) node);
        }
    }

    private void processHtmlOpenTag(HtmlElement tag) {
        String tk = tag.toString();


        switch (tk) {
            case "i" -> fontStyle = Font.ITALIC;
            case "b" -> fontStyle = Font.BOLD;
            case "small" -> fontSize -= 2;
            case "big" -> fontSize += 4;
            case "center", "h1" -> isLineCentered = true;
            case "sup" -> {
                property = TextRenderingProperty.SUPERSCRIPT;
                fontSize /= 2;
            }
            case "abbr" -> fontName = DEFAULT_FONT + " SC";
            case "pre" -> {
                fontName = MONOSPACE_FONT;
                property = TextRenderingProperty.PREFORMATTED;
            }
            case null, default -> {
            }
        }
    }

    private void processHtmlCloseTag(HtmlElement tag) {
        String tk = tag.toString();


        switch (tk) {
            case "i", "b" -> fontStyle = Font.PLAIN;
            case "small" -> fontSize += 2;
            case "big" -> fontSize -= 4;
            case "br", "p", "center", "h1" -> flushLineBuffer();
            case "sup" -> {
                property = TextRenderingProperty.PLAIN;
                fontSize *= 2;
            }
            case "abbr" -> fontName = DEFAULT_FONT;
            case "pre" -> {
                fontName = DEFAULT_FONT;
                property = TextRenderingProperty.PLAIN;
            }
            case null, default -> {
            }
        }
    }

    private void processHtmlText(HtmlText text) {
        Font font = FontCache.retrieve(fontName, fontStyle, fontSize);
        int direction = isRtl ? -1 : 1;

        for (String tk : splitText(text.toString())) {
            int wordWidth = (int) font.getStringBounds(tk, FRC).getWidth();
            int whitespaceWidth = (int) font.getStringBounds(" ", FRC).getWidth();

            if (Objects.equals(tk, "\n")) {
                this.flushLineBuffer();
            }
            else if (Emoji.isEmoji(tk)) {
                displayList.add(new Emoji(tk, new Point(cursorX, cursorY), fontSize));

                cursorX += fontSize * direction;
            }
            else {
                lineBuffer.add(new Text(tk, new Point(cursorX, 0), font, property));

                cursorX += (wordWidth + whitespaceWidth) * direction;
            }

            if (cursorX + wordWidth >= ctx.getWidth() && !(property == TextRenderingProperty.PREFORMATTED)) {
                this.flushLineBuffer();
            }
        }
    }

    private void flushLineBuffer() {
        if (lineBuffer.isEmpty()) {
            cursorY += DEFAULT_LINE_HEIGHT;

            return;
        }

        int offsetX = 0;

        if (isLineCentered) {
            Logger.verbose("Encountered line centered text");

            int lineWidth = measureLineWidth(lineBuffer);

            offsetX = (ctx.getWidth() - lineWidth) / 2;
        }

        Stream<LineMetrics> ascentMetrics = lineBuffer.stream().map(Text::getFontMetrics);
        float maxAscent = Collections.max(ascentMetrics.map(LineMetrics::getAscent).toList());
        float baseline = cursorY + 1.25f * maxAscent;

        for (Text word : lineBuffer) {
            if (word.getProperty() == TextRenderingProperty.SUPERSCRIPT) {
                int y = (int) ((int) baseline - 2 * maxAscent);
                word.setPositionY(y);
            }
            else {
                int y = (int) ((int) baseline - maxAscent);
                word.setPositionY(y);
            }

            word.incrementXPosition(offsetX);
            displayList.add(word);
        }

        Stream<LineMetrics> descentMetrics = lineBuffer.stream().map(Text::getFontMetrics);
        float maxDescent = Collections.max(descentMetrics.map(LineMetrics::getDescent).toList());
        cursorY = (int) ((int) baseline + 1.25f * maxDescent);

        cursorX = ctx.getBaseTextPosition().x;
        isLineCentered = false;
        lineBuffer.clear();
    }

    private static int measureLineWidth(Deque<Text> line) {
        int width = 0;

        for (Text word : line) {
            int wordWidth = (int) word.getFont().getStringBounds(word.toString(), FRC).getWidth();
            int whitespaceWidth = (int) word.getFont().getStringBounds(" ", FRC).getWidth();

            width += wordWidth + whitespaceWidth;
        }

        return width;
    }

    public static ArrayList<String> splitText(String text) {
        ArrayList<String> result = new ArrayList<>();
        String[] words = text.split(" ");

        for (String word : words) {
            result.addAll(splitWord(word));
        }

        return result;
    }

    private static ArrayList<String> splitWord(String text) {
        ArrayList<String> result = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        String[] characters = text.codePoints()
                .mapToObj(java.lang.Character::toString)
                .toArray(String[]::new);

        for (String character : characters) {
            if (Objects.equals(character, "\n") || Emoji.isEmoji(character)) {
                if (!builder.isEmpty()) {
                    result.add(builder.toString());
                    builder.setLength(0);
                }

                result.add(character);
            }
            else {
                builder.append(character);
            }
        }

        result.add(builder.toString());

        return result;
    }

    public Deque<RenderingComponent> getDisplayList() {
        return displayList;
    }

    public int getLastDisplayListEntryY() {
        if (displayList.isEmpty()) {
            return 0;
        }

        return displayList.getLast().getPosition().y;
    }
}
