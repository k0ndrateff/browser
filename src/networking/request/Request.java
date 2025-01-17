package networking.request;

import error.NotImplementedException;
import networking.URL;
import networking.UrlScheme;
import networking.request.http.HttpRequest;
import networking.request.http.HttpsRequest;
import networking.response.Response;

public abstract class Request {
    protected URL url;

    public Request(URL url) {
        this.url = url;
    }

    public static Request create(URL url) {
        if (url.getScheme().equals(UrlScheme.HTTP)) {
            return new HttpRequest(url);
        }
        if (url.getScheme().equals(UrlScheme.HTTPS)) {
            return new HttpsRequest(url);
        }
        if (url.getScheme().equals(UrlScheme.FILE)) {
            return new FileRequest(url);
        }
        if (url.getScheme().equals(UrlScheme.DATA)) {
            return new DataRequest(url);
        }

        throw new NotImplementedException("Non-HTTP protocol handling");
    }

    public abstract Response make();
}
