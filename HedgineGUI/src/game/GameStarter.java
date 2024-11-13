package game;

import chess.Board;
import chess.IO.FENException;
import graphics.MainWindow;
import graphics.dialogs.GameEndDialogs;
import utility.Sides;

public class GameStarter {
    private static Thread t = null;
    private static MainWindow mainWindow = null;
    private static GameManager gameManager = null;
    private GameStarter(){ }

    public static void startNewGame(Player white, Player black){
        startNewGame("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", white, black);
    }

    public static void startNewGame(String fen, Player white, Player black){
        if (t != null){
            //stop the current game, and than do shit
            t = null;
        }

		//Human w = new Human(Sides.WHITE, "vilagos");
		//Human b = new Human(Sides.BLACK, "sotet");
		
		try{
			gameManager.initialzeGame(new Board(fen), white, black);
		}
		catch(FENException e) {
            GameEndDialogs.errorDialog(mainWindow, e.getMessage());
            gameManager.initialzeGame(new Board(), white, black);
		}
		
        mainWindow.repaint();
        
		t = new Thread(gameManager);
		t.start();
    }

    public static void addGameManager(GameManager gameManager){
        GameStarter.gameManager = gameManager;
    }

    public static void addMainWindow(MainWindow mainWindow){
        GameStarter.mainWindow = mainWindow;
    }
}
