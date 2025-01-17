package networking.response;

import document.HtmlDocument;
import error.Logger;
import error.NotImplementedException;
import networking.URL;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HttpResponse extends Response<HtmlDocument> {
    private final URL url;

    private String version;
    private String status;
    private String explanation;
    private Map<String, String> headers;
    private HtmlDocument body;

    private boolean isRetrievedFromCache = false;

    public HttpResponse(URL url) {
        super();

        this.url = url;
        this.isRetrievedFromCache = true;
    }

    public HttpResponse(URL url, String response) {
        super();

        this.url = url;
        this.processResponseHeaders(response);

        Logger.verbose("Received " + version + " response: " + status + " " + explanation);
    }

    private void processResponseHeaders(String response) {
        Map<String, String> headers = new HashMap<>();

        String[] responseParts = response.split("\r\n");
        String[] statusLineParts = responseParts[0].split(" ", 3);

        version = statusLineParts[0];
        status = statusLineParts[1];
        explanation = statusLineParts[2];

        for (int i = 1; i < responseParts.length; i++) {
            String[] headerParts = responseParts[i].split(":", 2);

            headers.put(headerParts[0].toLowerCase(Locale.ROOT), headerParts[1].toLowerCase(Locale.ROOT).trim());
        }

        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = new HtmlDocument(body);
        this.isPending = false;
    }

    @Override
    public HtmlDocument getData() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getStatus() {
        return status;
    }

    public int getContentLength() {
        if (headers.containsKey("content-length")) {
            return Integer.parseInt(headers.get("content-length"));
        }

        return 0;
    }

    public String getRedirectLocation() {
        if (status.startsWith("3") && headers.containsKey("location")) {
            return headers.get("location");
        }

        else return null;
    }

    public String getContentEncoding() {
        return headers.get("content-encoding");
    }

    public boolean isTransferEncodingChunked() {
        if (headers.containsKey("transfer-encoding")) {
            if (headers.get("transfer-encoding").equals("chunked")) return true;

            throw new NotImplementedException("Handling non-chunked transfer-encoding");
        }

        return false;
    }

    public URL getUrl() {
        return url;
    }
}
