package rendering.component;

import rendering.PaintingContext;

import javax.swing.*;
import java.awt.*;

public abstract class RenderingComponent extends JComponent {
    protected Point position;
    protected int layer;

    public abstract void paint(Graphics g, PaintingContext ctx);

    public Point getPosition() {
        return this.position;
    }

    public int getLayer() {
        return this.layer;
    }
}
