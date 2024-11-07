package main;

import chess.Board;
import chess.IO.FENException;
import game.GameManager;
import game.Human;
import graphics.MainWindow;
import utility.Sides;

public class Main {
	public static void main(String[] args) {
		GameManager gameManager = new GameManager();
		
		Human w = new Human(Sides.WHITE, "vilagos");
		Human b = new Human(Sides.BLACK, "sotet");
		
		/*try{
			gameManager.initialzeGame(new Board("k7/7P/8/K7/8/8/8/8 w KQkq - 0 1"), w, b);
		}
		catch(FENException e) {
			System.out.println(e.getMessage());*/
			gameManager.initialzeGame(new Board(), w, b);
		//}
		
		new MainWindow(gameManager);
		
		Thread t = new Thread(gameManager);
		t.start();
	}
}
