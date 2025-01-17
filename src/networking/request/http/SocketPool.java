package networking.request.http;

import error.Logger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SocketPool {
    private static final int DEFAULT_TIMEOUT = 5000;

    private static final Map<String, Socket> socketPool = new HashMap<>();
    private static final Map<String, SSLSocket> sslSocketPool = new HashMap<>();

    private static final SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

    private static String getKey(String host, int port) {
        return host + ":" + port;
    }

    public static Socket getSocket(String host, int port) throws IOException {
        String key = getKey(host, port);

        if (!socketPool.containsKey(key)) {
            Logger.verbose("Open socket not found for " + host + ":" + port + ", creating new one...");

            Socket socket = new Socket(host, port);
            socket.setSoTimeout(DEFAULT_TIMEOUT);

            socketPool.put(key, socket);

            return socket;
        }
        else {
            Logger.verbose("Reusing socket for " + host + ":" + port);

            return socketPool.get(key);
        }
    }

    public static SSLSocket getSslSocket(String host, int port) throws IOException {
        String key = getKey(host, port);

        if (!sslSocketPool.containsKey(key)) {
            Logger.verbose("Open SSL socket not found for " + host + ":" + port + ", creating new one...");

            SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, port);
            socket.setSoTimeout(DEFAULT_TIMEOUT);

            sslSocketPool.put(key, socket);

            return socket;
        }
        else {
            Logger.verbose("Reusing SSL socket for " + host + ":" + port);

            return sslSocketPool.get(key);
        }
    }
}
