package main;

import chess.*;
import game.*;

public class Main {
	public static void main(String[] args) {
		GameManager g = new GameManager();
		g.setBoard(new Board());
		try {
			g.startGame();
		}
		catch (GameStartException e) {
			System.out.println(e.getMessage());
		}
	}
}
