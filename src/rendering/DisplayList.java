package rendering;

import error.Logger;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Objects;

public class DisplayList {
    private final Deque<RenderingComponent> displayList = new ArrayDeque<>();

    public void layoutText(String text, Point position, int width, boolean isRtl) {
        Logger.verbose("Laying out text...");

        int hStep = isRtl ? -13 : 13;
        int vStep = 18;
        int cursorX = position.x + hStep;
        int cursorY = position.y + vStep;

        for (String c : this.splitText(text)) {
            if (Objects.equals(c, "\n")) {
                cursorX = position.x + hStep;

                cursorY += vStep;
            } else if (Emoji.isEmoji(c)) {
                displayList.add(new Emoji(c, new Point(cursorX, cursorY)));

                cursorX += hStep;
            } else {
                displayList.add(new Character(String.valueOf(c), new Point(cursorX, cursorY)));

                cursorX += hStep;
            }

            if (cursorX >= width - hStep) {
                cursorX = position.x + hStep;
                cursorY += vStep;
            }
        }
    }

    private String[] splitText(String text) {
        int[] codePoints = text.codePoints().toArray();

        return Arrays.stream(codePoints)
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
