package main;

import chess.*;
import chess.IO.FENException;
import game.*;

public class Main {
	public static void main(String[] args) {
		GameManager g = new GameManager();
		g.setBoard(new Board());
		/*try { g.setBoard(new Board("rnbq1bnr/pppppkpp/8/8/8/8/PPPPPPPP/RNBQKB1R w KQkq - 0 4")); }
		catch(FENException e){ System.out.println(e.getMessage()); return; }*/
		
		try {
			g.startGame();
		}
		catch (GameStartException e) {
			System.out.println(e.getMessage());
		}
	}
}
