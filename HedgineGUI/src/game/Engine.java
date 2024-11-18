package game;

import java.io.File;

import chess.Move;
import game.interfaces.MoveListener;
import utility.Sides;

public class Engine extends Player {
	File enginePath;
	public Engine(Sides side, String name, File enginePath) {
		super(side, name);
		human = false;
		this.enginePath = enginePath;
	}

	@Override
	public void makeMove(MoveListener listener, Move m) {
		// tell the gameManager, that we have a move ready
		listener.onMoveReady(m);
	}
}
