package networking.response;

import document.HtmlDocument;
import networking.URL;

public class FileResponse extends Response<HtmlDocument> {
    public FileResponse(String response) {
        this.response = new HtmlDocument(response, new URL("about:blank"));
        this.isPending = false;
    }

    @Override
    public HtmlDocument getData() {
        return this.response;
    }
}
