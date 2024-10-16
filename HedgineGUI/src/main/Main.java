package main;

import chess.*;
import chess.IO.FENException;
import game.*;

public class Main {
	public static void main(String[] args) {
		Board b;
		try {
			b = new Board("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1");
		}
		catch (FENException e) {
			return;
		}
		b.perfTest(1, true);
		b.printLegalMoves();
		//assertEquals(9, b.perfTest(1));
		
		/*
		GameManager g = new GameManager();
		g.setBoard(new Board());
		/*try { g.setBoard(new Board("rnbq1bnr/pppppkpp/8/8/8/8/PPPPPPPP/RNBQKB1R w KQkq - 0 4")); }
		catch(FENException e){ System.out.println(e.getMessage()); return; }
		
		try {
			g.startGame();
		}
		catch (GameStartException e) {
			System.out.println(e.getMessage());
		}*/
	}
}
