package game;

import java.util.logging.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import chess.Board;
import chess.Move;
import chess.IO.FENException;
import game.interfaces.ClockListener;
import game.interfaces.GameEventListener;
import game.interfaces.VisualChangeListener;
import graphics.dialogs.InformationDialogs;
import game.interfaces.MoveListener;
import game.interfaces.TimeEventListener;
import utility.*;

/**
 * Manages the lifecycle and logic of a chess game, including player interactions,
 * time control, move processing, and engine communication.
 *
 * Serves as the central controller for chess games, handles board setup, player turns, time management, and interaction with external chess engines. 
 * Notifies listeners about game state changes, visual updates, and game results.
 */
public class GameManager implements Runnable, MoveListener, TimeEventListener{
	private static final Logger logger = Logger.getLogger(GameManager.class.getName());

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
	private String lastEngineCommand = null;

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
		moves = new ArrayList<>();
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
	public void setResult(Result r) {
		result = r;
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
	
	public boolean isGameRunning() {
		return running;
	}

	public String lastEngineCommand() {
		return lastEngineCommand;
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
		logger.info("gameManager started");
		
		running = true;		
		Thread t = null;
		if (clock.getTimeControl() != TimeControl.NO_CONTROL){
			t = new Thread(clock);
			t.start();
			clock.setTicking(true);
		}
		
		try {
			startEngines();
		} 
		catch (IOException e) {
			InformationDialogs.errorDialog(null, "Failed to communicate with engine: " + e.getMessage());
			stopRunning();
		}
	
		if (!currentPlayer.isHuman()) notifyEngine();

		while (running) {
			synchronized (this) {
				logger.info((board.tomove() == Sides.WHITE ? "White to move" : "Black to move"));
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
					
					if (running && !currentPlayer.isHuman()) notifyEngine();
				}
				else {
					logger.info("Illegal input: ");
				}
				logger.info(currentMove.toString());
			}
		}
		
		stopRunning();
		logger.info("gameManager stooped");
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
				listener.onDraw();
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
	
	private void notifyEngine() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("position fen " + startFEN);
		
		if (!moves.isEmpty()) {
			sb.append(" moves ");
			for (Move m : moves) {
				sb.append(m.toString() + " ");
			}
		}
		sb.append("\n");
		
		if (clock.getTimeControl() == TimeControl.NO_CONTROL) {
			sb.append("go movetime 2000");
		}
		else if (clock.getTimeControl() == TimeControl.FIX_TIME_PER_MOVE) {
			sb.append("go movetime " + (int)(clock.getWhiteTime() * 0.9));
		}
		else if (clock.getTimeControl() == TimeControl.FISCHER) {
			sb.append("go wtime ");
			sb.append(clock.getWhiteTime());
			sb.append(" btime ");
			sb.append(clock.getBlackTime());
			if (clock.getIncrementStartMove() < moves.size() / 2) {
				sb.append(" winc ");
				sb.append(clock.getIncrement());
				sb.append(" binc ");
				sb.append(clock.getIncrement());
			}
		}
		
		lastEngineCommand = new String(sb);
		try {
			((Engine)currentPlayer).sendCommand(lastEngineCommand);
		} catch (IOException e) {
			return;
		}
	}
	
	private void startEngines() throws IOException {
		if (!white.isHuman()) {
			((Engine)white).sendCommand("ucinewgame");
		}
		if (!black.isHuman()) {
			((Engine)black).sendCommand("ucinewgame");
		}
	}
	
	public void notifyGameStateChanged() {
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
		if (!white.isHuman()) {
			((Engine)white).quitEngine();
		}
		if (!black.isHuman()) {
			((Engine)black).quitEngine();
		}
		notifyAll();
	}
	
	public synchronized void takeBack(){
		Board temp;
		try{
			temp = new Board(startFEN);
		}
		catch (FENException f){
			return;
		}
		if (moves.isEmpty()) return;
		
		if (!currentPlayer.isHuman()) {
			try {
				((Engine)(currentPlayer)).sendCommand("stop");
			} catch (IOException e) {
				return;
			}
		}
		
		for (int i = 0; i < moves.size()-1; i++){
			temp.makeMove(moves.get(i));
		}
		moves.remove(moves.size()-1);
		board = temp;
		clock.pressClock();
		currentPlayer = (currentPlayer == white) ? black : white;
		if (!currentPlayer.isHuman()) notifyEngine();
		
		notifyGameStateChanged();
	} 
}
