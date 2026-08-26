package control;

import core.chess.Move;
import game.GameConfiguration;

public interface GameController {
    void startGame(GameConfiguration config);
    void submitMove(Move move);
    void resign();
    void offerDraw();
    void takeBack();
    void stopGame();

    GameState getState();
	
}
 