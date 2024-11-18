package game;

import java.util.ArrayList;
import java.util.List;

import chess.Board;
import chess.Move;
import chess.IO.FENException;
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
	private Move currentMove = null;
	private Board board = null;
	private Clock clock = null;
	private ArrayList<Move> moves;
	private String startFEN = null;
	private Result result;

	/* * * * * * * *
	 * Constructor *
	 * * * * * * * */
	public GameManager() {
		moves = new ArrayList<>();
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
		startFEN = b.convertToFEN();
	}
	public void addGameEventListener(GameEventListener listener) {
		eventListeners.add(listener); 
	}
	
	public void addVisualChangeListener(VisualChangeListener listener) {
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
	
	public Clock getClock(){
		return clock;
	}

	public List<Move> getMoves(){
		return moves;
	}

	public String startFEN(){
		return startFEN;
	}

	public Result getResult(){
		return result;
	}

	public Player getPlayer(Sides s){
		if (s == Sides.BLACK){
			return black;
		}
		return white;
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
		result = Result.ONGOING;
	}
	
	@Override
	public void run() {
		System.out.println("gameManager started");
		
		running = true;
		Thread t = null;
		if (clock.getTimeControl() != TimeControl.NO_CONTROL){
			t = new Thread(clock);
			t.start();
			clock.setTicking(true);
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
	
	/* * * * * * * * * *
	 * Game end logic  *
	 * * * * * * * * * */
	private void checkGameEnd() {
		if (board.getResult() == Result.ONGOING) {
			return;
		}
		for (GameEventListener listener : eventListeners) {
			//the game ended
			if (board.getResult() == Result.ONGOING){
				continue;
			}
			result = board.getResult();

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
	
	public synchronized void takeBack(){
		Board temp;
		try{
			temp = new Board(startFEN);
		}
		catch (FENException f){
			//can't reach this
			return;
		}
		if (moves.size() < 2) return;

		for (int i = 0; i < moves.size()-2; i++){
			temp.makeMove(moves.get(i));
		}
		board = temp;
		Move last = moves.get(moves.size()-2);
		moves.remove(moves.size()-1);
		moves.remove(moves.size()-1);

		onMoveReady(last);
	} 
	
	

	
}
