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
		board = b;
	}
	
	/* * * * * * * *
	 * Game Logic  *
	 * * * * * * * */
	public void startGame() throws GameStartException {
		if (board == null /*|| p1 == null || p2 == null*/) {
			throw new GameStartException("One or more more components are not initialzed! (Board, Player1, Player 2)");
		}

		Scanner scanner = new Scanner(System.in);
		Move m = null;
		while(true) {
			if (m != null) if (m.isNull()) break;
			board.generateLegalMoves();
			board.printLegalMoves();
			
			if (board.inCheck(board.tomove())) System.out.print("Check! ");
			if (board.tomove() == Sides.white) { System.out.print("White to move\n");}
			else { System.out.print("Black to move\n"); }
			
			System.out.println(board);
			System.out.println(board.convertToFEN());
			
			board.generateLegalMoves();
			
			String s;
			if (scanner.hasNextLine()) {
				s = scanner.nextLine();
			}
			else {
				break;
			}
			m = new Move(s);
			
			if (m.isNull()) break;
			if (!board.isMoveLegal(m)) break;
			board.makeMove(m);			
		}
		scanner.close();
	}
	
	
	
}
