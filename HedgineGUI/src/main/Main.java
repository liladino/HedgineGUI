package main;

import chess.Board;
import chess.IO.FENException;
import game.GameManager;
import game.GameStartException;
import graphics.MainWindow;

public class Main {
	public static void main(String[] args) {
		GameManager g = new GameManager();
		try{
			g.setBoard(new Board("k7/7P/8/8/8/8/8/K7 w KQkq - 0 1"));
		}
		catch(FENException e) {
			g.setBoard(new Board());
		}
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
