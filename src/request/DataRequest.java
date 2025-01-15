package request;

import document.HtmlDocument;
import response.DataResponse;

public class DataRequest extends Request {
    public DataRequest(URL url) {
        super(url);
    }

    @Override
    public DataResponse make() {
        String[] parts = this.url.getPath().split(",", 2);

        return new DataResponse(new HtmlDocument(parts[1]), parts[0]);
    }
}
