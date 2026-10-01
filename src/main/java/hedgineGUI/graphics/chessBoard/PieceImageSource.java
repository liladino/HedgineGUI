package hedgineGUI.graphics.chessBoard;

import java.awt.image.BufferedImage;

public interface PieceImageSource {
    boolean hasImage(char piece);

    BufferedImage render(char piece, int pixelWidth, int pixelHeight);
}