package rendering;

import error.Logger;

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
    }

    public static boolean isEmoji(String ch) {
        int[] codePoints = ch.codePoints().toArray();

        Set<java.lang.Character.UnicodeBlock> emojiBlocks = Set.of(
                java.lang.Character.UnicodeBlock.EMOTICONS,
                java.lang.Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS,
                java.lang.Character.UnicodeBlock.SUPPLEMENTAL_SYMBOLS_AND_PICTOGRAPHS,
                java.lang.Character.UnicodeBlock.TRANSPORT_AND_MAP_SYMBOLS
        );

        boolean isEmoji = false;
        for (int codePoint : codePoints) {
            if (emojiBlocks.contains(java.lang.Character.UnicodeBlock.of(codePoint)) ||
                    java.lang.Character.getType(codePoint) == java.lang.Character.OTHER_SYMBOL) {
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

    private File getFile() {
        return new File(EMOJI_DIR, getEmojiCode() + ".png");
    }

    @Override
    public void paint(Graphics g, PaintingContext ctx) {
        File file = getFile();

        if (!file.exists()) {
            Logger.verbose("Emoji from file " + file.getAbsolutePath() + "  not found! Rendering as text instead...");

            g.drawString(String.valueOf(emoji), position.x, position.y - ctx.getScrollY());
        }
        else {
            try {
                BufferedImage img = ImageIO.read(file);

                // TODO: somehow calculate magic number 12, which is emoji image offset
                g.drawImage(img, position.x, position.y - ctx.getScrollY() - 12, size, size, null);
            } catch (IOException e) {
                Logger.error(e);
                g.drawString(String.valueOf(emoji), position.x, position.y - ctx.getScrollY());
            }
        }
    }
}
