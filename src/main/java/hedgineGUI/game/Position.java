package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import core.chess.Move;
import game.clock.ClockSnapshot;
import utility.Sides;

/** Immutable information a player may use to choose its next move. */
public final class Position {
    private final String initialFen;
    private final String fen;
    private final Sides sideToMove;
    private final List<Move> moveHistory;
    private final ClockSnapshot clock;

    public Position(
            String initialFen,
            String fen,
            Sides sideToMove,
            List<Move> moveHistory,
            ClockSnapshot clock) {
        this.initialFen = Objects.requireNonNull(initialFen, "initialFen");
        this.fen = Objects.requireNonNull(fen, "fen");
        this.sideToMove = Objects.requireNonNull(sideToMove, "sideToMove");
        this.moveHistory = immutableMoves(moveHistory);
        this.clock = clock;
    }

    private static List<Move> immutableMoves(List<Move> moves) {
        Objects.requireNonNull(moves, "moveHistory");
        List<Move> copy = new ArrayList<>(moves.size());
        for (Move move : moves) {
            copy.add(new Move(move));
        }
        return Collections.unmodifiableList(copy);
    }

    public String getInitialFen() {
        return initialFen;
    }

    public String getFen() {
        return fen;
    }

    public Sides getSideToMove() {
        return sideToMove;
    }

    public List<Move> getMoveHistory() {
        return moveHistory;
    }

    public ClockSnapshot getClock() {
        return clock;
    }
}
