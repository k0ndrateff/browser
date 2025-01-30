package rendering.layout;

import document.HtmlElement;
import document.HtmlNode;
import document.HtmlText;
import error.Logger;
import rendering.*;
import rendering.component.Emoji;
import rendering.component.Rectangle;
import rendering.component.RenderingComponent;
import rendering.component.Text;
import rendering.styles.CssColor;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.stream.Stream;

public class BlockLayout extends Layout {
    private static final String[] BLOCK_ELEMENTS = new String[] {
            "html", "body", "article", "section", "nav", "aside",
            "h1", "h2", "h3", "h4", "h5", "h6", "hgroup", "header",
            "footer", "address", "p", "hr", "pre", "blockquote",
            "ol", "ul", "menu", "li", "dl", "dt", "dd", "figure",
            "figcaption", "main", "div", "table", "form", "fieldset",
            "legend", "details", "summary"
    };

    public static final String SOFT_HYPHEN_STRING = Character.toString(0x00AD);
    public static final FontRenderContext FRC = new FontRenderContext(new AffineTransform(), true, false);
    private static final int DEFAULT_LINE_HEIGHT = 20;
    private static final String DEFAULT_FONT = "SF Pro";
    private static final String MONOSPACE_FONT = "Courier New";

    private final Deque<RenderingComponent> displayList = new ArrayDeque<>();
    private final Deque<Text> lineBuffer = new ArrayDeque<>();

    private int cursorX;
    private int cursorY;
    private int fontStyle = Font.PLAIN;
    private int fontSize = 16;
    private Color textColor = Color.BLACK;
    private boolean isLineCentered = false;
    private String fontName = DEFAULT_FONT;
    private TextRenderingProperty property = TextRenderingProperty.PLAIN;

    public BlockLayout(HtmlNode node, Layout parent, Layout previous, RenderingContext ctx) {
        super(node, parent, previous, ctx);
    }

    public DisplayMode getDisplayMode() {
        if (node instanceof HtmlText) {
            return DisplayMode.INLINE;
        }
        else if (node.getChildren().stream().anyMatch(child -> child instanceof HtmlElement && Arrays.asList(BLOCK_ELEMENTS).contains(child.toString()))) {
            return DisplayMode.BLOCK;
        }
        else if (!node.getChildren().isEmpty()) {
            return DisplayMode.INLINE;
        }
        else {
            return DisplayMode.BLOCK;
        }
    }

    @Override
    public void render() {
        this.x = this.parent.x;
        this.width = this.parent.width;

        if (this.previous != null) {
            this.y = this.previous.y + this.previous.height;
        }
        else {
            this.y = this.parent.y;
        }

        DisplayMode mode = getDisplayMode();

        if (mode == DisplayMode.BLOCK) {
            Logger.verbose("Rendering block...");

            Layout previous = null;

            for (HtmlNode child : node.getChildren()) {
                if (Objects.equals(child.toString(), "head")) continue;

                Layout next = new BlockLayout(child, this, previous, ctx);
                children.add(next);
                previous = next;
            }
        }
        else {
            Logger.verbose("Rendering inline text...");

            cursorX = this.x;
            cursorY = 0;

            this.traverseTree(node);

            this.flushLineBuffer();
        }

        for (Layout child : children) {
            child.render();
        }

        if (mode == DisplayMode.BLOCK) {
            this.height = (int) this.children.stream().mapToDouble(child -> child.height).sum();
        }
        else {
            this.height = this.cursorY;
        }

        postRenderingProcessing();

        Logger.verbose("Rendering " + this.node.toString() + " into " + this.width + " x " + this.height);
    }

