package hedgineGUI.graphics.chessBoard;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

/** Rasterizes SVG piece resources at the exact pixel size requested by the UI. */
public final class SvgPieceImageSource implements PieceImageSource {
    private static final String PIECES = "PRBNQKprbnqk";

    private final Map<Character, URL> resources;

    public SvgPieceImageSource() {
        this("/pieces");
    }

    /**
     * Creates a source backed by SVG files in {@code resourceDirectory}.
     * Files are expected to be named wp.svg, bk.svg, and so on.
     */
    public SvgPieceImageSource(String resourceDirectory) {
        if (resourceDirectory == null || resourceDirectory.isBlank()) {
            throw new IllegalArgumentException("Resource directory must not be blank");
        }

        String normalizedDirectory = resourceDirectory.startsWith("/")
                ? resourceDirectory
                : "/" + resourceDirectory;
        if (normalizedDirectory.endsWith("/")) {
            normalizedDirectory = normalizedDirectory.substring(
                    0, normalizedDirectory.length() - 1);
        }

        Map<Character, URL> foundResources = new HashMap<>();
        for (char piece : PIECES.toCharArray()) {
            URL resource = SvgPieceImageSource.class.getResource(
                    normalizedDirectory + "/" + fileName(piece));
            if (resource != null) {
                foundResources.put(piece, resource);
            }
        }
        resources = Map.copyOf(foundResources);
    }

    @Override
    public boolean hasImage(char piece) {
        return resources.containsKey(piece);
    }

    @Override
    public BufferedImage render(char piece, int pixelWidth, int pixelHeight) {
        if (pixelWidth <= 0 || pixelHeight <= 0) {
            throw new IllegalArgumentException("Image dimensions must be positive");
        }

        URL resource = resources.get(piece);
        if (resource == null) {
            throw new IllegalArgumentException(
                    "No SVG resource exists for piece '" + piece + "'");
        }

        BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
        transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, (float) pixelWidth);
        transcoder.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, (float) pixelHeight);

        try {
            // Supplying the URL rather than only an InputStream gives Batik a base URI.
            transcoder.transcode(new TranscoderInput(resource.toExternalForm()), null);
        } catch (TranscoderException error) {
            throw new IllegalStateException(
                    "Could not render SVG resource " + resource, error);
        }

        BufferedImage image = transcoder.image();
        if (image == null) {
            throw new IllegalStateException(
                    "SVG renderer produced no image for " + resource);
        }
        return image;
    }

    private static String fileName(char piece) {
        if (PIECES.indexOf(piece) < 0) {
            throw new IllegalArgumentException("Unsupported chess piece: " + piece);
        }
        String color = Character.isUpperCase(piece) ? "w" : "b";
        return color + Character.toLowerCase(piece) + ".svg";
    }

    private static final class BufferedImageTranscoder extends ImageTranscoder {
        private BufferedImage image;

        @Override
        public BufferedImage createImage(int width, int height) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        }

        @Override
        public void writeImage(BufferedImage renderedImage, TranscoderOutput output) {
            image = renderedImage;
        }

        BufferedImage image() {
            return image;
        }
    }
}
