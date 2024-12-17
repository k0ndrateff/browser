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
                    cacheMap.put(line.split(" ")[0], line.split(" ")[1]);

                    if (line.split(" ").length > 2) {
                        ttlMap.put(line.split(" ")[0], Instant.parse(line.split(" ")[2]));
                    }
                    else {
                        ttlMap.put(line.split(" ")[0], Instant.now());
                    }
                }
            }
        }
        catch (IOException e) {
            System.err.println("Error initializing cache controller: " + e.getMessage());
        }
    }

    public boolean contains(String host, short port, String path) {
        if (ttlMap.containsKey(host + ":" + port + path)) {
            Instant expires = ttlMap.get(host + ":" + port + path);
            Instant now = Instant.now();

            if (now.isBefore(expires)) {
                return cacheMap.containsKey(host + ":" + port + path);
            }
            else {
                cacheMap.remove(host + ":" + port + path);
                ttlMap.remove(host + ":" + port + path);

                return false;
            }
        }

        return false;
    }

    public String get(String host, short port, String path) {
        String fileName = cacheMap.get(host + ":" + port + path);

        File file = new File(this.containerPath + "/" + fileName);
        if (file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(this.containerPath + "/" + fileName));

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

        cacheMap.put(host + ":" + port + path, fileName);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, ttl);

        ttlMap.put(host + ":" + port + path, calendar.toInstant());

        try {
            File cacheFile = new File(this.containerPath + "/" + fileName);
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
