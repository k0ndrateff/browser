package core;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class WindowController extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private final DrawingCanvas canvas;

    // Custom canvas for drawing
    private static class DrawingCanvas extends JPanel {
        private final BufferedImage canvasImage;
        private final Graphics2D canvasGraphics;

        public DrawingCanvas() {
            canvasImage = new BufferedImage(WindowController.WIDTH, WindowController.HEIGHT, BufferedImage.TYPE_INT_ARGB);
            canvasGraphics = canvasImage.createGraphics();

            canvasGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            canvasGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(canvasImage, 0, 0, null);
        }

        public void drawChar(String text, int x, int y) {
            canvasGraphics.setColor(Color.BLACK);
            canvasGraphics.drawString(text, x, y);
            repaint();
        }

        public void cleanup() {
            canvasGraphics.dispose();
        }
    }

    public WindowController() {
        setTitle("k0ndrateff/browser");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        this.canvas = new DrawingCanvas();
        canvas.setBackground(Color.WHITE);
        add(canvas);

        setLocationRelativeTo(null);
    }

    public void createWindow() {
        SwingUtilities.invokeLater(() -> {
            this.setVisible(true);
        });
    }

    public void cleanup() {
        this.canvas.cleanup();
    }

    private void drawText(String text) {
        final int HSTEP = 13;
        final int VSTEP = 18;

        int cursorX = HSTEP;
        int cursorY = VSTEP;

        for (int i = 0; i < text.length(); i++) {
            this.canvas.drawChar(String.valueOf(text.charAt(i)), cursorX, cursorY);
            cursorX += HSTEP;

            if (cursorX >= WIDTH - HSTEP) {
                cursorX = HSTEP;
                cursorY += VSTEP;
            }
        }
    }

    public void drawPage(String content) {
        this.drawText(content);
    }
}
