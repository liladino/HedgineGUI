package game;

import java.io.IOException;

import core.ClockBuilder;
import core.TimeInputException;
import core.chess.Board;
import core.chess.IO.FENException;
import core.clock.Clock;
import core.clock.ClockController;
import core.clock.SwingTimer;
import graphics.GraphicSettings;
import graphics.MainWindow;
import graphics.dialogs.InformationDialogs;
import utility.Sides;
import utility.TimeControl;

/**
 * 
 */
public class GameStarter {
	private static Thread t = null;
	private static MainWindow mainWindow = null;
	private static GameManager gameManager = null;
	private GameStarter(){ }

	public static void startNewGame(Player white, Player black, String timeControl){
		startNewGame("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", white, black, timeControl);
	}

	public static void startNewGame(String fen, Player white, Player black, String timeControl){
		if (t != null){
			//stop the current game
			if (t.isAlive()){
				gameManager.stopRunning();
			}
			t = null;
		}
		
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
		
		initializeGame(fen, white, black, timeControl);
		
		if (mainWindow != null) mainWindow.getRightPanel().setWhiteName(white.getName());
		if (mainWindow != null) mainWindow.getRightPanel().setBlackName(black.getName());

		GraphicSettings.rotateBoard = false;
		if (!white.isHuman() && black.isHuman()){
			GraphicSettings.rotateBoard = true;
		}

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
		
		if (mainWindow != null) mainWindow.repaint();
		gameManager.notifyGameStateChanged();
		
		t = new Thread(gameManager);
		t.start();
	}
	
	private static void initializeGame(String fen, Player white, Player black, String timeControl) {
		try{
			gameManager.initialzeGame(new Board(fen), white, black);
		}
		catch(FENException e) {
			InformationDialogs.errorDialog(mainWindow, e.getMessage());
			gameManager.initialzeGame(new Board(), white, black);
		}

		// try {
		// 	ClockBuilder.setClock(gameManager.getClock(), timeControl);
		// }
		// catch (TimeInputException t){
		// 	InformationDialogs.errorDialog(mainWindow, "Invalid time fromat: " + t.getMessage() + "\nTime set to no control");
		// 	gameManager.getClock().setControlType(TimeControl.NO_CONTROL);
		// }
	}

	public static void setGameManager(GameManager gameManager){
		GameStarter.gameManager = gameManager;
	}

	public static GameManager getGameManager(){
		return gameManager;
	}

	public static void addMainWindow(MainWindow mainWindow){
		GameStarter.mainWindow = mainWindow;
	}

	public static MainWindow getMainWindow(){
		return mainWindow;
	}

}
