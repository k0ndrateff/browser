package request;

import error.NotImplementedException;
import response.Response;

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

        throw new NotImplementedException("Non-HTTP protocol handling");
    }

    public Response make() {
        throw new AbstractMethodError("Request.make() can be called only from subclasses");
    }
}
