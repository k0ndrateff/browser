package request;

import error.Logger;
import error.NotImplementedException;
import response.HttpResponse;
import java.net.Socket;

public class HttpsRequest extends HttpRequest {
    private static final int DEFAULT_PORT = 443;

    public HttpsRequest(URL url) {
        super(url);
    }

    @Override
    public HttpResponse make() {
        Logger.verbose("Making HTTPS request...");

        int port = DEFAULT_PORT;

        if (this.url.isPortDefined()) {
            port = this.url.getPort();
        }

        try (Socket socket = SocketPool.getSslSocket(this.url.getHost(), port)) {
            return this.performRequestWithSocket(socket);
        }
        catch (Exception e) {
            Logger.error(e);

            throw new NotImplementedException("Handling broken requests");
        }
    }
}
