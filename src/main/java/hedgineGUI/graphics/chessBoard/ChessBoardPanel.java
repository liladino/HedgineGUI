package graphics.chessBoard;

import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import control.GameController;
import control.GameState;
import core.chess.Move;
import core.chess.Square;
import graphics.GraphicSettings;
import graphics.MenuManager;
import graphics.dialogs.PromotionDialog;
import utility.Sides;

/** Renders a GameState and translates pointer gestures into controller commands. */
public final class ChessBoardPanel extends JPanel {
    private static final long serialVersionUID = 987168713547L;
    private static final Logger LOGGER = Logger.getLogger(ChessBoardPanel.class.getName());

    private final transient GameController controller;
    private final transient Map<Character, BufferedImage> images = new HashMap<>();
    private final transient PieceImageCache imageCache;

    private transient GameState state;
    private transient Square selectedSquare = new Square();

    private transient BoardGeometry geometry;
    private int geometryWidth = -1;
    private int geometryHeight = -1;
    private boolean geometryRotated;

    private transient Point pressPoint;
    private transient Point dragPoint;
    private transient Square dragOrigin = new Square();
    private char draggedPiece;
    private boolean dragCandidate;
    private boolean dragging;

    public ChessBoardPanel(GameController controller, MenuManager menuManager) {
        this.controller = controller;
        this.state = controller.getState();
        loadImages();
        this.imageCache = new PieceImageCache(images);

        setPreferredSize(new Dimension(720, 720));

        controller.addStateListener(this::receiveState);
        menuManager.addAppearanceListener(this::appearanceChanged);
        installMouseInput();
    }

