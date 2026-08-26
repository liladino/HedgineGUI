package graphics.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
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
    private transient GameState state;
    private transient Square selected = new Square();

    private int boardSize;
    private int squareSize;
    private boolean dragged;
    private int dragX;
    private int dragY;
    private char draggedPiece;

    public ChessBoardPanel(GameController controller, MenuManager menuManager) {
        this.controller = controller;
        this.state = controller.getState();
        loadImages();
        setPreferredSize(new Dimension(720, 720));

        controller.addStateListener(this::receiveState);
        menuManager.addAppearanceListener(this::repaint);
        installMouseInput();
    }

    private void receiveState(GameState newState) {
        Runnable update = () -> {
            state = newState;
            if (!newState.isMoveInputAllowed()) {
                selected = new Square();
                dragged = false;
            }
            repaint();
        };
        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
        } else {
            SwingUtilities.invokeLater(update);
        }
    }

    private void installMouseInput() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (!state.isMoveInputAllowed()) {
                    return;
                }
                Square square = getSquare(event.getX(), event.getY());
                if (square.isNull()) {
                    return;
                }

                if (!GraphicSettings.dragDrop) {
                    handleSquareClick(square);
                    return;
                }

                char piece = state.pieceAt(square);
                if (piece == ' ' || pieceColor(piece) != state.getSideToMove()
                        || !state.hasLegalMoveFrom(square)) {
                    handleSquareClick(square);
                    return;
                }

                selected = square;
                draggedPiece = piece;
                dragged = true;
                dragX = event.getX();
                dragY = event.getY();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (!GraphicSettings.dragDrop || !dragged) {
                    return;
                }
                dragged = false;
                Square destination = getSquare(event.getX(), event.getY());
                if (!destination.isNull()) {
                    handleSquareClick(destination);
                } else {
                    selected = new Square();
                    repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                if (!GraphicSettings.dragDrop || !dragged) {
                    return;
                }
                dragX = event.getX();
                dragY = event.getY();
                repaint();
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    private Square getSquare(int x, int y) {
        updateDimensions();
        int boardLeft = (getWidth() - boardSize) / 2;
        int boardTop = (getHeight() - boardSize) / 2;
        if (x < boardLeft || x >= boardLeft + boardSize
                || y < boardTop || y >= boardTop + boardSize) {
            return new Square();
        }

        int displayFile = (x - boardLeft) / squareSize;
        int displayRank = (y - boardTop) / squareSize;
        char file;
        int rank;
        if (GraphicSettings.rotateBoard) {
            file = (char) ('h' - displayFile);
            rank = displayRank + 1;
        } else {
            file = (char) ('a' + displayFile);
            rank = 8 - displayRank;
        }
        return new Square(file, rank);
    }

    private void handleSquareClick(Square square) {
        if (!state.isMoveInputAllowed()) {
            return;
        }

        if (selected.isNull()) {
            selectIfMovable(square);
            return;
        }

        Square from = new Square(selected);
        char targetPiece = state.pieceAt(square);
        if (targetPiece != ' ' && pieceColor(targetPiece) == state.getSideToMove()) {
            if (from.equals(square)) {
                selected = new Square();
            } else {
                selectIfMovable(square);
            }
            repaint();
            return;
        }

        Move move = createMoveWithPromotion(from, square);
        selected = new Square();
        dragged = false;
        if (move != null && state.isLegalMove(move)) {
            controller.submitMove(move);
        }
        repaint();
    }

    private void selectIfMovable(Square square) {
        char piece = state.pieceAt(square);
        if (piece != ' ' && pieceColor(piece) == state.getSideToMove()
                && state.hasLegalMoveFrom(square)) {
            selected = new Square(square);
        } else {
            selected = new Square();
        }
        repaint();
    }

    private Move createMoveWithPromotion(Square from, Square to) {
        char piece = state.pieceAt(from);
        if (Character.toLowerCase(piece) != 'p' || (to.getRank() != 1 && to.getRank() != 8)) {
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
        updateDimensions();
        drawBoard(graphics, dragged ? selected : null);
        if (dragged) {
            drawPiece(
                    graphics,
                    dragX - squareSize / 2,
                    dragY - squareSize / 2,
                    draggedPiece);
        }
    }

    private void updateDimensions() {
        boardSize = Math.min(getWidth(), getHeight());
        squareSize = Math.max(1, boardSize / 8);
        boardSize = squareSize * 8;
    }

    private void drawBoard(Graphics graphics, Square omittedSquare) {
        int boardLeft = (getWidth() - boardSize) / 2;
        int boardTop = (getHeight() - boardSize) / 2;
        Move lastMove = state.getLastMove();

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                int x = GraphicSettings.rotateBoard
                        ? boardLeft + ('h' - file) * squareSize
                        : boardLeft + (file - 'a') * squareSize;
                int y = GraphicSettings.rotateBoard
                        ? boardTop + (rank - 1) * squareSize
                        : boardTop + (8 - rank) * squareSize;

                graphics.setColor((file - 'a' + rank) % 2 == 0
                        ? getSquareColor(Sides.WHITE)
                        : getSquareColor(Sides.BLACK));
                graphics.fillRect(x, y, squareSize, squareSize);

                Square square = new Square(file, rank);
                if (lastMove != null && (square.equals(lastMove.getFrom())
                        || square.equals(lastMove.getTo()))) {
                    drawOverlay(graphics, 'L', x, y);
                }

                char piece = state.pieceAt(square);
                boolean checkedKing = state.isInCheck()
                        && ((piece == 'K' && state.getSideToMove() == Sides.WHITE)
                                || (piece == 'k' && state.getSideToMove() == Sides.BLACK));
                if (checkedKing) {
                    drawOverlay(graphics, 'C', x, y);
                }
                if (square.equals(selected)) {
                    drawOverlay(graphics, 'S', x, y);
                }

                if (piece != ' ' && (omittedSquare == null || !square.equals(omittedSquare))) {
                    drawPiece(graphics, x, y, piece);
                }
            }
        }
    }

    private void drawOverlay(Graphics graphics, char key, int x, int y) {
        BufferedImage image = images.get(key);
        if (image != null) {
            graphics.drawImage(image, x, y, squareSize, squareSize, this);
        }
    }

    private void drawPiece(Graphics graphics, int x, int y, char piece) {
        BufferedImage image = images.get(piece);
        if (image != null) {
            graphics.drawImage(
                    image.getScaledInstance(squareSize, squareSize, Image.SCALE_FAST),
                    x, y, squareSize, squareSize, this);
            return;
        }

        graphics.setColor(Color.RED);
        graphics.setFont(new Font("TimesRoman", Font.PLAIN, 30));
        graphics.drawString(Character.toString(piece), x + squareSize / 2, y + squareSize / 2);
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
