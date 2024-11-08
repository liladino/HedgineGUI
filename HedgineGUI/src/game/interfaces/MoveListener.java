package game.interfaces;

import chess.Move;

public interface MoveListener {
	void onMoveReady(Move m);
}