package game;

import chess.Move;
import game.interfaces.MoveListener;
import utility.Sides;

public abstract class Player {
	protected String name;
	protected int rating;
	protected Sides side;
	protected boolean human;
	
	public Player(Sides side, String name, int rating) {
		this.name = name;
		this.side = side;
		this.rating = rating;
	}
	public Player(Sides side, String name) {
		this.name = name;
		this.side = side;
		rating = 0;
	}
	
	public abstract void makeMove(MoveListener listener, Move m);
	
	public String getName() {
		return name;
	}
	
	public Sides getSide() {
		return side;
	}
	
	public int getRating() {
		return rating;
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
