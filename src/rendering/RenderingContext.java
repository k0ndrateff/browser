package rendering;

import java.awt.*;

public class RenderingContext {
    private Point baseTextPosition;
    private int width;

    public RenderingContext() {}

    public void setBaseTextPosition(Point baseTextPosition) {
        this.baseTextPosition = baseTextPosition;
    }

    public Point getBaseTextPosition() {
        return baseTextPosition;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }
}
