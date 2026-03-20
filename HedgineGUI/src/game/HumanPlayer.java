package game;

import core.chess.Move;
import utility.Sides;

public class HumanPlayer extends Player{
	public HumanPlayer(Sides side, String name) {
		super(side, name);
		human = true;
	}

	@Override
	public void makeMove(Move m) {
		listener.onMoveReady(m);
	}
	
}
