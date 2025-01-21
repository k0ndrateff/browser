package rendering;

import java.awt.*;
import java.util.HashMap;

public class FontCache {
    private static final HashMap<String, Font> fontCache = new HashMap<>();

    public static String getKey(String fontName, int style, int size) {
        return fontName + "/" + style + "/" + size;
    }

    public static Font retrieve(String fontName, int style, int size) {
        String key = FontCache.getKey(fontName, style, size);

        if (!fontCache.containsKey(key)) {
            Font font = new Font(fontName, style, size);

            fontCache.put(key, font);

            return font;
        }
        else {
            return fontCache.get(key);
        }
    }
}
