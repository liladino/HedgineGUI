package hedgineGUI.graphics.chessBoard;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Supplies board images at an exact pixel size. SVGs are preferred; raster (PNG)
 * images are used for keys without an SVG, or when an SVG fails to render.
 * Raster originals are loaded lazily, at most once per key, and kept in memory.
 */
public final class PieceImageCache {
    private static final Logger LOGGER = Logger.getLogger(PieceImageCache.class.getName());

    private final Function<Character, BufferedImage> rasterLoader;
    private final PieceImageSource vectorSource;
    private final Map<Character, Optional<BufferedImage>> rasterOriginals = new HashMap<>();
    private final Map<Character, BufferedImage> rendered = new HashMap<>();
    private final Set<Character> unusableVectorImages = new HashSet<>();
    private int cachedWidth = -1;
    private int cachedHeight = -1;

    /**
     * @param rasterLoader loads the original raster image for a key, or returns null
     *                     if none exists; called at most once per key
     * @param vectorSource preferred source for keys it has an image for
     */
    public PieceImageCache(
            Function<Character, BufferedImage> rasterLoader,
            PieceImageSource vectorSource) {
        this.rasterLoader = Objects.requireNonNull(rasterLoader, "rasterLoader");
        this.vectorSource = Objects.requireNonNull(vectorSource, "vectorSource");
    }

    /**
     * Returns the image for {@code piece} at the given size, reusing earlier results
     * while the size stays the same. Intended for the board's square size.
     */
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

        BufferedImage image = render(piece, pixelWidth, pixelHeight);
        if (image != null) {
            rendered.put(piece, image);
        }
        return image;
    }

    /**
     * Renders {@code piece} at the given size without touching the sized cache, so
     * one-off sizes (e.g. dialog icons) do not evict the board's images.
     */
    public BufferedImage render(
            char piece,
            int pixelWidth,
            int pixelHeight) {
        if (pixelWidth <= 0 || pixelHeight <= 0) {
            return null;
        }

        if (vectorSource.hasImage(piece) && !unusableVectorImages.contains(piece)) {
            try {
                return vectorSource.render(piece, pixelWidth, pixelHeight);
            } catch (IllegalStateException error) {
                unusableVectorImages.add(piece);
                LOGGER.warning("Could not render SVG for piece '" + piece
                        + "'; using the PNG fallback: " + error.getMessage());
            }
        }
        return scaleRaster(rasterOriginal(piece), pixelWidth, pixelHeight);
    }

    private BufferedImage rasterOriginal(char piece) {
        return rasterOriginals
                .computeIfAbsent(piece, key -> Optional.ofNullable(rasterLoader.apply(key)))
                .orElse(null);
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
