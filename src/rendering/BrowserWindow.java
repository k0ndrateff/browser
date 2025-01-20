package rendering;

import document.Entity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

public class BrowserWindow extends JFrame implements ComponentListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private final PageCanvas canvas;

    private ArrayList<Entity> tokens;
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

    public void displayText(ArrayList<Entity> tokens, boolean isRtl) {
        this.tokens = tokens;
        this.isRtl = isRtl;

        Point textPosition = isRtl ? new Point(canvas.getDrawingWidth() - 40, 20) : new Point(20, 20);

        displayList = new DisplayList();
        displayList.layoutText(tokens, textPosition, canvas.getDrawingWidth(), isRtl);
        canvas.setText(displayList);

        repaint();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if (canvas != null) {
            canvas.changeSize(getWidth(), getHeight());
            displayText(tokens, isRtl);
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {}

    @Override
    public void componentShown(ComponentEvent e) {}

    @Override
    public void componentHidden(ComponentEvent e) {}
}
