package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import core.chess.Board;
import core.chess.Move;
import core.chess.Square;
import game.clock.ClockSnapshot;
import utility.Result;
import utility.Sides;
import utility.TimeControl;

/** Immutable, game-layer snapshot. It never exposes the mutable Board. */
public final class GameSnapshot {
    private final char[][] pieces;
    private final Sides sideToMove;
    private final List<Move> legalMoves;
    private final List<Move> moveHistory;
    private final String initialFen;
    private final String whiteName;
    private final String blackName;
    private final Result result;
    private final GameTermination termination;
    private final Sides winner;
    private final boolean running;
    private final boolean moveInputAllowed;
    private final boolean inCheck;
    private final ClockSnapshot clock;
    private final String errorMessage;

    GameSnapshot(
            Board board,
            List<Move> legalMoves,
            List<Move> moveHistory,
            String initialFen,
            String whiteName,
            String blackName,
            Result result,
            GameTermination termination,
            Sides winner,
            boolean running,
            boolean moveInputAllowed,
            ClockSnapshot clock,
            String errorMessage) {
        this.pieces = copyPieces(board);
        this.sideToMove = board.tomove();
        this.legalMoves = immutableMoves(legalMoves);
        this.moveHistory = immutableMoves(moveHistory);
        this.initialFen = initialFen;
        this.whiteName = whiteName;
        this.blackName = blackName;
        this.result = result;
        this.termination = termination;
        this.winner = winner;
        this.running = running;
        this.moveInputAllowed = moveInputAllowed;
        this.inCheck = board.inCheck();
        this.clock = clock;
        this.errorMessage = errorMessage;
    }

    private GameSnapshot() {
        this.pieces = new char[8][8];
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                pieces[rank][file] = ' ';
            }
        }
        this.sideToMove = Sides.WHITE;
        this.legalMoves = Collections.emptyList();
        this.moveHistory = Collections.emptyList();
        this.initialFen = GameConfiguration.STANDARD_START_FEN;
        this.whiteName = "White";
        this.blackName = "Black";
        this.result = Result.ONGOING;
        this.termination = GameTermination.NONE;
        this.winner = null;
        this.running = false;
        this.moveInputAllowed = false;
        this.inCheck = false;
        this.clock = new ClockSnapshot(
                TimeControl.NO_CONTROL, 0, 0, Sides.WHITE, false, false, false);
        this.errorMessage = null;
    }

    public static GameSnapshot empty() {
        return new GameSnapshot();
    }

    private static char[][] copyPieces(Board board) {
        char[][] copy = new char[8][8];
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
                copy[rank - 1][file - 'a'] = board.boardAt(file, rank);
            }
        }
        return copy;
    }

    private static List<Move> immutableMoves(List<Move> source) {
        List<Move> copy = new ArrayList<>(source.size());
        for (Move move : source) {
            copy.add(new Move(move));
        }
        return Collections.unmodifiableList(copy);
    }

    public char pieceAt(char file, int rank) {
        if (file < 'a' || file > 'h' || rank < 1 || rank > 8) {
            return 0;
        }
        return pieces[rank - 1][file - 'a'];
    }

    public char pieceAt(Square square) {
        return pieceAt(square.getFile(), square.getRank());
    }

    public Sides getSideToMove() { return sideToMove; }
    public List<Move> getLegalMoves() { return legalMoves; }
    public List<Move> getMoveHistory() { return moveHistory; }
    public String getInitialFen() { return initialFen; }
    public String getWhiteName() { return whiteName; }
    public String getBlackName() { return blackName; }
    public Result getResult() { return result; }
    public GameTermination getTermination() { return termination; }
    public Sides getWinner() { return winner; }
    public boolean isRunning() { return running; }
    public boolean isMoveInputAllowed() { return moveInputAllowed; }
    public boolean isInCheck() { return inCheck; }
    public ClockSnapshot getClock() { return clock; }
    public String getErrorMessage() { return errorMessage; }
}
