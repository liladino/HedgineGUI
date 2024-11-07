package game;

import java.util.ArrayList;
import java.util.List;

import chess.Board;
import chess.Move;
import utility.*;

public class GameManager implements Runnable, MoveListener, TimeEventListener{
	private Player black = null;
	private Player white = null;
	private Player currentPlayer;
	private Board board = null;
	private volatile boolean moveReady;
	private volatile boolean timeExpired;
	private GameEventListener gameEventListener;
	private List<GameUpdateListener> listeners;
	private Clock clock = null;

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
		clock = new Clock(TimeControl.NO_BONUS);
		clock.setStartTime(5000);
		clock.setTimeEventListener(this);
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
		if (board == null || white == null || black == null || gameEventListener == null) {
			throw new GameStartException("One or more more components are not initialzed! (Board/Player1/Player 2/GameEventListener)");
		}
		
		Thread t;
		if (clock.getTimeControl() != TimeControl.NO_CONTROL){
			clock.setTicking(true);
			t = new Thread(clock);
			t.start();
		}

		while (true) {
			synchronized (this) {
				System.out.println((board.tomove() == Sides.WHITE ? "White to move" : "Black to move"));
				moveReady = false;
				
				while (!moveReady && !timeExpired) {
					try {
						wait(); 
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt(); 
					}
				}

				if (timeExpired) {
					System.out.println("Time expired for " + currentPlayer);
					handleTimeExpired(); // e.g., declare victory for the opponent
					break; // Exit the game loop if needed
				}
				
				if (board.isMoveLegal(currentMove)) {
					board.makeMove(currentMove);
					currentPlayer = (currentPlayer == white) ? black : white;
					
					clock.pressClock();
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
	
	private void checkGameEnd() {
		if (board.getResult() == Result.ONGOING) {
			return;
		}
		//the game ended
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
			
		System.exit(0);
	}

	private void handleTimeExpired(){
		if (board.tomove() == Sides.WHITE){
			if (board.sufficientMaterial(Sides.BLACK)){
				gameEventListener.onTimeIsUp(Sides.BLACK);
			}
			else{
				gameEventListener.onTimeIsUp();
			}
		}
		else {
			if (board.sufficientMaterial(Sides.WHITE)){
				gameEventListener.onTimeIsUp(Sides.WHITE);
			}
			else{
				gameEventListener.onTimeIsUp();
			}
		}

		System.exit(0);
	}

	@Override
	public void onTimeIsUp() {
		handleTimeExpired();
	}
	

	
}
