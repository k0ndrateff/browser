package rendering;

import java.awt.*;
import java.util.HashMap;
import java.util.Objects;

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

    public static boolean isFontFamilyAvailable(String fontName) {
        if (Objects.equals(fontName, "sans-serif") || Objects.equals(fontName, "serif") || Objects.equals(fontName, "monospace"))
            return true;

        GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = g.getAvailableFontFamilyNames();

        for (String font : fonts) {
            if (font.equals(fontName)) {
                return true;
            }
        }

        return false;
    }
}
