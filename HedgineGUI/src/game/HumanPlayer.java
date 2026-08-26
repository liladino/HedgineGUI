package game;

import core.chess.Move;
import utility.Sides;

/** A player whose move is supplied by an external adapter such as a GUI or CLI. */
public final class HumanPlayer extends Player {
    public HumanPlayer(Sides side, String name) {
        super(side, name);
    }

    @Override
    protected void onMoveRequested(Position position) {
        // The controller exposes the pending position to the active input view.
    }

    @Override
    public boolean submitMove(Move move) {
        return returnMove(move);
    }

    @Override
    public boolean acceptsExternalMoves() {
        return true;
    }

    /** Temporary migration alias for code that still calls makeMove. */
    @Deprecated
    public boolean makeMove(Move move) {
        return submitMove(move);
    }
}
