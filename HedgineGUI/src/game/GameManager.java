package game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import core.chess.Board;
import core.chess.Move;
import core.chess.IO.FENException;
import game.clock.ClockController;
import game.clock.ClockListener;
import game.clock.ClockSnapshot;
import game.clock.TimeInputException;
import utility.Result;
import utility.Sides;

/**
 * Event-driven game loop. It coordinates Player objects without knowing how
 * either player obtains a move and has no dependency on Swing or the control UI.
 */
public final class GameManager implements MoveReceiver, ClockListener {
    private static final Logger LOGGER = Logger.getLogger(GameManager.class.getName());

    @FunctionalInterface
    public interface StateListener {
        void onStateChanged(GameSnapshot snapshot);
    }

    private final Object stateLock = new Object();
    private final List<StateListener> listeners = new CopyOnWriteArrayList<>();

    private Player white;
    private Player black;
    private Player currentPlayer;
    private Board board;
    private String initialFen;
    private final List<Move> moves = new ArrayList<>();
    private final List<ClockSnapshot> clockHistory = new ArrayList<>();
    private ClockController clockController;
    private ClockSnapshot clockSnapshot;
    private Result result = Result.ONGOING;
    private GameTermination termination = GameTermination.NONE;
    private Sides winner;
    private boolean running;
    private boolean awaitingMove;
    private String errorMessage;

    public void addStateListener(StateListener listener) {
        listeners.add(listener);
    }

    public void removeStateListener(StateListener listener) {
        listeners.remove(listener);
    }

    public void startGame(GameConfiguration configuration) throws GameException {
        if (isGameRunning()) {
            stopGame();
        }

        final Board newBoard;
        final ClockController newClock;
        try {
            newBoard = configuration.createBoard();
            newClock = configuration.createClockController();
        } catch (FENException | TimeInputException error) {
            throw new GameException("Invalid game configuration: " + error.getMessage(), error);
        }

        Player newWhite = configuration.getWhite();
        Player newBlack = configuration.getBlack();
        try {
            newWhite.startGame();
            newBlack.startGame();
        } catch (IOException error) {
            newWhite.endGame();
            newBlack.endGame();
            throw new GameException("Could not start player: " + error.getMessage(), error);
        }

        newClock.subscribe(this);
        newClock.setActiveSide(newBoard.tomove());
        newClock.setPlyCount(pliesBeforePosition(newBoard));
        newClock.startClock();

        synchronized (stateLock) {
            white = newWhite;
            black = newBlack;
            board = newBoard;
            initialFen = newBoard.convertToFEN();
            moves.clear();
            clockHistory.clear();
            clockController = newClock;
            clockSnapshot = newClock.snapshot();
            currentPlayer = playerFor(newBoard.tomove());
            result = Result.ONGOING;
            termination = GameTermination.NONE;
            winner = null;
            errorMessage = null;
            running = true;
            awaitingMove = false;
        }

        publishCurrentState();
        requestCurrentMove();
    }

    private int pliesBeforePosition(Board position) {
        return Math.max(0, (position.getFullMoveCount() - 1) * 2
                + (position.tomove() == Sides.BLACK ? 1 : 0));
    }

    private void requestCurrentMove() {
        final Player player;
        final Position position;
        synchronized (stateLock) {
            if (!running || board == null) {
                return;
            }
            player = currentPlayer;
            awaitingMove = true;
            position = new Position(
                    initialFen,
                    board.convertToFEN(),
                    board.tomove(),
                    moves,
                    clockSnapshot);
        }

        try {
            player.requestMove(position, this);
        } catch (RuntimeException error) {
            onMoveRequestFailed(player, error);
            return;
        }
        publishCurrentState();
    }

    /** Routes external input through the active Player's polymorphic API. */
    public boolean submitMove(Move move) {
        Player player;
        synchronized (stateLock) {
            if (!running || !awaitingMove || currentPlayer == null) {
                return false;
            }
            player = currentPlayer;
        }
        return player.submitMove(move);
    }

    @Override
    public void onMoveReceived(Player player, Move move) {
        boolean continueGame;
        boolean gameEnded;

        synchronized (stateLock) {
            if (!running || !awaitingMove || player != currentPlayer) {
                return;
            }
            awaitingMove = false;

            if (move.isNull() || !board.isMoveLegal(move)) {
                errorMessage = "Illegal move submitted by " + player.getName() + ": " + move;
                continueGame = true;
                gameEnded = false;
            } else {
                clockHistory.add(clockController.snapshot());
                board.makeMove(new Move(move));
                moves.add(new Move(move));
                clockController.pressClock();
                clockSnapshot = clockController.snapshot();
                result = board.getResult();
                errorMessage = null;
                currentPlayer = playerFor(board.tomove());

                gameEnded = result != Result.ONGOING;
                continueGame = !gameEnded;
                if (gameEnded) {
                    running = false;
                    setBoardTermination();
                }
            }
        }

        if (gameEnded) {
            stopResources();
            publishCurrentState();
        } else {
            publishCurrentState();
            if (continueGame) {
                requestCurrentMove();
            }
        }
    }

    @Override
    public void onMoveRequestFailed(Player player, Exception error) {
        synchronized (stateLock) {
            if (!running || player != currentPlayer) {
                return;
            }
            awaitingMove = false;
            running = false;
            termination = GameTermination.ERROR;
            errorMessage = error.getMessage();
        }
        stopResources();
        publishCurrentState();
    }

