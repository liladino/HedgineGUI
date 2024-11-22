package game;

import chess.Move;
import game.interfaces.MoveListener;
import utility.Sides;

public abstract class Player {
	protected String name;
	protected Sides side;
	protected boolean human;
	protected MoveListener listener;
	
	protected Player(Sides side, String name) {
		this.name = name;
		this.side = side;
	}
	
	public void setMoveListener(MoveListener listener) {
		this.listener = listener;
	}
	
	public abstract void makeMove(Move m);
	
	public String getName() {
		return name;
	}
	
	public Sides getSide() {
		return side;
	}
	
	public boolean isHuman() {
		return human;
	}
	
	public void setSide(Sides s) {
		side = s;
	}
	
	public void switchSide() {
		if (side == Sides.BLACK) side = Sides.WHITE;
		else side = Sides.BLACK;
	}
}
