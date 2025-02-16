package rendering.styles;

import rendering.styles.selector.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CssParser {
    private static final Character[] SPECIAL_CHARACTERS = new Character[] { '#', '-', '.', '%', ',', '"', '!' };

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

    private String value() {
        int start = pointer;

        while (pointer < css.length()) {
            if (Character.isAlphabetic(css.charAt(pointer)) || Character.isDigit(css.charAt(pointer)) || Character.isWhitespace(css.charAt(pointer)) ||  Arrays.asList(SPECIAL_CHARACTERS).contains(css.charAt(pointer))) {
                pointer++;
            }
            else {
                break;
            }
        }

        if (!(pointer > start)) {
            throw new IllegalStateException("CSS value parsing error");
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
        String value = value();
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
        String selector = word().toLowerCase();

        Selector out = resolveSelector(selector);

        whitespace();

        while (pointer < css.length() && css.charAt(pointer) != '{') {
            selector = word().toLowerCase();

            Selector descendant = resolveSelector(selector);

            out = new DescendantSelector(out, descendant);
            whitespace();
        }

        return out;
    }

    private Selector resolveSelector(String selector) {
        if (selector.contains(":has")) {
            String[] parts = selector.split(":has");
            return new SelectorSequence(new Selector[]{resolveSelector(parts[0]), new HasSelector(resolveSelector(parts[1].substring(1, parts[1].length() - 1)))});
        }
        else if (selector.startsWith(".")) {
            return new ClassSelector(selector.substring(1));
        }
        else if (selector.contains(".")) {
            String[] parts = selector.split("\\.");
            Selector[] selectors = new Selector[parts.length];
            selectors[0] = new TagSelector(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                selectors[i] = new ClassSelector(parts[i]);
            }
            return new SelectorSequence(selectors);
        }
        else {
            return new TagSelector(selector);
        }
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

                Map<String, CssRule> importantPairs = new HashMap<>();

                pairs.forEach((prop, rule) -> {
                    if (rule.getValue().endsWith("!important")) {
                        importantPairs.put(prop, new CssRule(prop, rule.getValue().substring(0, rule.getValue().length() - 11)));
                    }
                });

                blocks.add(new CssBlock(selector, pairs));

                if (!importantPairs.isEmpty()) {
                    blocks.add(new CssBlock(selector.setPriority(10000), importantPairs));
                }
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
