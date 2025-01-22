package rendering;

import document.HtmlLayoutEntity;
import document.HtmlTag;
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
    public static final FontRenderContext FRC = new FontRenderContext(new AffineTransform(), true, false);
    public static final int DEFAULT_LINE_HEIGHT = 20;

    private final boolean isRtl;
    private final RenderingContext ctx;

    private final Deque<RenderingComponent> displayList = new ArrayDeque<>();
    private final Deque<Text> lineBuffer = new ArrayDeque<>();

    private int cursorX;
    private int cursorY;
    private int fontStyle = Font.PLAIN;
    private int fontSize = 16;
    private boolean isLineCentered = false;

    public TextRenderer(RenderingContext ctx, boolean isRtl) {
        this.ctx = ctx;
        this.isRtl = isRtl;
    }

    public void render(ArrayList<HtmlLayoutEntity> tokens) {
        Logger.verbose("Rendering text...");

        cursorX = ctx.getBaseTextPosition().x;
        cursorY = ctx.getBaseTextPosition().y;

        for (HtmlLayoutEntity token : tokens) {
            if (token instanceof HtmlTag) {
                processHtmlTag((HtmlTag) token);
            }
            else if (token instanceof HtmlText) {
                processHtmlText((HtmlText) token);
            }
        }

        this.flushLineBuffer();
    }

    private void processHtmlTag(HtmlTag tag) {
        String tk = tag.toString();


        switch (tk) {
            case "i" -> fontStyle = Font.ITALIC;
            case "b" -> fontStyle = Font.BOLD;
            case "/i", "/b" -> fontStyle = Font.PLAIN;
            case "small" -> fontSize -= 2;
            case "/small" -> fontSize += 2;
            case "big" -> fontSize += 4;
            case "/big" -> fontSize -= 4;
            case "br /", "/p", "/center", "/h1" -> flushLineBuffer();
            case "center", "h1 class=\"title\"" -> isLineCentered = true;
            case null, default -> {
            }
        }
    }

    private void processHtmlText(HtmlText text) {
        Font font = FontCache.retrieve("SF Pro", fontStyle, fontSize);
        int direction = isRtl ? -1 : 1;

        for (String tk : splitText(text.toString())) {
            int wordWidth = (int) font.getStringBounds(tk, FRC).getWidth();
            int whitespaceWidth = (int) font.getStringBounds(" ", FRC).getWidth();

            if (Objects.equals(tk, "\n")) {
                this.flushLineBuffer();
            }
            else if (Emoji.isEmoji(tk)) {
                displayList.add(new Emoji(tk, new Point(cursorX, cursorY), fontSize));

                cursorX += 16 * direction;
            }
            else {
                lineBuffer.add(new Text(tk, new Point(cursorX, 0), font));

                cursorX += (wordWidth + whitespaceWidth) * direction;
            }

            if (cursorX + wordWidth >= ctx.getWidth()) {
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
            int y = (int) ((int) baseline - maxAscent);
            word.setPositionY(y);
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

    private static boolean containsEmojiOrNewline(String word) {
        return Arrays.stream(splitWord(word)).anyMatch(ch -> Emoji.isEmoji(ch) || Objects.equals(ch, "\n"));
    }

    public static ArrayList<String> splitText(String text) {
        ArrayList<String> result = new ArrayList<>();
        Matcher matcher = Pattern.compile("[\\p{L}\\p{N}\\p{P}]+|\\p{So}+|\\n").matcher(text);

        while (matcher.find()) {
            String word = matcher.group();
            if (containsEmojiOrNewline(word)) {
                result.addAll(Arrays.asList(splitWord(word)));
            } else {
                result.add(word);
            }
        }

        return result;
    }

    private static String[] splitWord(String text) {
        return text.codePoints()
                .mapToObj(java.lang.Character::toString)
                .toArray(String[]::new);
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
