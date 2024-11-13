package game;

import java.util.ArrayList;
import java.util.List;

import chess.Board;
import chess.Move;
import game.interfaces.ClockListener;
import game.interfaces.GameEventListener;
import game.interfaces.VisualChangeListener;
import game.interfaces.MoveListener;
import game.interfaces.TimeEventListener;
import utility.*;

public class GameManager implements Runnable, MoveListener, TimeEventListener{
	private Player black = null;
	private Player white = null;
	private Player currentPlayer;
	private Board board = null;
	private volatile boolean moveReady;
	private volatile boolean timeExpired;
	private volatile boolean running;
	private List<GameEventListener> eventListeners;
	private List<VisualChangeListener> updateListeners;
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
		clock = new Clock(this);
		clock.setTimeEventListener(this);

		clock.setControlType(TimeControl.FIX_TIME_PER_MOVE);
		clock.setMoveTime(new Second(5));
		clock.setActiveSide(Sides.WHITE);
	}
	
	/* * * * * *
	 * Setters *
	 * * * * * */
	public void setBoard() {
		setBoard(new Board());
	}
	public void setBoard(Board b) {
		board = b;
		int plies = b.getFullMoveCount() * 2 + (b.tomove() == Sides.WHITE ? 0 : 1);
		clock.setPlyCount(plies);
		clock.setActiveSide(b.tomove());
	}
	public void addGameEventListener(GameEventListener listener) {
		eventListeners.add(listener); 
	}
	
	public void addGameUpdateListener(VisualChangeListener listener) {
		updateListeners.add(listener);
	}

	public void setClockPanels(ClockListener whiteClockPanel, ClockListener blackClockPanel){
		clock.setClockPanels(whiteClockPanel, blackClockPanel);
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

	public Clock getClock(){
		return clock;
	}
	
	/* * * * * * * *
	 * Game Logic  *
	 * * * * * * * */
	public void initialzeGame(Board b, Player p1, Player p2) {
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
	public void run() {
		running = true;
		Thread t = null;
		boolean clockStarted = false;
		if (clock.getTimeControl() != TimeControl.NO_CONTROL){
			clock.setTicking(false);
			t = new Thread(clock);
			t.start();
		}

		while (running) {
			synchronized (this) {
				System.out.println((board.tomove() == Sides.WHITE ? "White to move" : "Black to move"));
				moveReady = false;
				
				while (!moveReady && !timeExpired && running) {
					try {
						wait(); 
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt(); 
					}
				}

				if (timeExpired || !running) {
					break; 
				}
				
				if (board.isMoveLegal(currentMove)) {
					board.makeMove(currentMove);
					currentPlayer = (currentPlayer == white) ? black : white;
					moves.add(currentMove);
					
					if (t != null){
						if (!clockStarted){
							clockStarted = true;
							clock.setTicking(true);
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

		System.out.println("gameManager stooped");
	}
	
	/* * * * * * * * *
	 * COMMUNICATION *
	 * * * * * * * * */
	
	private void notifyGameStateChanged() {
		for (VisualChangeListener listener : updateListeners) {
			listener.onGameStateChanged();
		}
	}

	@Override
	public synchronized void onMoveReady(Move m) {
		currentMove = m;
		moveReady = true;
		notifyAll();
	}

	@Override
	public void onTimeIsUp() {
		timeExpired = true;
		handleTimeExpired();
	}

	public void stopRunning(){
		running = false;
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
	}

	
	

	
}
