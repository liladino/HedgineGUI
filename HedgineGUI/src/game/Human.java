package game;

import chess.Move;
import utility.Sides;

public class Human extends Player{
	public Human(Sides side, String name) {
		super(side, name);
		human = true;
	}

	@Override
	public void makeMove(Move m) {
		listener.onMoveReady(m);
	}
	
}
