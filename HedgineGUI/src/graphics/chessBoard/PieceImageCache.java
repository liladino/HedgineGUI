package graphics.chessBoard;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public final class PieceImageCache {
    private final Map<Character, BufferedImage> originals;
    private final Map<Character, BufferedImage> scaled = new HashMap<>();
    private int cachedSize = -1;

    public PieceImageCache(Map<Character, BufferedImage> originals) {
        this.originals = Map.copyOf(originals);
    }

    public BufferedImage get(char key, int size) {
        if (size != cachedSize) {
            rebuild(size);
        }
        return scaled.get(key);
    }

    private void rebuild(int size) {
        scaled.clear();
        cachedSize = size;

        if (size <= 0) {
            return;
        }

        for (Map.Entry<Character, BufferedImage> entry : originals.entrySet()) {
            BufferedImage destination =
                    new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

            Graphics2D graphics = destination.createGraphics();
            try {
                graphics.setRenderingHint(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                graphics.drawImage(entry.getValue(), 0, 0, size, size, null);
            } finally {
                graphics.dispose();
            }

            scaled.put(entry.getKey(), destination);
        }
    }
}