    private void receiveState(GameState newState) {
        Runnable update = () -> {
            state = newState;
            if (!newState.isMoveInputAllowed()) {
                selectedSquare = new Square();
                resetDragState();
            }
            repaint();
        };
        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
        } else {
            SwingUtilities.invokeLater(update);
        }
    }

    private void appearanceChanged() {
        geometryWidth = -1;
        repaint();
    }

    private void installMouseInput() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (!state.isMoveInputAllowed()) {
                    return;
                }

                Square square = geometry().squareAt(event.getPoint());
                if (square.isNull()) {
                    return;
                }

                if (!GraphicSettings.dragDrop) {
                    handleSquareClick(square);
                    return;
                }

                pressPoint = event.getPoint();
                dragPoint = event.getPoint();
                dragOrigin = new Square(square);
                draggedPiece = state.pieceAt(square);
                dragCandidate = canDrag(square);
                dragging = false;
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                if (!GraphicSettings.dragDrop || pressPoint == null || !dragCandidate) {
                    return;
                }

                Point nextPoint = event.getPoint();
                if (!dragging && pressPoint.distance(nextPoint) >= DragSource.getDragThreshold()) {
                    dragging = true;
                    selectedSquare = new Square();

                    Rectangle initialDirtyRegion = geometry().boundsOf(dragOrigin)
                            .union(spriteBounds(nextPoint));
                    dragPoint = nextPoint;
                    repaint(initialDirtyRegion);
                    return;
                }

                if (dragging) {
                    Rectangle dirtyRegion = spriteBounds(dragPoint)
                            .union(spriteBounds(nextPoint));
                    dragPoint = nextPoint;
                    repaint(dirtyRegion);
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (pressPoint == null) {
                    return;
                }

                if (!GraphicSettings.dragDrop) {
                    resetDragState();
                    repaint();
                    return;
                }

                Square origin = new Square(dragOrigin);
                boolean completedDrag = dragging;
                Rectangle dirtyRegion = completedDrag
                        ? geometry().boundsOf(origin).union(spriteBounds(dragPoint))
                        : null;

                resetDragState();

                if (completedDrag) {
                    selectedSquare = new Square();
                    Square destination = geometry().squareAt(event.getPoint());
                    repaint(dirtyRegion);
                    if (!destination.isNull()) {
                        trySubmitMove(origin, destination);
                    }
                } else {
                    handleSquareClick(origin);
                }
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    private boolean canDrag(Square square) {
        char piece = state.pieceAt(square);
        return piece != ' '
                && pieceColor(piece) == state.getSideToMove()
                && state.hasLegalMoveFrom(square);
    }

    private void resetDragState() {
        pressPoint = null;
        dragPoint = null;
        dragOrigin = new Square();
        draggedPiece = ' ';
        dragCandidate = false;
        dragging = false;
    }

    private Rectangle spriteBounds(Point center) {
        int size = geometry().squareSize();
        Rectangle bounds = new Rectangle(
                center.x - size / 2,
                center.y - size / 2,
                size,
                size);
        bounds.grow(2, 2);
        return bounds;
    }

    private void handleSquareClick(Square square) {
        if (!state.isMoveInputAllowed() || square.isNull()) {
            return;
        }

        if (selectedSquare.isNull()) {
            selectIfMovable(square);
            return;
        }

        Square from = new Square(selectedSquare);
        char targetPiece = state.pieceAt(square);
        if (targetPiece != ' ' && pieceColor(targetPiece) == state.getSideToMove()) {
            if (from.equals(square)) {
                selectedSquare = new Square();
            } else {
                selectIfMovable(square);
            }
            repaint();
            return;
        }

        selectedSquare = new Square();
        trySubmitMove(from, square);
        repaint();
    }

    private void selectIfMovable(Square square) {
        char piece = state.pieceAt(square);
        if (piece != ' '
                && pieceColor(piece) == state.getSideToMove()
                && state.hasLegalMoveFrom(square)) {
            selectedSquare = new Square(square);
        } else {
            selectedSquare = new Square();
        }
        repaint();
    }

    private void trySubmitMove(Square from, Square to) {
        Move move = createMoveWithPromotion(from, to);
        if (move != null && state.isLegalMove(move)) {
            controller.submitMove(move);
        }
    }

    private Move createMoveWithPromotion(Square from, Square to) {
        char piece = state.pieceAt(from);
        if (Character.toLowerCase(piece) != 'p'
                || (to.getRank() != 1 && to.getRank() != 8)) {
            return new Move(from, to, ' ');
        }

        boolean promotionIsLegal = false;
        for (char promotion : new char[] {'q', 'r', 'n', 'b'}) {
            if (state.isLegalMove(new Move(from, to, promotion))) {
                promotionIsLegal = true;
                break;
            }
        }
        if (!promotionIsLegal) {
            return null;
        }

        PromotionDialog dialog = new PromotionDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                new HashMap<>(images),
                state.getSideToMove());
        dialog.setVisible(true);
        return new Move(from, to, dialog.getSelectedPiece());
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        BoardGeometry currentGeometry = geometry();
        drawBoard(graphics, currentGeometry, dragging ? dragOrigin : null);

        if (dragging && dragPoint != null) {
            int size = currentGeometry.squareSize();
            drawImage(
                    graphics,
                    draggedPiece,
                    dragPoint.x - size / 2,
                    dragPoint.y - size / 2,
                    size);
        }
    }

    private BoardGeometry geometry() {
        boolean rotated = GraphicSettings.rotateBoard;
        if (geometry == null
                || geometryWidth != getWidth()
                || geometryHeight != getHeight()
                || geometryRotated != rotated) {
            geometry = new BoardGeometry(getSize(), rotated);
            geometryWidth = getWidth();
            geometryHeight = getHeight();
            geometryRotated = rotated;
        }
        return geometry;
    }

    private void drawBoard(Graphics graphics, BoardGeometry currentGeometry, Square omittedSquare) {
        Move lastMove = state.getLastMove();
        int size = currentGeometry.squareSize();

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                Square square = new Square(file, rank);
                Rectangle bounds = currentGeometry.boundsOf(square);

                graphics.setColor(getSquareColor((file - 'a' + rank) % 2 == 0 ? Sides.WHITE : Sides.BLACK));
                graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

                if (lastMove != null && (square.equals(lastMove.getFrom())
                        || square.equals(lastMove.getTo()))) {
                    drawImage(graphics, 'L', bounds.x, bounds.y, size);
                }

                char piece = state.pieceAt(square);
                boolean checkedKing = state.isInCheck()
                        && ((piece == 'K' && state.getSideToMove() == Sides.WHITE)
                                || (piece == 'k' && state.getSideToMove() == Sides.BLACK));
                if (checkedKing) {
                    drawImage(graphics, 'C', bounds.x, bounds.y, size);
                }
                if (square.equals(selectedSquare)) {
                    drawImage(graphics, 'S', bounds.x, bounds.y, size);
                }

                if (piece != ' ' && (!square.equals(omittedSquare))) {
                    drawImage(graphics, piece, bounds.x, bounds.y, size);
                }
            }
        }
    }

    private void drawImage(Graphics graphics, char key, int x, int y, int size) {
        BufferedImage image = imageCache.get(key, size);
        if (image != null) {
            graphics.drawImage(image, x, y, this);
            return;
        }

        if (Character.isLetter(key)) {
            graphics.setColor(Color.RED);
            graphics.setFont(new Font("TimesRoman", Font.PLAIN, Math.max(12, size / 2)));
            graphics.drawString(Character.toString(key), x + size / 2, y + size / 2);
        }
    }

    private Color getSquareColor(Sides side) {
        if (GraphicSettings.colors.get(GraphicSettings.selectedScheme) == null) {
            return side == Sides.WHITE ? Color.WHITE : Color.GRAY;
        }
        return side == Sides.WHITE
                ? GraphicSettings.colors.get(GraphicSettings.selectedScheme).first
                : GraphicSettings.colors.get(GraphicSettings.selectedScheme).second;
    }

    private Sides pieceColor(char piece) {
        return Character.isUpperCase(piece) ? Sides.WHITE : Sides.BLACK;
    }

    private void loadImages() {
        for (char piece : "PRBNQKprbnqk".toCharArray()) {
            String color = Character.isUpperCase(piece) ? "w" : "b";
            loadImage(piece, "/resources/pieces/" + color
                    + Character.toLowerCase(piece) + ".png");
        }
        loadImage('S', "/resources/select/blue.png");
        loadImage('L', "/resources/select/lastmove.png");
        loadImage('C', "/resources/select/magenta.png");
    }

    private void loadImage(char key, String path) {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Resource not found: " + path);
            }
            images.put(key, ImageIO.read(stream));
        } catch (IOException error) {
            LOGGER.warning("Could not load " + path + ": " + error.getMessage());
        }
    }
}
