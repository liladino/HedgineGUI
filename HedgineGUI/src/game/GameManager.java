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
	/* * * * * *
	 * Players *
	 * * * * * */
	private Player black = null;
	private Player white = null;
	private Player currentPlayer;

	/* * * * *
	 * Meta  *
	 * * * * */
	private volatile boolean moveReady;
	private volatile boolean timeExpired;
	private volatile boolean running;
	private List<GameEventListener> eventListeners;
	private List<VisualChangeListener> updateListeners;

	/* * * * *
	 * Game  *
	 * * * * */
	private int moveCount;
	private Move currentMove = null;
	//private String startPos = null; 
	private Board board = null;
	private Clock clock = null;
	private ArrayList<Move> moves;

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

		//startPos = b.convertToFEN();
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

	/*public String getStartPos(){
		return startPos;
	}*/
	
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
		System.out.println("gameManager started");
		
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
	public synchronized void onTimeIsUp(Sides active) {
		timeExpired = true;
		notifyAll();
		handleTimeExpired(active);
	}

	public synchronized void stopRunning(){
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
		stopRunning();
	}

	private void handleTimeExpired(Sides active){
		for (GameEventListener listener : eventListeners) {
			if (active == Sides.WHITE){
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
