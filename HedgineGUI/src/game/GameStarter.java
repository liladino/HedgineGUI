package game;

import chess.Board;
import chess.IO.FENException;
import graphics.GraphicSettings;
import graphics.MainWindow;
import graphics.dialogs.InformationDialogs;
import utility.TimeControl;

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
		
		try{
			gameManager.initialzeGame(new Board(fen), white, black);
		}
		catch(FENException e) {
			InformationDialogs.errorDialog(mainWindow, e.getMessage());
			gameManager.initialzeGame(new Board(), white, black);
		}

		try {
			TimeInformationConverter.setClock(gameManager.getClock(), timeControl);
		}
		catch (TimeInputException t){
			InformationDialogs.errorDialog(mainWindow, "Invalid time fromat: " + t.getMessage());
			gameManager.getClock().setControlType(TimeControl.NO_CONTROL);
		}
		
		mainWindow.getRightPanel().setWhiteName(white.getName());
		mainWindow.getRightPanel().setBlackName(black.getName());

		GraphicSettings.rotateBoard = false;
		if (!white.isHuman() && black.isHuman()){
			GraphicSettings.rotateBoard = true;
		}

		mainWindow.repaint();
		
		t = new Thread(gameManager);
		t.start();
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
}
