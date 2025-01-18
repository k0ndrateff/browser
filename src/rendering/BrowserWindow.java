package rendering;

import javax.swing.*;
import java.awt.*;

public class BrowserWindow extends JFrame {
    private static final Point TEXT_POSITION = new Point(0, 0);
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private final PageCanvas canvas;

    private final DisplayList displayList = new DisplayList();

    public BrowserWindow() {
        super("k0ndrateff/browser");

        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);

        canvas = new PageCanvas();
        add(canvas);
    }

    public void displayText(String text) {
        displayList.layoutText(text, TEXT_POSITION, WIDTH);
        canvas.setText(displayList.getDisplayList());

        repaint();
    }
}
