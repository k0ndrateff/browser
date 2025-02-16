package rendering;

import java.awt.*;

public class RenderingContext {
    private Point position;
    private int width;

    private boolean isRtl;

    public RenderingContext() {}

    public void setPosition(Point position) {
        this.position = position;
    }

    public Point getPosition() {
        return position;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public void setIsRtl(boolean isRtl) {
        this.isRtl = isRtl;
    }

    public boolean isRtl() {
        return isRtl;
    }
}