    private void postRenderingProcessing() {
        if (this.node instanceof HtmlElement && Objects.equals(this.node.toString(), "li")) {
            displayList.add(new Rectangle(new Point(this.x + fontSize, this.y + 8), 3, 3, Color.BLACK));
        }

        if (this.node instanceof HtmlElement && ((HtmlElement) this.node).getStyle().containsKey("background-color")) {
            CssColor bgColor = new CssColor(((HtmlElement) this.node).getStyle().get("background-color").getValue().toUpperCase());

            if (bgColor.isDisplayable()) {
                displayList.add(new Rectangle(new Point(this.x, this.y), this.width, this.height, bgColor.getColor()));
            }
        }
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

        if (tag.getStyle().containsKey("font-weight") && tag.getStyle().get("font-weight") != null) {
            if (tag.getStyle().get("font-weight").getValue().equals("bold")) {
                fontStyle = Font.BOLD;
            }
            else {
                fontStyle = Font.PLAIN;
            }
        }

        if (tag.getStyle().containsKey("font-style") && tag.getStyle().get("font-style") != null) {
            if (tag.getStyle().get("font-style").getValue().equals("italic")) {
                fontStyle = Font.ITALIC;
            }
            else {
                fontStyle = Font.PLAIN;
            }
        }

        if (tag.getStyle().containsKey("font-size") && tag.getStyle().get("font-size") != null) {
            fontSize = Math.round(Float.parseFloat(tag.getStyle().get("font-size").getValue().replace("px", "")));
        }

        if (tag.getStyle().containsKey("color") && tag.getStyle().get("color") != null) {
            CssColor color = new CssColor(tag.getStyle().get("color").getValue().toUpperCase());

            if (color.isDisplayable()) {
                textColor = color.getColor();
            }
        }

        if (tag.getStyle().containsKey("text-align") && tag.getStyle().get("text-align") != null) {
            if (tag.getStyle().get("text-align").getValue().equals("center")) {
                isLineCentered = true;
            }
            else {
                flushLineBuffer();
            }
        }

        switch (tk) {
            case "sup" -> property = TextRenderingProperty.SUPERSCRIPT;
            case "abbr" -> fontName = DEFAULT_FONT + " SC";
            case "pre" -> {
                fontName = MONOSPACE_FONT;
                property = TextRenderingProperty.PREFORMATTED;
            }
            case "li" -> cursorX += 2 * fontSize;
            case null, default -> {
            }
        }
    }

    private void processHtmlCloseTag(HtmlElement tag) {
        String tk = tag.toString();


        switch (tk) {
            case "br", "p" -> flushLineBuffer();
            case "sup" -> {
                property = TextRenderingProperty.PLAIN;
            }
            case "abbr" -> fontName = DEFAULT_FONT;
            case "pre" -> {
                fontName = DEFAULT_FONT;
                property = TextRenderingProperty.PLAIN;
            }
            case "li" -> cursorX -= 2 * fontSize;
            case null, default -> {
            }
        }
    }

    private void processHtmlText(HtmlText text) {
        Font font = FontCache.retrieve(fontName, fontStyle, fontSize);
        int direction = ctx.isRtl() ? -1 : 1;

        for (String tk : splitText(text.toString())) {
            int wordWidth = (int) font.getStringBounds(tk, FRC).getWidth();
            int whitespaceWidth = (int) font.getStringBounds(" ", FRC).getWidth();

            if (Objects.equals(tk, "\n")) {
                this.flushLineBuffer();
            }
            else if (Emoji.isEmoji(tk)) {
                displayList.add(new Emoji(tk, new Point(cursorX, this.y), fontSize));

                cursorX += fontSize * direction;
            }
            else {
                lineBuffer.add(new Text(tk, new Point(cursorX, 0), font, property, textColor));

                cursorX += (wordWidth + whitespaceWidth) * direction;
            }

            if (cursorX + wordWidth >= this.width && !(property == TextRenderingProperty.PREFORMATTED)) {
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

            offsetX = (this.width - lineWidth) / 2;
        }

        Stream<LineMetrics> ascentMetrics = lineBuffer.stream().map(Text::getFontMetrics);
        float maxAscent = Collections.max(ascentMetrics.map(LineMetrics::getAscent).toList());
        float baseline = cursorY + 1.25f * maxAscent;

        for (Text word : lineBuffer) {
            if (word.getProperty() == TextRenderingProperty.SUPERSCRIPT) {
                int y = this.y + (int) ((int) baseline - 2 * maxAscent);
                word.setPositionY(y);
            }
            else {
                int y = this.y + (int) ((int) baseline - maxAscent);
                word.setPositionY(y);
            }

            word.incrementXPosition(offsetX);
            displayList.add(word);
        }

        Stream<LineMetrics> descentMetrics = lineBuffer.stream().map(Text::getFontMetrics);
        float maxDescent = Collections.max(descentMetrics.map(LineMetrics::getDescent).toList());
        cursorY = (int) ((int) baseline + 1.25f * maxDescent);

        cursorX = this.x;
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
}
