package networking.request;

import document.HtmlDocument;
import error.Logger;
import networking.URL;
import networking.response.DataResponse;

public class DataRequest extends Request {
    public DataRequest(URL url) {
        super(url);
    }

    @Override
    public DataResponse make() {
        Logger.verbose("Making data request...");

        if (this.url.isAboutBlank()) {
            return new DataResponse(new HtmlDocument(""), "text/html");
        }

        String[] parts = this.url.getPath().split(",", 2);

        return new DataResponse(new HtmlDocument(parts[1]), parts[0]);
    }
}
