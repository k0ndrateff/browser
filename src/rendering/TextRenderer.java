package rendering;

import document.HtmlLayoutEntity;
import document.HtmlTag;
import document.HtmlText;
import error.Logger;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextRenderer {
    private final boolean isRtl;
    private final RenderingContext ctx;

    private final Deque<RenderingComponent> displayList = new ArrayDeque<>();
    FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, false);

    private int cursorX;
    private int cursorY;
    private int fontStyle = Font.PLAIN;
    private int fontSize = 16;

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
            case null, default -> {
            }
        }
    }

    private void processHtmlText(HtmlText text) {
        Font font = new Font("SF Pro", fontStyle, fontSize);
        int direction = isRtl ? -1 : 1;

        for (String tk : splitText(text.toString())) {
            int wordWidth = (int) font.getStringBounds(tk, frc).getWidth();
            int whitespaceWidth = (int) font.getStringBounds(" ", frc).getWidth();
            int lineHeight = (int) ((int) font.getLineMetrics(tk, frc).getHeight() * 1.25);

            if (Objects.equals(tk, "\n")) {
                cursorX = ctx.getBaseTextPosition().x;

                cursorY += lineHeight;
            }
            else if (Emoji.isEmoji(tk)) {
                displayList.add(new Emoji(tk, new Point(cursorX, cursorY), fontSize));

                cursorX += 16 * direction;
            }
            else {
                displayList.add(new Text(tk, new Point(cursorX, cursorY), font));

                cursorX += (wordWidth + whitespaceWidth) * direction;
            }

            if (cursorX + wordWidth >= ctx.getWidth()) {
                cursorX = ctx.getBaseTextPosition().x;
                cursorY += lineHeight;
            }
        }
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
