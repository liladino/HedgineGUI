package game;

import java.util.logging.Logger;

import core.chess.Board;
import core.chess.Move;
import core.chess.IO.FENException;
import core.chess.IO.PGNConverter;
import core.clock.ClockListener;
import core.clock.ClockSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.StructuredTaskScope.TimeoutException;

import graphics.dialogs.InformationDialogs;
import utility.*;

/**
 * Manages the lifecycle and logic of a chess game
 */
public class GameManager implements Runnable, MoveListener, TimeEventListener, ClockListener{
	/* TODO: implement a GameController facade that exposes an interface for e.g. menu
	 * to control the GameManager object, and request data from it.
	 * 
	 * TODO: implement GameState, which contains the relevant information for the chessboard panel
	 */
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
	private String lastEngineCommand = null;

	/* * * * *
	 * Game  *
	 * * * * */
	private Move currentMove = null;
	private Board board = null;
	private ArrayList<Move> moves;
	private String startFEN = null;
	private Result result;
	private ClockSnapshot lastClockSnapshot = null;

	/* * * * * * * *
	 * Constructor *
	 * * * * * * * */
	public GameManager() {
		eventListeners = new ArrayList<>();
	}
		
	/* * * * * *
	 * Setters *
	 * * * * * */
	// public void setBoard() {
	// 	setBoard(new Board());
	// }
	private void setBoard(Board b) {
		moves = new ArrayList<>();
		board = b;
		int plies = b.getFullMoveCount() * 2 + (b.tomove() == Sides.WHITE ? 0 : 1);
		startFEN = b.convertToFEN();
	}

	public void addGameChangeListener(GameEventListener listener) {
		eventListeners.add(listener); 
	}

	// public void setClockPanels(ClockListener whiteClockPanel, ClockListener blackClockPanel){
		// clock.setClockPanels(whiteClockPanel, blackClockPanel);
	// }

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
	public Player getWhite() {
		return white;
	}
	public Player getBlack() {
		return black;
	}
	public List<Move> getMoves(){
		return moves;
	}
	public String getStartFEN(){
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
		
		try {
			startEngines();
		} 
		catch (IOException e) {
			InformationDialogs.errorDialog(null, "Failed to communicate with engine: " + e.getMessage());
			stopRunning();
		}
	
		if (!currentPlayer.isHuman()) notifyEngine();

		while (running) {
			logger.info((board.tomove() == Sides.WHITE ? "White to move" : "Black to move"));
			moveReady = false;
			
			synchronized (this) {
				while (!moveReady && !timeExpired && running) {
					try {
						wait(); 
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt(); 
					}
				}
			}

			if (timeExpired || !running) {
				if (!running) {
					logger.info("game not running");
				}
				if (timeExpired) {
					logger.info("time expired");
				}
				break; 
			}
			
			if (board.isMoveLegal(currentMove)) {
				board.makeMove(currentMove);
				currentPlayer = (currentPlayer == white) ? black : white;
				moves.add(currentMove);

				notifyGameStateChanged();
				checkGameEnd();
				
				if (running && !currentPlayer.isHuman()) notifyEngine();
			}
			else {
				logger.info("Illegal input: ");
			}
			logger.info(currentMove.toString());
		}
		
		stopRunning();
		timeExpired = false;
		logger.info("gameManager stopped");
	}
	
	/* * * * * * * * * *
	 * Game end logic  *
	 * * * * * * * * * */
	private void checkGameEnd() {
		result = board.getResult();
		if (result == Result.ONGOING) {
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
		
		if (null == lastClockSnapshot) {
			sb.append("go movetime 2000");
		}
		else {
			if (lastClockSnapshot.getTimeControl() == TimeControl.NO_CONTROL) {
				sb.append("go movetime 2000");
			}
			else if (lastClockSnapshot.getTimeControl() == TimeControl.FIX_TIME_PER_MOVE) {
				sb.append("go movetime " + (int)(lastClockSnapshot.getWhiteTimeMs() * 0.9));
			}
			else if (lastClockSnapshot.getTimeControl() == TimeControl.FISCHER) {
				sb.append("go wtime ");
				sb.append(lastClockSnapshot.getWhiteTimeMs());
				sb.append(" btime ");
				sb.append(lastClockSnapshot.getBlackTimeMs());
				if (lastClockSnapshot.getWincMs() != 0 || lastClockSnapshot.getBincMs() != 0) {
					sb.append(" winc ");
					sb.append(lastClockSnapshot.getWincMs());
					sb.append(" binc ");
					sb.append(lastClockSnapshot.getWincMs());
				}
			}
		}
		
		lastEngineCommand = new String(sb);
		try {
			((EnginePlayer)currentPlayer).sendCommand(lastEngineCommand);
		} catch (IOException e) {
			return;
		}
	}
	
	private void startEngines() throws IOException {
		if (!white.isHuman()) {
			((EnginePlayer)white).sendCommand("ucinewgame");
		}
		if (!black.isHuman()) {
			((EnginePlayer)black).sendCommand("ucinewgame");
		}
	}
	
	public void notifyGameStateChanged() {
		for (GameEventListener listener : eventListeners) {
			listener.onGameStateChanged(PGNConverter.convertToMoves(getStartFEN(), getMoves()));
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
			((EnginePlayer)white).quitEngine();
		}
		if (!black.isHuman()) {
			((EnginePlayer)black).quitEngine();
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
		
		int takebacks = 1;
		
		if (!currentPlayer.isHuman()) {
			try {
				((EnginePlayer)(currentPlayer)).sendCommand("stop");
			} catch (IOException e) {
				return;
			}
		}
		else {
			//if the other player is an engine, one should take back 2 moves.
			Player otherPlayer = (currentPlayer == white ? black : white);
			if (!otherPlayer.isHuman()) {
				takebacks = 2;
			}
		}
		
		for (int i = 0; i < moves.size()-takebacks; i++){
			temp.makeMove(moves.get(i));
		}
		for (int i = 0; i < takebacks; i++) {
			moves.remove(moves.size()-1);
			currentPlayer = (currentPlayer == white) ? black : white;
		}
		
		if (takebacks == 1) {
			// clock.pressClock();
		}
		
		board = temp;
		if (!currentPlayer.isHuman()) notifyEngine();
		
		notifyGameStateChanged();
	}

	@Override
	public void onTick(ClockSnapshot snapshot) {
		lastClockSnapshot = snapshot;
	}

	@Override
	public void onTimeUp(ClockSnapshot snapshot) {
		lastClockSnapshot = snapshot;
		onTimeIsUp(snapshot.getFlaggedSide());
	} 
}
