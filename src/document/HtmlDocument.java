package document;

public class HtmlDocument {
    private final String content;
    private boolean isRtl;
    private boolean isViewSource;

    public HtmlDocument(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setRtl(boolean rtl) {
        this.isRtl = rtl;
    }

    public boolean isRtl() {
        return isRtl;
    }

    public void setViewSource(boolean viewSource) {
        this.isViewSource = viewSource;
    }

    public boolean isViewSource() {
        return isViewSource;
    }
}
