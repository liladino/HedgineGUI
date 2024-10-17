package main;

import chess.Board;
import game.GameManager;
import game.GameStartException;
import graphics.MainWindow;

public class Main {
	public static void main(String[] args) {
		GameManager g = new GameManager();
		g.setBoard(new Board());
		new MainWindow(g);
		
		/*GameManager g = new GameManager();
		g.setBoard(new Board());
		
		try {
			g.startGame();
		}
		catch (GameStartException e) {
			System.out.println(e.getMessage());
		}*/
	}
}
