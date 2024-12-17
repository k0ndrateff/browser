package core;

import java.io.*;
import java.time.Instant;
import java.util.*;

public class CachingController {
    private final int MAX_CACHE_SIZE = 200;
    public final int DEFAULT_TTL = 86400;

    private final String containerPath;

    private final Map<String, String> cacheMap = new HashMap<>();
    private final Map<String, Instant> ttlMap = new HashMap<>();

    public CachingController(String containerPath) {
        this.containerPath = containerPath;
    }

    public void init() {
        String cacheMapPath = this.containerPath + "/.cachemap";
        File cacheMapFile = new File(cacheMapPath);

        try {
            boolean cacheMapFileDidntExist = cacheMapFile.createNewFile();

            if (!cacheMapFileDidntExist) {
                BufferedReader br = new BufferedReader(new FileReader(cacheMapPath));

                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(" ");

                    cacheMap.put(parts[0], parts[1]);

                    if (parts.length > 2) {
                        ttlMap.put(parts[0], Instant.parse(parts[2]));
                    }
                    else {
                        ttlMap.put(parts[0], Instant.now());
                    }
                }
            }
        }
        catch (IOException e) {
            System.err.println("Error initializing cache controller: " + e.getMessage());
        }
    }

    private String getMapKey(String host, short port, String path) {
        return host + ":" + port + path;
    }

    private String getFileName(String name) {
        return this.containerPath + "/" + name;
    }

    public boolean contains(String host, short port, String path) {
        String key = this.getMapKey(host, port, path);

        if (ttlMap.containsKey(key)) {
            Instant expires = ttlMap.get(key);
            Instant now = Instant.now();

            if (now.isBefore(expires)) {
                return cacheMap.containsKey(key);
            }
            else {
                cacheMap.remove(key);
                ttlMap.remove(key);

                return false;
            }
        }

        return false;
    }

    public String get(String host, short port, String path) {
        String key = this.getMapKey(host, port, path);
        String fileName = this.getFileName(cacheMap.get(key));

        File file = new File(fileName);
        if (file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(fileName));

                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                return sb.toString();
            }
           catch (IOException e) {
                System.err.println("Error getting cache file: " + e.getMessage());
           }
        }

        return null;
    }

    public void put(String host, short port, String path, String content, int ttl) {
        String fileName = UUID.randomUUID().toString();

        String key = this.getMapKey(host, port, path);

        cacheMap.put(key, fileName);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, ttl);

        ttlMap.put(key, calendar.toInstant());

        try {
            File cacheFile = new File(this.getFileName(fileName));
            boolean cacheFileDidntExist = cacheFile.createNewFile();

            if (cacheFileDidntExist) {
                BufferedWriter bw = new BufferedWriter(new FileWriter(cacheFile));

                bw.write(content);
                bw.flush();
            }
        }
       catch (IOException e) {
            System.err.println("Error writing cache file: " + e.getMessage());
       }
    }

    private void updateCacheMapFile() throws IOException {
        File cacheMapFile = new File(this.containerPath + "/.cachemap");

        if (cacheMapFile.exists()) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(cacheMapFile));

            for (Map.Entry<String, String> entry : cacheMap.entrySet()) {
                bw.write(entry.getKey() + " " + entry.getValue());

                if (ttlMap.containsKey(entry.getKey())) {
                    bw.write(" " + ttlMap.get(entry.getKey()) + "\n");
                }
                else {
                    bw.write("\n");
                }
            }

            bw.flush();
        }
    }

    public void clearCache() {
        try {
            File dir = new File(this.containerPath);

            for(File file: Objects.requireNonNull(dir.listFiles()))
                if (!file.isDirectory())
                    file.delete();
        }
        catch (NullPointerException e) {
            System.err.println("Error clearing cache: " + e.getMessage());
        }
    }

    public void shutdown() {
        try {
            if (this.cacheMap.size() < MAX_CACHE_SIZE) {
                this.updateCacheMapFile();
            }
            else {
                this.clearCache();
            }
        }
        catch (IOException e) {
            System.err.println("Error shutting down cache controller: " + e.getMessage());
        }
    }
}
