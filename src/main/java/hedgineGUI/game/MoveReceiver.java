package hedgineGUI.game;

import hedgineGUI.core.chess.Move;

/** Receives the one result of a Player.requestMove call. */
public interface MoveReceiver {
    /** receivedAtMs is when the move reached the GUI (System.currentTimeMillis), used to stop the clock. */
    void onMoveReceived(Player player, Move move, long receivedAtMs);

    default void onMoveRequestFailed(Player player, Exception error) {
        // The receiver may choose to turn a player failure into a game failure.
    }
}
