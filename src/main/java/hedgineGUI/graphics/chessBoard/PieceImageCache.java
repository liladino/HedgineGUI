package hedgineGUI.graphics.chessBoard;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

public final class PieceImageCache {
    private static final Logger LOGGER = Logger.getLogger(PieceImageCache.class.getName());

    private final Map<Character, BufferedImage> rasterFallbacks;
    private final PieceImageSource vectorSource;
    private final Map<Character, BufferedImage> rendered = new HashMap<>();
    private final Set<Character> unusableVectorImages = new HashSet<>();
    private int cachedWidth = -1;
    private int cachedHeight = -1;

    public PieceImageCache(
            Map<Character, BufferedImage> rasterFallbacks,
            PieceImageSource vectorSource) {
        this.rasterFallbacks = Map.copyOf(rasterFallbacks);
        this.vectorSource = Objects.requireNonNull(vectorSource, "vectorSource");
    }

    public BufferedImage get(
            char piece,
            int pixelWidth,
            int pixelHeight) {
        if (pixelWidth <= 0 || pixelHeight <= 0) {
            return null;
        }

        if (pixelWidth != cachedWidth || pixelHeight != cachedHeight) {
            rendered.clear();
            cachedWidth = pixelWidth;
            cachedHeight = pixelHeight;
        }

        BufferedImage cached = rendered.get(piece);
        if (cached != null) {
            return cached;
        }

        BufferedImage image;
        if (vectorSource.hasImage(piece) && !unusableVectorImages.contains(piece)) {
            try {
                image = vectorSource.render(piece, pixelWidth, pixelHeight);
            } catch (IllegalStateException error) {
                unusableVectorImages.add(piece);
                LOGGER.warning("Could not render SVG for piece '" + piece
                        + "'; using the PNG fallback: " + error.getMessage());
                image = scaleRaster(rasterFallbacks.get(piece), pixelWidth, pixelHeight);
            }
        } else {
            image = scaleRaster(rasterFallbacks.get(piece), pixelWidth, pixelHeight);
        }

        if (image != null) {
            rendered.put(piece, image);
        }
        return image;
    }

    private static BufferedImage scaleRaster(
            BufferedImage original,
            int pixelWidth,
            int pixelHeight) {
        if (original == null) {
            return null;
        }

        BufferedImage destination = new BufferedImage(
                pixelWidth,
                pixelHeight,
                BufferedImage.TYPE_INT_ARGB_PRE);

        Graphics2D graphics = destination.createGraphics();
        try {
            graphics.setComposite(AlphaComposite.Src);
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(
                    RenderingHints.KEY_ALPHA_INTERPOLATION,
                    RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            graphics.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            graphics.drawImage(
                    original,
                    0,
                    0,
                    pixelWidth,
                    pixelHeight,
                    null);
        } finally {
            graphics.dispose();
        }
        return destination;
    }
}

