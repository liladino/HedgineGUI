package game;

import chess.Move;

public interface MoveListener {
    void onMoveReady(Move m);
}