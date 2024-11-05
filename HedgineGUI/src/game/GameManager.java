package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import chess.Board;
import chess.Move;
import graphics.GameEventListener;
import utility.*;

public class GameManager implements Runnable, MoveListener{
	private Player black = null;
	private Player white = null;
	private Player currentPlayer;
	private Board board = null;
	private Move currentMove = null;
	private boolean moveReady;
	private GameEventListener gameEventListener;
	private List<GameUpdateListener> listeners;
	
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
		listeners = new ArrayList<>();
	}
	
	/* * * * * *
	 * Setters *
	 * * * * * */
	public void setBoard() {
		board = new Board();
	}
	public void setBoard(Board b) {
		board = b;
	}
	public void setGameEventListener(GameEventListener listener) {
		gameEventListener = listener; 
	}
	
	public void addGameUpdateListener(GameUpdateListener listener) {
		listeners.add(listener);
	}

	private void notifyGameStateChanged() {
		for (GameUpdateListener listener : listeners) {
			listener.onGameStateChanged();
		}
	}
	
	/* * * * * *
	 * Getters *
	 * * * * * */
	public Board getBoard() {
		return board;
		//return new Board(board);
	}
	
	public Player getCurrentPlayer() {
		return currentPlayer;
	}
	
	public int getMoveCount() {
		return moveCount;
	}
	
	/* * * * * * * *
	 * Game Logic  *
	 * * * * * * * */
	public void initialzeGame(Board b, Player p1, Player p2) throws GameStartException {
		if (p1.getSide() == Sides.white) {
			white = p1;
			black = p2;
		}
		else {
			white = p2;
			black = p1;
		}
		setBoard(b);
		if (board == null || p1 == null || p2 == null) {
			throw new GameStartException("One or more more components are not initialzed! (Board, Player1, Player 2)");
		}
		
		currentPlayer = white;
	}
	
	@Override
	public void run() throws GameStartException {
		if (board == null || white == null || black == null) {
			throw new GameStartException("One or more more components are not initialzed! (Board, Player1, Player 2)");
		}

		while (true) {
			synchronized (this) {
				System.out.println((board.tomove() == Sides.white ? "White to move" : "Black to move"));
				moveReady = false;
				
				while (!moveReady) {
					try {
						wait(); 
					} catch (InterruptedException e) {
						//time ran out maybe?
						Thread.currentThread().interrupt(); 
					}
				}
				
				if (board.isMoveLegal(currentMove)) {
					board.makeMove(currentMove);
					currentPlayer = (currentPlayer == white) ? black : white;
					
					notifyGameStateChanged();
					checkGameEnd();
				}
				else {
					System.out.print("Illegal input: ");
				}
				System.out.println(currentMove);
			}
		}
	}
	
	@Override
	public synchronized void onMoveReady(Move m) {
		currentMove = m;
		moveReady = true;
		notify();
	}
	
	public void checkGameEnd() {
		if (board.getResult() == Result.onGoing) {
			return;
		}
		//the game ended
		if (gameEventListener != null) {
			if (Result.whiteWon == board.getResult()) {
				gameEventListener.onCheckmate(Sides.white);
			}
			else if (Result.blackWon == board.getResult()){
				gameEventListener.onCheckmate(Sides.black);
			}
			else if (Result.stalemate == board.getResult()) {
				gameEventListener.onStalemate();
			}
			else if (Result.draw == board.getResult()) {
				//if the board signals a draw, the material is insufficient.
				gameEventListener.onInsufficientMaterial();
			}
		}
			
		System.exit(0);
	}

	
}
