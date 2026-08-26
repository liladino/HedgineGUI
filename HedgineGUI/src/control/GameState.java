package control;

import java.util.List;
import java.util.Objects;

import core.chess.Move;
import core.chess.Square;
import core.chess.IO.PGNConverter;
import game.GameSnapshot;
import game.GameTermination;
import game.clock.ClockSnapshot;
import utility.Result;
import utility.Sides;

/** Read-only view model consumed by Swing (or any future UI). */
public final class GameState {
    private final GameSnapshot snapshot;
    private final String moveText;

    public GameState(GameSnapshot snapshot) {
        this.snapshot = Objects.requireNonNull(snapshot, "snapshot");
        this.moveText = PGNConverter.convertToMoves(
                snapshot.getInitialFen(), snapshot.getMoveHistory());
    }

    public static GameState empty() {
        return new GameState(GameSnapshot.empty());
    }

    public char pieceAt(char file, int rank) {
        return snapshot.pieceAt(file, rank);
    }

    public char pieceAt(Square square) {
        return snapshot.pieceAt(square);
    }

    public boolean isLegalMove(Move move) {
        return snapshot.getLegalMoves().contains(move);
    }

    public boolean hasLegalMoveFrom(Square from) {
        for (Move move : snapshot.getLegalMoves()) {
            if (move.getFrom().equals(from)) {
                return true;
            }
        }
        return false;
    }

    public Move getLastMove() {
        List<Move> moves = snapshot.getMoveHistory();
        return moves.isEmpty() ? null : new Move(moves.get(moves.size() - 1));
    }

    public Sides getSideToMove() { return snapshot.getSideToMove(); }
    public List<Move> getLegalMoves() { return snapshot.getLegalMoves(); }
    public List<Move> getMoveHistory() { return snapshot.getMoveHistory(); }
    public String getWhiteName() { return snapshot.getWhiteName(); }
    public String getBlackName() { return snapshot.getBlackName(); }
    public Result getResult() { return snapshot.getResult(); }
    public GameTermination getTermination() { return snapshot.getTermination(); }
    public Sides getWinner() { return snapshot.getWinner(); }
    public boolean isRunning() { return snapshot.isRunning(); }
    public boolean isMoveInputAllowed() { return snapshot.isMoveInputAllowed(); }
    public boolean isInCheck() { return snapshot.isInCheck(); }
    public ClockSnapshot getClock() { return snapshot.getClock(); }
    public String getMoveText() { return moveText; }
    public String getErrorMessage() { return snapshot.getErrorMessage(); }
}
