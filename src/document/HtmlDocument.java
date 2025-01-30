package document;

import networking.*;

public class HtmlDocument {
    private final URL requestUrl;

    private final String content;
    private boolean isRtl;
    private boolean isViewSource;

    public HtmlDocument(String content, URL url) {
        this.content = content;
        this.requestUrl = url;
    }

    public String getContent() {
        return content;
    }

    public URL getRequestUrl() {
        return requestUrl;
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
