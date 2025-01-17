package networking.request.http;

import error.Logger;
import error.NotImplementedException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class HttpCompression {
    public static String decode(byte[] data, String encoding) {
        if (encoding == null) {
            return new String(data, StandardCharsets.UTF_8);
        }

        if (encoding.equals("gzip")) {
            return decodeGzip(data);
        }

        throw new NotImplementedException("Content-Encoding " + encoding);
    }

    private static String decodeGzip(byte[] data) {
        Logger.verbose("Decoding Gzip response...");

        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
             GZIPInputStream gzipStream = new GZIPInputStream(byteStream);
             InputStreamReader reader = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {

            StringBuilder decodedString = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                decodedString.append(line).append("\n");
            }

            if (!decodedString.isEmpty()) {
                decodedString.setLength(decodedString.length() - 1);
            }

            return decodedString.toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to decode Gzip data", e);
        }
    }
}
