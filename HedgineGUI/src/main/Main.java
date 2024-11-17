package main;

import game.GameManager;
import game.GameStarter;
import game.Human;
import graphics.MainWindow;
import utility.Sides;

public class Main {
	public static void main(String[] args) {
		GameManager gameManager = new GameManager();
		MainWindow mainWindow = new MainWindow(gameManager);

		GameStarter.addGameManager(gameManager);
		GameStarter.addMainWindow(mainWindow);
		GameStarter.startNewGame(new Human(Sides.WHITE, "White"), new Human(Sides.BLACK, "Black"), "N");
	}
}
