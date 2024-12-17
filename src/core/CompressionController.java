package core;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class CompressionController {
    public static String decode(byte[] contents, String method) {
        if (method.equals("gzip")) {
            return CompressionController.decodeGzip(contents);
        }

        System.err.println("Unsupported decoding method: " + method);
        return new String(contents, StandardCharsets.UTF_8);
    }

    private static String decodeGzip(byte[] contents) {
        InputStream is = new ByteArrayInputStream(contents);

        try {
            return new String(new GZIPInputStream(is).readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            System.err.println("Error decoding GZIP contents: " + e.getMessage());
        }

        return null;
    }

    public static byte[] readChunkedResponse(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        ByteArrayOutputStream chunkedData = new ByteArrayOutputStream();

        try {
            while (true) {
                String chunkSizeLine = reader.readLine();
                if (chunkSizeLine == null) {
                    throw new IOException("Unexpected end of stream while reading chunk size");
                }

                int chunkSize;
                try {
                    chunkSize = Integer.parseInt(chunkSizeLine.trim(), 16);
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid chunk size: " + chunkSizeLine, e);
                }

                if (chunkSize == 0) {
                    break;
                }

                byte[] chunkData = new byte[chunkSize];
                int bytesRead = 0;
                while (bytesRead < chunkSize) {
                    int result = inputStream.read(chunkData, bytesRead, chunkSize - bytesRead);
                    if (result == -1) {
                        throw new IOException("Unexpected end of stream while reading chunk data");
                    }
                    bytesRead += result;
                }
                chunkedData.write(chunkData);

                String trailingLine = reader.readLine();
                if (trailingLine == null || !trailingLine.isEmpty()) {
                    throw new IOException("Invalid chunk format: missing or malformed trailing CRLF");
                }
            }

            while (true) {
                String trailerLine = reader.readLine();
                if (trailerLine == null || trailerLine.isEmpty()) {
                    break;
                }
            }
        }
        catch (IOException e) {
            System.err.println("Error reading chunk response: " + e.getMessage());
        }

        return chunkedData.toByteArray();
    }
}
