package request;

import error.Logger;
import error.NotImplementedException;
import response.HttpResponse;

import javax.net.ssl.SSLSocketFactory;
import java.net.Socket;

public class HttpsRequest extends HttpRequest {
    private static final int DEFAULT_PORT = 443;

    public HttpsRequest(URL url) {
        super(url);
    }

    @Override
    public HttpResponse make() {
        int port = DEFAULT_PORT;

        if (this.url.isPortDefined()) {
            port = this.url.getPort();
        }

        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

        try (Socket socket = sslSocketFactory.createSocket(this.url.getHost(), port)) {
            return this.performRequestWithSocket(socket);
        }
        catch (Exception e) {
            Logger.error(e);

            throw new NotImplementedException("Handling broken requests");
        }
    }
}
