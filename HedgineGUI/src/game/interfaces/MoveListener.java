package game.interfaces;

import chess.Move;

/**
 * Players noitfy the GameManager 
 */
public interface MoveListener {
	void onMoveReady(Move m);
}