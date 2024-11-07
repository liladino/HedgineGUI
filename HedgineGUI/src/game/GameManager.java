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
	private List<GameEventListener> eventListeners = null;
	private List<GameUpdateListener> updateListeners;
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
		updateListeners = new ArrayList<>();
		eventListeners = new ArrayList<>();
		clock = new Clock(TimeControl.NO_BONUS, this);
		clock.setStartTime(60 * 1000);
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
	public void addGameEventListener(GameEventListener listener) {
		eventListeners.add(listener); 
	}
	
	public void addGameUpdateListener(GameUpdateListener listener) {
		updateListeners.add(listener);
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
		if (board == null || white == null || black == null || eventListeners == null) {
			throw new GameStartException("One or more more components are not initialzed! (Board/Player1/Player2/GameEventListener)");
		}
		
		Thread t = null;
		boolean clockStarted = false;
		if (clock.getTimeControl() != TimeControl.NO_CONTROL){
			clock.setTicking(true);
			t = new Thread(clock);
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
					handleTimeExpired();
					break; 
				}
				
				if (board.isMoveLegal(currentMove)) {
					board.makeMove(currentMove);
					currentPlayer = (currentPlayer == white) ? black : white;
					
					if (t != null){
						if (!clockStarted){
							clockStarted = true;
							t.start();
						}
						clock.pressClock();
					}
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
	
	/* * * * * * * * *
	 * COMMUNICATION *
	 * * * * * * * * */
	
	private void notifyGameStateChanged() {
		for (GameUpdateListener listener : updateListeners) {
			listener.onGameStateChanged();
		}
	}

	@Override
	public synchronized void onMoveReady(Move m) {
		currentMove = m;
		moveReady = true;
		notifyAll();
	}
	
	private void checkGameEnd() {
		if (board.getResult() == Result.ONGOING) {
			return;
		}
		for (GameEventListener listener : eventListeners) {
			//the game ended
			if (Result.WHITE_WON == board.getResult()) {
				listener.onCheckmate(Sides.WHITE);
			}
			else if (Result.BLACK_WON == board.getResult()){
				listener.onCheckmate(Sides.BLACK);
			}
			else if (Result.STALEMATE == board.getResult()) {
				listener.onStalemate();
			}
			else if (Result.DRAW == board.getResult()) {
				//if the board signals a draw, the material is insufficient.
				listener.onInsufficientMaterial();
			}
		}
		
		System.exit(0);
	}

	private void handleTimeExpired(){
		for (GameEventListener listener : eventListeners) {
			if (board.tomove() == Sides.WHITE){
				if (board.sufficientMaterial(Sides.BLACK)){
					listener.onTimeIsUp(Sides.BLACK);
				}
				else{
					listener.onTimeIsUp();
				}
			}
			else {
				if (board.sufficientMaterial(Sides.WHITE)){
					listener.onTimeIsUp(Sides.WHITE);
				}
				else{
					listener.onTimeIsUp();
				}
			}
		}

		System.exit(0);
	}

	@Override
	public void onTimeIsUp() {
		handleTimeExpired();
	}
	

	
}