    public void resign() {
        synchronized (stateLock) {
            if (!running || currentPlayer == null) {
                return;
            }
            winner = opposite(currentPlayer.getSide());
            result = winner == Sides.WHITE ? Result.WHITE_WON : Result.BLACK_WON;
            termination = GameTermination.RESIGNATION;
            running = false;
            awaitingMove = false;
        }
        stopResources();
        publishCurrentState();
    }

    /** Takes back one ply. Policies such as two-ply takeback belong in Control. */
    public boolean takeBack() {
        final Player playerToCancel;
        final ClockSnapshot clockToRestore;
        synchronized (stateLock) {
            if (!running || moves.isEmpty()) {
                return false;
            }
            playerToCancel = currentPlayer;
            awaitingMove = false;

            moves.remove(moves.size() - 1);
            try {
                board = new Board(initialFen);
            } catch (FENException impossible) {
                throw new IllegalStateException("Stored initial FEN became invalid", impossible);
            }
            for (Move move : moves) {
                board.makeMove(move);
            }

            clockToRestore = clockHistory.remove(clockHistory.size() - 1);
            currentPlayer = playerFor(board.tomove());
            result = Result.ONGOING;
            termination = GameTermination.NONE;
            winner = null;
            errorMessage = null;
        }

        playerToCancel.cancelMoveRequest();
        clockController.restore(clockToRestore);
        synchronized (stateLock) {
            clockSnapshot = clockController.snapshot();
        }
        publishCurrentState();
        requestCurrentMove();
        return true;
    }

    public void stopGame() {
        synchronized (stateLock) {
            if (board == null) {
                return;
            }
            running = false;
            awaitingMove = false;
            if (termination == GameTermination.NONE) {
                termination = GameTermination.ABORTED;
            }
        }
        stopResources();
        publishCurrentState();
    }

    private void stopResources() {
        Player whiteToStop;
        Player blackToStop;
        ClockController clockToStop;
        synchronized (stateLock) {
            whiteToStop = white;
            blackToStop = black;
            clockToStop = clockController;
        }

        if (whiteToStop != null) {
            whiteToStop.endGame();
        }
        if (blackToStop != null) {
            blackToStop.endGame();
        }
        if (clockToStop != null) {
            clockToStop.pauseClock();
            synchronized (stateLock) {
                clockSnapshot = clockToStop.snapshot();
            }
        }
    }

    private void setBoardTermination() {
        if (result == Result.WHITE_WON) {
            winner = Sides.WHITE;
            termination = GameTermination.CHECKMATE;
        } else if (result == Result.BLACK_WON) {
            winner = Sides.BLACK;
            termination = GameTermination.CHECKMATE;
        } else if (result == Result.STALEMATE) {
            winner = null;
            termination = GameTermination.STALEMATE;
        } else {
            winner = null;
            termination = GameTermination.DRAW;
        }
    }

    @Override
    public void onTick(ClockSnapshot snapshot) {
        synchronized (stateLock) {
            if (!running) {
                return;
            }
            clockSnapshot = snapshot;
        }
        publishCurrentState();
    }

    @Override
    public void onTimeUp(ClockSnapshot snapshot) {
        synchronized (stateLock) {
            if (!running) {
                return;
            }
            clockSnapshot = snapshot;
            awaitingMove = false;
            running = false;
            termination = GameTermination.TIMEOUT;

            Sides flagged = snapshot.getFlaggedSide();
            Sides potentialWinner = opposite(flagged);
            if (board.sufficientMaterial(potentialWinner)) {
                winner = potentialWinner;
                result = winner == Sides.WHITE ? Result.WHITE_WON : Result.BLACK_WON;
            } else {
                winner = null;
                result = Result.DRAW;
            }
        }
        stopResources();
        publishCurrentState();
    }

    private Sides opposite(Sides side) {
        return side == Sides.WHITE ? Sides.BLACK : Sides.WHITE;
    }

    private Player playerFor(Sides side) {
        return side == Sides.WHITE ? white : black;
    }

    public GameSnapshot getSnapshot() {
        synchronized (stateLock) {
            return createSnapshot();
        }
    }

    private GameSnapshot createSnapshot() {
        if (board == null) {
            return GameSnapshot.empty();
        }

        List<Move> legalMoves = new ArrayList<>();
        int legalMoveCount = board.numberOfLegalMoves();
        for (int index = 0; index < legalMoveCount; index++) {
            legalMoves.add(new Move(board.getLegalMove(index)));
        }

        boolean inputAllowed = running
                && awaitingMove
                && currentPlayer != null
                && currentPlayer.acceptsExternalMoves();
        return new GameSnapshot(
                board,
                legalMoves,
                moves,
                initialFen,
                white.getName(),
                black.getName(),
                result,
                termination,
                winner,
                running,
                inputAllowed,
                clockSnapshot,
                errorMessage);
    }

    private void publishCurrentState() {
        GameSnapshot snapshot = getSnapshot();
        for (StateListener listener : listeners) {
            try {
                listener.onStateChanged(snapshot);
            } catch (RuntimeException error) {
                LOGGER.warning("State listener failed: " + error.getMessage());
            }
        }
    }

    public List<Move> getMoves() {
        synchronized (stateLock) {
            List<Move> copy = new ArrayList<>(moves.size());
            for (Move move : moves) {
                copy.add(new Move(move));
            }
            return Collections.unmodifiableList(copy);
        }
    }

    public String getStartFEN() {
        synchronized (stateLock) {
            return initialFen;
        }
    }

    public Result getResult() {
        synchronized (stateLock) {
            return result;
        }
    }

    public boolean isGameRunning() {
        synchronized (stateLock) {
            return running;
        }
    }

    public Board getBoard(){
        Board newBoard = board;
        return newBoard;
    }
}
