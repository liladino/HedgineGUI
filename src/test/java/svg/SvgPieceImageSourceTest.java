package svg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import hedgineGUI.graphics.chessBoard.SvgPieceImageSource;

class SvgPieceImageSourceTest {
    @Test
    void rendersAvailableSvgAtRequestedPixelSize() {
        SvgPieceImageSource source = new SvgPieceImageSource();

        assertTrue(source.hasImage('K'));
        assertTrue(source.hasImage('Q'));

        BufferedImage image = source.render('K', 137, 91);

        assertEquals(137, image.getWidth());
        assertEquals(91, image.getHeight());
        assertTrue(image.getColorModel().hasAlpha());
    }

    @Test
    void rejectsMissingPiecesAndInvalidDimensions() {
        SvgPieceImageSource source = new SvgPieceImageSource("/test-pieces");

        assertThrows(IllegalArgumentException.class, () -> source.render('Q', 80, 80));
        assertThrows(IllegalArgumentException.class, () -> source.render('K', 0, 80));
    }
}
