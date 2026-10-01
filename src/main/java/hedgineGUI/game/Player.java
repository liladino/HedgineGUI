package game;

import java.io.IOException;
import java.util.Objects;

import core.chess.Move;
import utility.Sides;

/**
 * A participant in a game.
 *
 * <p>A move request is asynchronous and one-shot. The game asks a player for a
 * move, and the player eventually returns exactly one move to the supplied
 * receiver. Human and engine players differ only in how they obtain that move.</p>
 */
public abstract class Player {
    private final String name;
    private final Sides side;

    private MoveReceiver moveReceiver;
    private Position requestedPosition;

    protected Player(Sides side, String name) {
        this.side = Objects.requireNonNull(side, "side");
        this.name = Objects.requireNonNull(name, "name");
    }

    /** Called once when this player is attached to a new game. */
    public void startGame() throws IOException {
        // Most player types need no setup.
    }

    /**
     * Requests one move. A second request cannot replace an outstanding one;
     * callers must cancel it first.
     */
    public final void requestMove(Position position, MoveReceiver receiver) {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(receiver, "receiver");

        synchronized (this) {
            if (moveReceiver != null) {
                throw new IllegalStateException("A move request is already pending for " + name);
            }
            requestedPosition = position;
            moveReceiver = receiver;
        }

        try {
            onMoveRequested(position);
        } catch (Exception error) {
            failMoveRequest(error);
        }
    }

    /**
     * Entry point for externally supplied input. Interactive subclasses override
     * this method; non-interactive players reject such submissions by default.
     */
    public boolean submitMove(Move move) {
        return false;
    }

    /** True when a controller may submit a move obtained from a UI or terminal. */
    public boolean acceptsExternalMoves() {
        return false;
    }

    /** Cancels the current request, if any. Late answers are then ignored. */
    public final void cancelMoveRequest() {
        boolean hadPendingRequest;
        synchronized (this) {
            hadPendingRequest = moveReceiver != null;
            clearMoveRequest();
        }

        if (hadPendingRequest) {
            onMoveRequestCancelled();
        }
    }

    /** Called when the game no longer needs this player. */
    public void endGame() {
        cancelMoveRequest();
    }

    /** Implemented by subclasses to begin acquiring a move. */
    protected abstract void onMoveRequested(Position position) throws Exception;

    /** Optional hook for stopping asynchronous work. */
    protected void onMoveRequestCancelled() {
        // No work to cancel by default.
    }

    /**
     * Completes the pending request. Engine implementations call this when a
     * bestmove arrives; interactive implementations call it from submitMove.
     */
    protected final boolean returnMove(Move move) {
        Objects.requireNonNull(move, "move");

        MoveReceiver receiver;
        synchronized (this) {
            if (moveReceiver == null) {
                return false;
            }
            receiver = moveReceiver;
            clearMoveRequest();
        }

        receiver.onMoveReceived(this, new Move(move));
        return true;
    }

    protected final Position getRequestedPosition() {
        synchronized (this) {
            return requestedPosition;
        }
    }

    protected final void failMoveRequest(Exception error) {
        MoveReceiver receiver;
        synchronized (this) {
            if (moveReceiver == null) {
                return;
            }
            receiver = moveReceiver;
            clearMoveRequest();
        }
        receiver.onMoveRequestFailed(this, error);
    }

    private void clearMoveRequest() {
        moveReceiver = null;
        requestedPosition = null;
    }

    public final String getName() {
        return name;
    }

    public final Sides getSide() {
        return side;
    }
}
