package graphics.chessBoard;

import java.awt.image.BufferedImage;


public interface PieceImageSource {
    BufferedImage render(char piece, int pixelWidth, int pixelHeight);
}