package hedgineGUI.graphics.chessBoard;

import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Point;
import hedgineGUI.core.chess.Square;

public final class BoardGeometry {
    private final Rectangle bounds;
    private final int squareSize;
    private final boolean rotated;

    public BoardGeometry(Dimension panelSize, boolean rotated) {
        int available = Math.min(panelSize.width, panelSize.height);
        squareSize = available / 8;
        int boardSize = squareSize * 8;

        bounds = new Rectangle(
                (panelSize.width - boardSize) / 2,
                (panelSize.height - boardSize) / 2,
                boardSize,
                boardSize);

        this.rotated = rotated;
    }

    public Square squareAt(Point point) {
        if (squareSize == 0 || !bounds.contains(point)) {
            return new Square();
        }

        int viewColumn = (point.x - bounds.x) / squareSize;
        int viewRow = (point.y - bounds.y) / squareSize;

        int fileIndex = rotated ? 7 - viewColumn : viewColumn;
        int rankIndex = rotated ? viewRow : 7 - viewRow;

        return new Square((char) ('a' + fileIndex), rankIndex + 1);
    }

    public Rectangle boundsOf(Square square) {
        int fileIndex = square.getFile() - 'a';
        int rankIndex = square.getRank() - 1;

        int viewColumn = rotated ? 7 - fileIndex : fileIndex;
        int viewRow = rotated ? rankIndex : 7 - rankIndex;

        return new Rectangle(
                bounds.x + viewColumn * squareSize,
                bounds.y + viewRow * squareSize,
                squareSize,
                squareSize);
    }

    public int squareSize() {
        return squareSize;
    }

    public Rectangle bounds() {
        return new Rectangle(bounds);
    }
}