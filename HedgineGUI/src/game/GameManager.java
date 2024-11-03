package game;

import java.util.ArrayList;
import java.util.Scanner;

import chess.Board;
import chess.Move;
import utility.*;

public class GameManager {
	private Player p1 = null;
	private Player p2 = null;
	private Board board = null;
	
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
	
	/* * * * * *
	 * Getters *
	 * * * * * */
	public Board getBoard() {
		return new Board(board);
	}
	
	public int getMoveCount() {
		return moveCount;
	}
	
	/* * * * * * * *
	 * Game Logic  *
	 * * * * * * * */
	//Command line 2 player game
	public void startGame() throws GameStartException {
		if (board == null /*|| p1 == null || p2 == null*/) {
			throw new GameStartException("One or more more components are not initialzed! (Board, Player1, Player 2)");
		}

		Scanner scanner = new Scanner(System.in);
		Move m = null;
		while(true) {
			if (m != null) if (m.isNull()) break;
			System.out.println(board.generateLegalMoves());
			//board.printLegalMoves();
			
			if (board.inCheck(board.tomove())) System.out.print("Check! ");
			if (board.tomove() == Sides.white) { System.out.print("White to move\n");}
			else { System.out.print("Black to move\n"); }
			
			System.out.println(board);
			System.out.println(board.convertToFEN());
			
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
			moveCount++;
		}
		scanner.close();
	}
	
	public void initialzeGame(Board b, Player p1, Player p2) throws GameStartException {
		setBoard(b);
		setPlayer1(p1);
		setPlayer2(p2);
		if (board == null || p1 == null || p2 == null) {
			throw new GameStartException("One or more more components are not initialzed! (Board, Player1, Player 2)");
		}
	}
	
	public boolean handleMove(Move m) {
        if (board.isMoveLegal(m)) {
            board.makeMove(m);
            
            if (board.getResult() != Result.onGoing) {
            	//the game ended
            	System.exit(0);
            }
            
            return true;
        }
        return false;
    }
	
}
