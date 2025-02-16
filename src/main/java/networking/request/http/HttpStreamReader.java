package networking.request.http;

import java.io.ByteArrayOutputStream;
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
    
    public byte[] readChunkedBody() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String line;
    
        while ((line = readLine()) != null) {
            int chunkSize = Integer.parseInt(line.trim(), 16);

            if (chunkSize == 0) {
                readLine();
                break;
            }
    
            byte[] chunk = new byte[chunkSize];
            int totalBytesRead = 0;
    
            while (totalBytesRead < chunkSize) {
                int bytesRead = inputStream.read(chunk, totalBytesRead, chunkSize - totalBytesRead);
                if (bytesRead == -1) {
                    throw new IOException("Unexpected end of stream while reading chunked body");
                }
                totalBytesRead += bytesRead;
            }
    
            outputStream.write(chunk);
            readLine();
        }
    
        return outputStream.toByteArray();
    }

    public byte[] readBody(int contentLength) throws IOException {
        byte[] body = new byte[contentLength];
        int totalBytesRead = 0;

        while (totalBytesRead < contentLength) {
            int bytesRead = inputStream.read(body, totalBytesRead, contentLength - totalBytesRead);
            
            if (bytesRead == -1) {
                break;
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
    
        return line.length() > 0 || prev != -1 ? line.toString() : null; // Return null if end of stream is reached
    }
}
