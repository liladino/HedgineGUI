package main;

import java.io.File;

import game.GameManager;
import game.GameStarter;
import game.Human;
import graphics.MainWindow;
import utility.Sides;

public class Main {
	public static void main(String[] args) {
		GameManager gameManager = new GameManager();
		MainWindow mainWindow = new MainWindow(gameManager);

		//initialize saves folder
		File theDir = new File(System.getProperty("user.dir") + "/saves");
		if (!theDir.exists()){
			theDir.mkdirs();
		}

		GameStarter.addGameManager(gameManager);
		GameStarter.addMainWindow(mainWindow);
		GameStarter.startNewGame(new Human(Sides.WHITE, "White"), new Human(Sides.BLACK, "Black"), "N");
	}
}
