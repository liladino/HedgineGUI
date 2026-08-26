package game;

import java.io.IOException;

import core.chess.Board;
import core.chess.IO.FENException;
import game.clock.Clock;
import game.clock.ClockBuilder;
import game.clock.ClockController;
import game.clock.SwingTimer;
import game.clock.TimeInputException;
import graphics.GraphicSettings;
import graphics.MainWindow;
import graphics.dialogs.InformationDialogs;
import utility.Sides;

/**
 * 
 */
public class GameStarter {
	private Thread t = null;
	private MainWindow mainWindow = null;
	private GameManager gameManager;
	public GameStarter(){
		gameManager = new GameManager();
	}

	public void startNewUIGame(Player white, Player black, String timeControl){
		startNewUIGame("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", white, black, timeControl);
	}

	public void startNewUIGame(String fen, Player white, Player black, String timeControl){
		if (t != null){
			//stop the current game
			if (t.isAlive()){
				gameManager.stopRunning();
			}
			t = null;
		}
		
		setPlayers(white, black);

		initializeGame(fen, white, black, timeControl);

		setGraphics(white, black);
		
		setClockController(timeControl, fen);

		if (mainWindow != null) mainWindow.repaint();
		gameManager.notifyGameStateChanged();
		
		t = new Thread(gameManager);
		t.start();
	}

	private void setClockController(String timeControl, String fen){
		Clock clock = new Clock();
		try {
			ClockBuilder.setClock(clock, timeControl);
		}
		catch (TimeInputException e){ }

		ClockController clockController = new ClockController(new SwingTimer(), clock);
		Board b = null;
		try{
			b = new Board(fen);
			clockController.setActiveSide(b.tomove());
		}
		catch(FENException e) {	}
	}

	private void setGraphics(Player white, Player black){
		if (mainWindow != null) mainWindow.getRightPanel().setWhiteName(white.getName());
		if (mainWindow != null) mainWindow.getRightPanel().setBlackName(black.getName());

		GraphicSettings.rotateBoard = false;
		if (!white.isHuman() && black.isHuman()){
			GraphicSettings.rotateBoard = true;
		}
	}

	private void setPlayers(Player white, Player black){
		try {
			if (!white.isHuman()) ((EnginePlayer)white).validateEngine();
		}
		catch (IOException e) {
			white = new HumanPlayer(Sides.WHITE, white.getName());
			if (mainWindow != null) InformationDialogs.errorDialog(mainWindow, "White error: " + e.getMessage() + "\nThe player is set to be human.");
		}
		
		try {
			if (!black.isHuman()) ((EnginePlayer)black).validateEngine();
		}
		catch (IOException e) {
			black = new HumanPlayer(Sides.BLACK, black.getName());
			if (mainWindow != null) InformationDialogs.errorDialog(mainWindow, "Black error: " + e.getMessage() + "\nThe player is set to be human.");
		}
				
		white.setMoveListener(gameManager);
		black.setMoveListener(gameManager);
	}
	
	private void initializeGame(String fen, Player white, Player black, String timeControl) {
		try{
			gameManager.initialzeGame(new Board(fen), white, black);
		}
		catch(FENException e) {
			InformationDialogs.errorDialog(mainWindow, e.getMessage());
			gameManager.initialzeGame(new Board(), white, black);
		}
	}

	// public void setGameManager(GameManager gameManager){
	// 	this.gameManager = gameManager;
	// }

	public GameManager getGameManager(){
		return gameManager;
	}

	public void setMainWindow(MainWindow mainWindow){
		this.mainWindow = mainWindow;
	}

	public MainWindow getMainWindow(){
		return mainWindow;
	}

}
