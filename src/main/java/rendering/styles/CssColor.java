package rendering.styles;

import java.awt.*;

public class CssColor {
    private final String colorString;

    public CssColor(String colorString) {
        this.colorString = colorString;
    }

    public boolean isDisplayable() {
        return getColor() != null;
    }

    public Color getColor() {
        if (colorString.startsWith("#")) {
            return Color.decode(String.valueOf(Integer.parseInt(colorString.substring(1), 16)));
        }
        else {
            CssNamedColor color = CssNamedColor.fromName(colorString);

            if (color != null) {
                return Color.decode(String.valueOf(Integer.parseInt(color.getHex(), 16)));
            }

            return null;
        }
    }
}
