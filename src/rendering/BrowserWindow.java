package rendering;

import document.HtmlDocument;
import document.HtmlNode;
import document.HtmlParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class BrowserWindow extends JFrame implements ComponentListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private final PageCanvas canvas;

    private HtmlParser htmlParser;
    private HtmlNode htmlTreeHead;
    private final RenderingContext renderingContext;

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

        this.renderingContext = new RenderingContext();
        renderingContext.setWidth(canvas.getDrawingWidth());
    }

    public void renderHtmlDocument(HtmlDocument document) {
        this.htmlParser = new HtmlParser(document);
        this.htmlTreeHead = htmlParser.parse();

        renderingContext.setBaseTextPosition(document.isRtl() ? new Point(canvas.getDrawingWidth() - 40, 20) : new Point(20, 20));

        rerenderCurrentDocument();
    }

    private void rerenderCurrentDocument() {
        TextRenderer renderer = new TextRenderer(renderingContext, htmlParser.getDocument().isRtl());
        renderer.render(htmlTreeHead);
        canvas.setText(renderer);

        repaint();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if (canvas != null) {
            canvas.changeSize(getWidth(), getHeight());
            renderingContext.setWidth(canvas.getDrawingWidth());

            rerenderCurrentDocument();
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {}

    @Override
    public void componentShown(ComponentEvent e) {}

    @Override
    public void componentHidden(ComponentEvent e) {}
}
