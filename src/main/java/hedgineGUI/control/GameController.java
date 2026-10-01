package hedgineGUI.control;

import hedgineGUI.core.chess.Move;
import hedgineGUI.game.GameConfiguration;
import hedgineGUI.game.GameException;
import hedgineGUI.game.Player;

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
