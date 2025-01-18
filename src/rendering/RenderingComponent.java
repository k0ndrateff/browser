package rendering;

import javax.swing.*;
import java.awt.*;

public abstract class RenderingComponent extends JComponent {
    protected Point position;

    public abstract void paint(Graphics g, int scrollY);

    public Point getPosition() {
        return this.position;
    }
}
