package main;

import java.io.File;

import game.GameManager;
import game.GameStarter;
import game.HumanPlayer;
import graphics.MainWindow;
import utility.Sides;

public class Main {
	public static void main(String[] args) {
		// GameManager gameManager = new GameManager();
		
		GameStarter gameStarter = new GameStarter();

		MainWindow mainWindow = new MainWindow(gameStarter.getGameManager());

		//initialize saves folder
		File theDir = new File(System.getProperty("user.dir") + "/saves");
		if (!theDir.exists()){
			theDir.mkdirs();
		}

		// GameStarter.setGameManager(gameManager);s
		// GameStarter.setMainWindow(mainWindow);
		gameStarter.startNewUIGame(new HumanPlayer(Sides.WHITE, "White"), new HumanPlayer(Sides.BLACK, "Black"), "N");
	}
}
