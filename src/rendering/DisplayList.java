package rendering;

import error.Logger;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisplayList {
    private final Deque<RenderingComponent> displayList = new ArrayDeque<>();
    private final Font font = new Font("SF Pro", Font.PLAIN, 16);
    FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, false);

    public void layoutText(String text, Point position, int width, boolean isRtl) {
        Logger.verbose("Laying out text...");

        int direction = isRtl ? -1 : 1;
        int cursorX = position.x;
        int cursorY = position.y;

        for (String token : splitText(text)) {
            int wordWidth = (int) font.getStringBounds(token, frc).getWidth();
            int whitespaceWidth = (int) font.getStringBounds(" ", frc).getWidth();
            int lineHeight = (int) ((int) font.getLineMetrics(token, frc).getHeight() * 1.25);

            if (Objects.equals(token, "\n")) {
                cursorX = position.x;

                cursorY += lineHeight;
            }
            else if (Emoji.isEmoji(token)) {
                displayList.add(new Emoji(token, new Point(cursorX, cursorY)));

                cursorX += 16 * direction;
            }
            else {
                displayList.add(new Character(token, new Point(cursorX, cursorY)));

                cursorX += (wordWidth + whitespaceWidth) * direction;
            }

            if (cursorX + wordWidth >= width) {
                cursorX = position.x;
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

    public int getLastEntryY() {
        if (displayList.isEmpty()) {
            return 0;
        }

        return displayList.getLast().getPosition().y;
    }
}
