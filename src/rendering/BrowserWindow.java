package rendering;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class BrowserWindow extends JFrame implements ComponentListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private final PageCanvas canvas;

    private String text;
    private DisplayList displayList;
    private boolean isRtl;

    public BrowserWindow() {
        super("k0ndrateff/browser");

        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setFocusable(true);
        setResizable(true);
        setVisible(true);

        addComponentListener(this);

        canvas = new PageCanvas();
        add(canvas);
    }

    public void displayText(String text, boolean isRtl) {
        this.text = text;
        this.isRtl = isRtl;

        Point textPosition = isRtl ? new Point(canvas.getDrawingWidth() - 20, 0) : new Point(0, 0);

        displayList = new DisplayList();
        displayList.layoutText(text, textPosition, canvas.getDrawingWidth(), isRtl);
        canvas.setText(displayList);

        repaint();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if (canvas != null) {
            canvas.changeSize(getWidth(), getHeight());
            displayText(text, isRtl);
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {}

    @Override
    public void componentShown(ComponentEvent e) {}

    @Override
    public void componentHidden(ComponentEvent e) {}
}
