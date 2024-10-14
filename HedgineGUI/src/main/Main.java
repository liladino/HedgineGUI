package main;

import chess.*;
import chess.IO.FENException;
import game.*;

public class Main {
	public static void main(String[] args) {
		GameManager g = new GameManager();
		g.setBoard(new Board());
		/* { g.setBoard(new Board()); }
		catch(FENException e){ System.out.println(e.getMessage()); return; }*/
		
		try {
			g.startGame();
		}
		catch (GameStartException e) {
			System.out.println(e.getMessage());
		}
	}
}
