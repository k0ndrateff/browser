package networking.response;

import document.HtmlDocument;

public class FileResponse extends Response<HtmlDocument> {
    public FileResponse(String response) {
        this.response = new HtmlDocument(response);
        this.isPending = false;
    }

    @Override
    public HtmlDocument getData() {
        return this.response;
    }
}
