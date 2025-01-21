package rendering;

public class PaintingContext {
    private int scrollY;

    public PaintingContext(int scrollY) {
        this.scrollY = scrollY;
    }

    public int getScrollY() {
        return scrollY;
    }

    public void setScrollY(int scrollY) {
        this.scrollY = scrollY;
    }
}
