package rendering.component;

import rendering.PaintingContext;

import java.awt.*;

public class Rectangle extends RenderingComponent {
    private final int width;
    private final int height;
    private final Color color;

    public Rectangle(Point position, int width, int height, Color color) {
        this.position = position;
        this.width = width;
        this.height = height;
        this.color = color;

        this.layer = 0;
    }

    @Override
    public void paint(Graphics g, PaintingContext ctx) {
        g.setColor(color);
        g.fillRect(position.x, position.y - ctx.getScrollY() - 12, width, height);
    }
}
