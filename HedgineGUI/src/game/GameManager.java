package game;

import java.util.ArrayList;
import java.util.Scanner;

import chess.Board;
import chess.Move;
import chess.Sides;
import utility.Pair;

public class GameManager {
	private Player p1;
	private Player p2;
	private Board board;
	
	/* * * * * * * * * * *
	 * Time information  *
	 * * * * * * * * * * */
	private TimeControl controlType;
	private int whiteTime; 
	private int blackTime;
	private int increment; 
	private int incrementStartMove; //e.g. increment comes after the 40th move
	private int moveTime; 
	ArrayList<Pair<Integer, Integer>> extraTimes; //after move X give players Y time (moveCount, extraTime)
	
	/* * * * *
	 * Moves *
	 * * * * */
	private int moveCount;
	ArrayList<Move> moves;
	
	/* * * * * * * *
	 * Constructor *
	 * * * * * * * */
	public GameManager() {
		moves = new ArrayList<Move>();
		controlType = TimeControl.noControl;
		moveCount = 0;
		extraTimes = new ArrayList<Pair<Integer, Integer>>();
	}
	
	/* * * * * *
	 * Setters *
	 * * * * * */
	public void setPlayer1(Player p) {
		p1 = p;
	}
	public void setPlayer2(Player p) {
		p2 = p;
	}
	public void setBoard() {
		board = new Board();
	}
	public void setBoard(Board b) {
		board = new Board();
	}
	
	/* * * * * * * *
	 * Game Logic  *
	 * * * * * * * */
	public void startGame() throws GameStartException {
		if (board == null /*|| p1 == null || p2 == null*/) {
			throw new GameStartException("One or more more comSponents are not initialzed! (Board, Player1, Player 2)");
		}
		board.printToStream(System.out, Sides.white);
		Scanner scanner = new Scanner(System.in);
		Move m;
		while(true) {
			String s;
			if (scanner.hasNextLine()) {
				s = scanner.nextLine();
			}
			else {
				break;
			}
			m = new Move(s);
			
			if (m.isNull()) break;
			
			board.makeMove(m);
			board.printToStream(System.out, Sides.white);
		}
		scanner.close();
	}
	
	
	
}
