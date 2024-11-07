package game;

import chess.Move;
import game.interfaces.MoveListener;
import utility.Sides;

public class Human extends Player{
	public Human(Sides side, String name) {
		super(side, name);
		human = true;
	}

	@Override
	public void makeMove(MoveListener listener, Move m) {
		listener.onMoveReady(m);
	}
	
}
