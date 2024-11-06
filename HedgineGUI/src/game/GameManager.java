package game;

import java.util.ArrayList;
import java.util.List;

import chess.Board;
import chess.Move;
import utility.*;

public class GameManager implements Runnable, MoveListener{
	private Player black = null;
	private Player white = null;
	private Player currentPlayer;
	private Board board = null;
	private boolean moveReady;
	private GameEventListener gameEventListener;
	private List<GameUpdateListener> listeners;
	
	/* * * * *
	 * Moves *
	 * * * * */
	private int moveCount;
	ArrayList<Move> moves;
	private Move currentMove = null;
	
	/* * * * * * * *
	 * Constructor *
	 * * * * * * * */
	public GameManager() {
		moves = new ArrayList<>();
		moveCount = 0;
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
		if (b == null || p1 == null || p2 == null) {
			throw new GameStartException("One or more more components are not initialzed! (Board, Player1, Player 2)");
		}
		
		if (p1.getSide() == Sides.WHITE) {
			white = p1;
			black = p2;
		}
		else {
			white = p2;
			black = p1;
		}
		setBoard(b);
		
		currentPlayer = white;
	}
	
	@Override
	public void run() throws GameStartException {
		if (board == null || white == null || black == null) {
			throw new GameStartException("One or more more components are not initialzed! (Board, Player1, Player 2)");
		}

		while (true) {
			synchronized (this) {
				System.out.println((board.tomove() == Sides.WHITE ? "White to move" : "Black to move"));
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
		if (board.getResult() == Result.ONGOING) {
			return;
		}
		//the game ended
		if (gameEventListener != null) {
			if (Result.WHITE_WON == board.getResult()) {
				gameEventListener.onCheckmate(Sides.WHITE);
			}
			else if (Result.BLACK_WON == board.getResult()){
				gameEventListener.onCheckmate(Sides.BLACK);
			}
			else if (Result.STALEMATE == board.getResult()) {
				gameEventListener.onStalemate();
			}
			else if (Result.DRAW == board.getResult()) {
				//if the board signals a draw, the material is insufficient.
				gameEventListener.onInsufficientMaterial();
			}
		}
			
		System.exit(0);
	}

	
}
