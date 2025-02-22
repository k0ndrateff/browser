package networking.request.http;

import error.Logger;
import error.NotImplementedException;
import networking.request.Request;
import networking.URL;
import networking.response.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest extends Request {
    private static final String DEFAULT_METHOD = "GET";
    private static final String HTTP_VERSION = "HTTP/1.1";
    private static final int DEFAULT_PORT = 80;

    private static final int MAX_REDIRECTS = 10;
    private static int redirectCount = 0;

    public HttpRequest(URL url) {
        super(url);
    }

    private Map<String, String> getRequestHeaders() {
        Map<String, String> headers = new HashMap<>();

        headers.put("Connection", "keep-alive");
        headers.put("User-Agent", "k0ndrateff/browser");
        headers.put("Accept-Encoding", "gzip");

        return headers;
    }

    private String getRequestHeader() {
        Map<String, String> headers = getRequestHeaders();

        StringBuilder request = new StringBuilder(DEFAULT_METHOD + " " + url.getPath() + " " + HTTP_VERSION + "\r\n");
        request.append("Host: ").append(url.getHost()).append("\r\n");

        for (Map.Entry<String, String> header : headers.entrySet()) {
            request.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        request.append("\r\n");

        return request.toString();
    }

    protected HttpResponse performRequestWithSocket(Socket socket) throws IOException {
        socket.getOutputStream().write(getRequestHeader().getBytes(StandardCharsets.UTF_8));

        InputStream inputStream = socket.getInputStream();
        HttpStreamReader reader = new HttpStreamReader(inputStream);

        HttpResponse response = new HttpResponse(this.url, reader.readHeaders());

        int contentLength = response.getContentLength();

        if (contentLength != 0) {
            Logger.verbose("HTTP Content-Length: " + contentLength);
        }
        else {
            Logger.verbose("No Content-Length, possibly HTTP Transfer-Encoding: chunked");
        }

        byte[] bodyBytes;

        if (response.isTransferEncodingChunked()) {
            bodyBytes = reader.readChunkedBody();
        }
        else {
            bodyBytes = reader.readBody(contentLength);
        }

        String decodedBody = HttpCompression.decode(bodyBytes, response.getContentEncoding());
        response.setBody(decodedBody);

        String redirectLocation = response.getRedirectLocation();

        if (redirectLocation != null) {
            if (redirectCount++ > MAX_REDIRECTS) {
                throw new NotImplementedException("Handling too many redirects");
            }

            Logger.verbose("Redirecting to " + response.getHeaders().get("location"));

            return (HttpResponse) Request.create(this.url.getRedirectedUrl(redirectLocation)).make();
        }

        HttpCache.store(response);

        return response;
    }

    @Override
    public HttpResponse make() {
        HttpResponse response = HttpCache.retrieve(this.url);

        if (response != null) {
            return response;
        }

        Logger.verbose("Making HTTP request...");

        int port = DEFAULT_PORT;

        if (this.url.isPortDefined()) {
            port = this.url.getPort();
        }

        try (Socket socket = SocketPool.getSocket(this.url.getHost(), port)) {
            return this.performRequestWithSocket(socket);
        }
        catch (Exception e) {
            Logger.error(e);

            throw new NotImplementedException("Handling broken requests");
        }
    }
}
