package rendering.component;

import core.Browser;
import error.Logger;
import rendering.PaintingContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class Emoji extends RenderingComponent {
    private static final String EMOJI_DIR = "emojis";
    private final String emoji;
    private final int size;

    public Emoji(String emoji, Point position, int size) {
        this.emoji = emoji;
        this.position = position;
        this.size = size;

        this.layer = 1;
    }

    public static boolean isEmoji(String ch) {
        int[] codePoints = ch.codePoints().toArray();

        Set<Character.UnicodeBlock> emojiBlocks = Set.of(
                Character.UnicodeBlock.EMOTICONS,
                Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS,
                Character.UnicodeBlock.SUPPLEMENTAL_SYMBOLS_AND_PICTOGRAPHS,
                Character.UnicodeBlock.TRANSPORT_AND_MAP_SYMBOLS
        );

        boolean isEmoji = false;
        for (int codePoint : codePoints) {
            if (emojiBlocks.contains(Character.UnicodeBlock.of(codePoint)) ||
                    Character.getType(codePoint) == Character.OTHER_SYMBOL) {
                isEmoji = true;
            }
        }

        if (codePoints.length > 1 && codePoints[codePoints.length - 1] == 0xFE0F) {
            isEmoji = true;
        }

        return isEmoji;
    }
    
    private String getEmojiCode() {
        int[] codePoints = emoji.codePoints().toArray();

        return Arrays.stream(codePoints)
                .mapToObj(cp -> String.format("%04X", cp)) // Convert to uppercase hex
                .collect(Collectors.joining("-"));
    }

    private String getFile() throws IOException {
        return Browser.getResource(EMOJI_DIR + getEmojiCode() + ".png");
    }

    @Override
    public void paint(Graphics g, PaintingContext ctx) {
        String emojiFile = "";

        try {
            emojiFile = getFile();
        } catch (IOException e) {
            Logger.error(e);
        }

        if (emojiFile.isEmpty()) {
            Logger.verbose("Emoji from file " + getEmojiCode() + "  not found! Rendering as text instead...");

            g.drawString(String.valueOf(emoji), position.x, position.y + 28 - ctx.getScrollY());
        }
        else {
            try {
                BufferedImage img = ImageIO.read(new File(emojiFile));

                g.drawImage(img, position.x, position.y + 28 - ctx.getScrollY(), size, size, null);
            } catch (IOException e) {
                Logger.error(e);
                g.drawString(String.valueOf(emoji), position.x, position.y + 28 - ctx.getScrollY());
            }
        }
    }
}
