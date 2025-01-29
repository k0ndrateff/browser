package rendering.styles;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CssParser {
    private static final Character[] SPECIAL_CHARACTERS = new Character[] { '#', '-', '.', '%' };

    private final String css;
    private int pointer;

    public CssParser(String css) {
        this.css = css;
        this.pointer = 0;
    }

    private void whitespace() {
        while (pointer < css.length() && Character.isWhitespace(css.charAt(pointer))) {
            pointer++;
        }
    }

    private String word() {
        int start = pointer;

        while (pointer < css.length()) {
            if (Character.isAlphabetic(css.charAt(pointer)) || Character.isDigit(css.charAt(pointer)) || Arrays.asList(SPECIAL_CHARACTERS).contains(css.charAt(pointer))) {
                pointer++;
            }
            else {
                break;
            }
        }

        if (!(pointer > start)) {
            throw new IllegalStateException("CSS word parsing error");
        }

        return css.substring(start, pointer);
    }

    private void literal(Character literal) {
        if (!(pointer < css.length() && css.charAt(pointer) == literal)) {
            throw new IllegalStateException("CSS literal parsing error");
        }

        pointer++;
    }

    private CssPropertyValue pair() {
        String property = word();
        whitespace();
        literal(':');
        whitespace();
        String value = word();
        return new CssPropertyValue(property, value);
    }

    public Map<String, CssPropertyValue> parse() {
        Map<String, CssPropertyValue> pairs = new HashMap<>();

        while (pointer < css.length()) {
            try {
                CssPropertyValue value = pair();
                pairs.put(value.getProperty(), value);
                whitespace();
                literal(';');
                whitespace();
            }
            catch (Exception e) {
                Character why = ignoreUntil(new Character[] { ';' });

                if (why != null && why == ';') {
                    literal(';');
                    whitespace();
                }
                else {
                    break;
                }
            }
        }

        return pairs;
    }

    private Character ignoreUntil(Character[] chars) {
        while (pointer < css.length()) {
            if (Arrays.asList(chars).contains(css.charAt(pointer))) {
                return css.charAt(pointer);
            }
            else {
                pointer++;
            }
        }

        return null;
    }
}
