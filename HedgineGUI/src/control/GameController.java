package control;

import core.chess.Move;
import game.GameConfiguration;
import game.GameException;

/** The only game-facing API the UI layer needs. */
public interface GameController {
    void startGame(GameConfiguration configuration) throws GameException;

    boolean submitMove(Move move);

    void resign();

    boolean takeBack();

    void stopGame();

    GameState getState();

    void addStateListener(GameStateListener listener);

    void removeStateListener(GameStateListener listener);
}
