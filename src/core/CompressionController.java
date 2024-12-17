package core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
}
