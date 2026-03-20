package game.interfaces;

import core.chess.Move;

/**
 * Players noitfy the GameManager 
 */
public interface MoveListener {
	void onMoveReady(Move m);
}