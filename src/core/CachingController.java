package core;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CachingController {
    private final String containerPath;
    private final Map<String, String> cacheMap = new HashMap<>();

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
                }
            }
        }
        catch (IOException e) {
            System.err.println("Error initializing cache controller: " + e.getMessage());
        }
    }

    public boolean contains(String host, short port, String path) {
        return cacheMap.containsKey(host + ":" + port + path);
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

    public void put(String host, short port, String path, String content) {
        String fileName = UUID.randomUUID().toString();

        cacheMap.put(host + ":" + port + path, fileName);

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
                bw.write(entry.getKey() + " " + entry.getValue() + "\n");
            }

            bw.flush();
        }
    }

    public void clearCache() {
        File cacheMapFile = new File(this.containerPath + "/.cachemap");

        if (cacheMapFile.exists()) {
            cacheMapFile.delete();
        }
    }

    public void shutdown() {
        try {
            this.updateCacheMapFile();
        }
        catch (IOException e) {
            System.err.println("Error shutting down cache controller: " + e.getMessage());
        }
    }
}
