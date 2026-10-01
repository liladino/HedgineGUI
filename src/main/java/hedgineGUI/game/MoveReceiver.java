package game;

import core.chess.Move;

/** Receives the one result of a Player.requestMove call. */
public interface MoveReceiver {
    void onMoveReceived(Player player, Move move);

    default void onMoveRequestFailed(Player player, Exception error) {
        // The receiver may choose to turn a player failure into a game failure.
    }
}
