package rendering;

import error.Logger;

import java.awt.*;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

public class DisplayList {
    private final Queue<RenderingComponent> displayList = new LinkedTransferQueue<>();

    public void layoutText(String text, Point position, int width) {
        Logger.verbose("Laying out text...");

        int hStep = 13;
        int vStep = 18;
        int cursorX = position.x + hStep;
        int cursorY = position.y + vStep;

        for (char c : text.toCharArray()) {
            if (c == '\n') {
                cursorX = position.x + hStep;

                cursorY += vStep;
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

    public Queue<RenderingComponent> getDisplayList() {
        return displayList;
    }
}
