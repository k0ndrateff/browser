package networking.request.http;

import java.io.IOException;
import java.io.InputStream;

public class HttpStreamReader {
    private final InputStream inputStream;

    public HttpStreamReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String readHeaders() throws IOException {
        StringBuilder headersBuilder = new StringBuilder();
        String line;

        while (!(line = readLine()).isEmpty()) {
            headersBuilder.append(line).append("\r\n");
        }

        return headersBuilder.toString();
    }

    public byte[] readBody(int contentLength) throws IOException {
        byte[] body = new byte[contentLength];
        int totalBytesRead = 0;

        while (totalBytesRead < contentLength) {
            int bytesRead = inputStream.read(body, totalBytesRead, contentLength - totalBytesRead);
            if (bytesRead == -1) {
                break; // End of stream
            }
            totalBytesRead += bytesRead;
        }

        if (totalBytesRead < contentLength) {
            throw new IOException("Incomplete networking.response body received. Expected " + contentLength + " bytes, got " + totalBytesRead);
        }

        return body;
    }

    private String readLine() throws IOException {
        StringBuilder line = new StringBuilder();
        int prev = -1, curr;

        while ((curr = inputStream.read()) != -1) {
            if (prev == '\r' && curr == '\n') {
                break;
            }
            if (curr != '\r') {
                line.append((char) curr);
            }
            prev = curr;
        }

        return line.toString();
    }
}
