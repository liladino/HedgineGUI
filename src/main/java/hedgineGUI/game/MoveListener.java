package hedgineGUI.game;

import hedgineGUI.core.chess.Move;

/**
 * Players noitfy the GameManager 
 */
public interface MoveListener {
	void onMoveReady(Move m);
}