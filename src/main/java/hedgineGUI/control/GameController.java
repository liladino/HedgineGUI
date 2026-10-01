package control;

import core.chess.Move;
import game.GameConfiguration;
import game.GameException;
import game.Player;

/** The only game-facing API the UI layer needs. */
public interface GameController {
    void startGame(GameConfiguration configuration) throws GameException;

    boolean submitMove(Move move);

    void resign();

    void smartTakeBack();

    void stopGame();

    void quitEngines();

    void requestMove();

    GameState getState();

    Player getCurrentPlayer();

    void addStateListener(GameStateListener listener);

    void removeStateListener(GameStateListener listener);

    String getStartFEN();

    String getFEN();

    String getPGN();
}
