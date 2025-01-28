package rendering;

import error.Logger;
import rendering.component.RenderingComponent;
import rendering.layout.Layout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PageCanvas extends JComponent implements KeyListener, MouseWheelListener {
    private static final int SCROLL_SPEED = 20;
    private int scrollY = 0;

    JScrollBar scrollBar = new JScrollBar(JScrollBar.VERTICAL);

    Layout layoutTreeHead;
    PaintingContext paintingContext = new PaintingContext(scrollY);

    public PageCanvas() {
        setSize(800, 600);
        setFocusable(true);
        addKeyListener(this);
        addMouseWheelListener(this);

        this.setLayout(new BorderLayout());

        scrollBar.setUnitIncrement(SCROLL_SPEED);
        scrollBar.addAdjustmentListener(e -> {
            scrollY = e.getValue();

            paintingContext.setScrollY(scrollY);
            repaint();
        });

        add(scrollBar, BorderLayout.EAST);
    }

    public int getDrawingWidth() {
        return getWidth() - scrollBar.getWidth();
    }

    public void changeSize(int width, int height) {
        Logger.verbose("Browser window resized to " + width + "×" + height);

        setSize(width, height);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.clearRect(0, 0, getDrawingWidth(), getHeight());

        if (layoutTreeHead != null) {
            paintTree(g, layoutTreeHead);
        }
    }

    private void paintTree(Graphics g, Layout layoutNode) {
        this.paintTreeLayer(g, layoutNode, 0);
        this.paintTreeLayer(g, layoutNode, 1);
    }

    private void paintTreeLayer(Graphics g, Layout layoutNode, int layer) {
        for (RenderingComponent component : layoutNode.getDisplayList()) {
            if (component.getPosition().y > scrollY + getHeight()) continue;
            if (component.getPosition().y + 18 < scrollY) continue;
            if (component.getLayer() != layer) continue;

            component.paint(g, paintingContext);
        }

        for (Layout child : layoutNode.getChildren()) {
            paintTreeLayer(g, child, layer);
        }
    }

    public void setText(Layout layoutTreeHead) {
        this.layoutTreeHead = layoutTreeHead;

        if (layoutTreeHead.getHeight() > this.getHeight()) {
            scrollBar.setMaximum(getMaxScrollY());
        }
        else {
            scrollBar.setMaximum(0);
        }

        repaint();
    }

    private int getMaxScrollY() {
        return layoutTreeHead.getHeight() - this.getHeight() + 20;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        handleKeyEvent(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        handleKeyEvent(e);
    }

    private void handleKeyEvent(KeyEvent e) {
        if (layoutTreeHead.getHeight() <= this.getHeight()) return;

        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            this.scrollY += SCROLL_SPEED;

            if (scrollY > getMaxScrollY()) scrollY = getMaxScrollY();
            repaint();
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            this.scrollY -= SCROLL_SPEED;

            if (scrollY < 0) scrollY = 0;
            repaint();
        }

        paintingContext.setScrollY(scrollY);
        scrollBar.setValue(scrollY);
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (layoutTreeHead.getHeight() <= this.getHeight()) return;

        scrollY += e.getWheelRotation() * SCROLL_SPEED;

        if (scrollY < 0) scrollY = 0;
        if (scrollY > getMaxScrollY()) scrollY = getMaxScrollY();
        repaint();

        paintingContext.setScrollY(scrollY);
        scrollBar.setValue(scrollY);
    }
}
