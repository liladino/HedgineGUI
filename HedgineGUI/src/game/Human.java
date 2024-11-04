package game;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import chess.Move;
import chess.Square;
import graphics.PromotionDialog;
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
