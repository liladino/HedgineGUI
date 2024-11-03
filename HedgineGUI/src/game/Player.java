package game;

import chess.Move;
import utility.Sides;

public abstract class Player {
	protected String name;
	protected int rating;
	protected Sides side;
	protected boolean human;
	
	public Player(Sides side, boolean human, String name, int rating) {
		this.name = name;
		this.side = side;
		this.human = human;
		this.rating = rating;
	}
	public Player(Sides side, boolean human, String name) {
		this.name = name;
		this.side = side;
		this.human = human;
		rating = 0;
	}
	public Player() {
		name = "default player";
		side = Sides.white;
		rating = 0;
		human = true;
	}
	
	/* getMove: check if there's a move available. It shouldn't care if the move is legal or not!
	 * Human: if a move was sent via command line / via GUI return that, othervise return a NullMove. 
	 * Computer: check if a move has arrived, e.g. bestmove g1f3
	 * 
	 * The first time the program calls for the engine it should additional data,
	 * so that should be a separate function with arguments: movetime, moves so far, startpos.
	 * That other getMove sends instructions via uci:
	 * 					e.g. 	position stratpos moves e2e4 e7e5
	 * 							go movetime 1000
	 * */
	public abstract Move getMove();
	
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
		if (side == Sides.black) side = Sides.white;
		else side = Sides.black;
	}
}
