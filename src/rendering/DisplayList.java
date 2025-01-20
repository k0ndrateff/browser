package rendering;

import document.Entity;
import document.Tag;
import document.Text;
import error.Logger;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisplayList {
    private final Deque<RenderingComponent> displayList = new ArrayDeque<>();
    FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, false);

    public void layoutText(ArrayList<Entity> tokens, Point position, int width, boolean isRtl) {
        Logger.verbose("Laying out text...");

        int direction = isRtl ? -1 : 1;
        int cursorX = position.x;
        int cursorY = position.y;

        int fontStyle = Font.PLAIN;

        for (Entity token : tokens) {
            if (token instanceof Tag) {
                String tk = token.getContent();

                if (Objects.equals(tk, "i")) {
                    fontStyle = Font.ITALIC;
                }
                else if (Objects.equals(tk, "b")) {
                    fontStyle = Font.BOLD;
                }
                else if (Objects.equals(tk, "/i") || Objects.equals(tk, "/b")) {
                    fontStyle = Font.PLAIN;
                }
            }
            else if (token instanceof Text) {
                Font font = new Font("SF Pro", fontStyle, 16);

                for (String tk : splitText(token.getContent())) {
                    int wordWidth = (int) font.getStringBounds(tk, frc).getWidth();
                    int whitespaceWidth = (int) font.getStringBounds(" ", frc).getWidth();
                    int lineHeight = (int) ((int) font.getLineMetrics(tk, frc).getHeight() * 1.25);

                    if (Objects.equals(tk, "\n")) {
                        cursorX = position.x;

                        cursorY += lineHeight;
                    }
                    else if (Emoji.isEmoji(tk)) {
                        displayList.add(new Emoji(tk, new Point(cursorX, cursorY)));

                        cursorX += 16 * direction;
                    }
                    else {
                        displayList.add(new Character(tk, new Point(cursorX, cursorY), font));

                        cursorX += (wordWidth + whitespaceWidth) * direction;
                    }

                    if (cursorX + wordWidth >= width) {
                        cursorX = position.x;
                        cursorY += lineHeight;
                    }
                }
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
