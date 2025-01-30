package rendering.styles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CssParser {
    private static final Character[] SPECIAL_CHARACTERS = new Character[] { '#', '-', '.', '%', ',', '"' };

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
        boolean inQuoted = false;

        while (pointer < css.length()) {
            if (css.charAt(pointer) == '"') {
                inQuoted = !inQuoted;
            }

            if (inQuoted || Character.isAlphabetic(css.charAt(pointer)) || Character.isDigit(css.charAt(pointer)) || Arrays.asList(SPECIAL_CHARACTERS).contains(css.charAt(pointer))) {
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

    private CssRule pair() {
        String property = word();
        whitespace();
        literal(':');
        whitespace();
        String value = word();
        return new CssRule(property, value);
    }

    public Map<String, CssRule> body() {
        Map<String, CssRule> pairs = new HashMap<>();

        while (pointer < css.length() && css.charAt(pointer) != '}') {
            try {
                CssRule value = pair();
                pairs.put(value.getProperty(), value);
                whitespace();
                literal(';');
                whitespace();
            }
            catch (Exception e) {
                Character why = ignoreUntil(new Character[] { ';', '}' });

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

    private Selector selector() {
        Selector out = new TagSelector(word().toLowerCase());
        whitespace();

        while (pointer < css.length() && css.charAt(pointer) != '{') {
            String tag = word();
            Selector descendant = new TagSelector(tag.toLowerCase());
            out = new DescendantSelector(out, descendant);
            whitespace();
        }

        return out;
    }

    public ArrayList<CssBlock> parse() {
        ArrayList<CssBlock> blocks = new ArrayList<>();

        while (pointer < css.length()) {
            try {
                whitespace();
                Selector selector = selector();
                literal('{');
                whitespace();
                Map<String, CssRule> pairs = body();
                literal('}');
                blocks.add(new CssBlock(selector, pairs));
            }
            catch (Exception e) {
                Character why = ignoreUntil(new Character[] { '}' });

                if (why != null && why == '}') {
                    literal('}');
                    whitespace();
                }
                else {
                    break;
                }
            }
        }

        return blocks;
    }
}
