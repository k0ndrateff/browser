package request;

import error.Logger;
import error.NotImplementedException;
import response.FileResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileRequest extends Request {
    public FileRequest(URL url) {
        super(url);
    }

    @Override
    public FileResponse make() {
        Logger.verbose("Making file request...");

        File file = new File(this.url.getPath());
        StringBuilder result = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(file)) {
            result.append(new String(fis.readAllBytes(), StandardCharsets.UTF_8));
        }
        catch (IOException e) {
            Logger.error(e);
            throw new NotImplementedException("Handling broken file requests");
        }

        return new FileResponse(result.toString());
    }
}
