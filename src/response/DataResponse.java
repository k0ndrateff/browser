package response;

import document.HtmlDocument;

public class DataResponse extends Response<HtmlDocument> {
    private final String mimeType;

    public DataResponse(HtmlDocument response, String mimeType) {
        this.response = response;
        this.mimeType = mimeType;
        this.isPending = false;
    }

    @Override
    public HtmlDocument getData() {
        return this.response;
    }
}
