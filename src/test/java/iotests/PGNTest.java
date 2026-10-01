package iotests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hedgineGUI.control.DefaultGameController;
import hedgineGUI.control.GameController;
import hedgineGUI.core.IO.PGNConverter;
import hedgineGUI.core.chess.Board;
import hedgineGUI.core.chess.Move;
import hedgineGUI.core.chess.Square;
import hedgineGUI.game.GameConfiguration;
import hedgineGUI.game.GameManager;
import hedgineGUI.game.HumanPlayer;
import hedgineGUI.game.Player;
import hedgineGUI.game.clock.HeadlessTicker;
import hedgineGUI.utility.Sides;

public class PGNTest {
	Board startBoard;
	Board wayward;
	Board pins;

	@BeforeEach
	void init(){
		startBoard = new Board();
		assertDoesNotThrow(() -> wayward = new Board("r1bqkb1r/pppp1ppp/2n2n2/4p2Q/2B1P3/8/PPPP1PPP/RNB1K1NR w KQkq - 4 4"));
		assertDoesNotThrow(() -> pins = new Board("1r2n2k/P7/8/1n1n3n/Q1p5/8/Q1Q5/R3K2R w KQ - 0 1"));
	}

	@Test
	void standardGameTest(){
		Move e4 = new Move(new Square('e', 2), new Square('e', 4), ' ');	
		assertEquals("e4", PGNConverter.convertMoveToPGNString(startBoard, e4));
		startBoard.makeMove(e4);
		
		Move e6 = new Move(new Square('e', 7), new Square('e', 6), ' ');
		assertEquals("e6", PGNConverter.convertMoveToPGNString(startBoard, e6));
		startBoard.makeMove(e6);

		Move e5 = new Move(new Square('e', 4), new Square('e', 5), ' ');
		assertEquals("e5", PGNConverter.convertMoveToPGNString(startBoard, e5));
		startBoard.makeMove(e5);

		Move d5 = new Move(new Square('d', 7), new Square('d', 5), ' ');
		assertEquals("d5", PGNConverter.convertMoveToPGNString(startBoard, d5));
		startBoard.makeMove(d5);

		Move ed6 = new Move(new Square('e', 5), new Square('d', 6), ' ');
		assertEquals("exd6", PGNConverter.convertMoveToPGNString(startBoard, ed6));		
		startBoard.makeMove(ed6);

		Move bishopd6 = new Move(new Square('f', 8), new Square('d', 6), ' ');
		assertEquals("Bxd6", PGNConverter.convertMoveToPGNString(startBoard, bishopd6));		
		startBoard.makeMove(bishopd6);

		Move bishopb5 = new Move(new Square('f', 1), new Square('b', 5), ' ');
		assertEquals("Bb5+", PGNConverter.convertMoveToPGNString(startBoard, bishopb5));		
		startBoard.makeMove(bishopb5);
		
	}

	@Test
	void waywardAttackTest(){
		Move qxf7 = new Move(new Square('h', 5), new Square('f', 7), ' ');	
		assertEquals("Qxf7#", PGNConverter.convertMoveToPGNString(wayward, qxf7));
		
		Move qxe5 = new Move(new Square('h', 5), new Square('e', 5), ' ');	
		assertEquals("Qxe5+", PGNConverter.convertMoveToPGNString(wayward, qxe5));
		
		Move bxf7 = new Move(new Square('c', 4), new Square('f', 7), ' ');	
		assertEquals("Bxf7+", PGNConverter.convertMoveToPGNString(wayward, bxf7));
		wayward.makeMove(bxf7);

		Move ke7 = new Move(new Square('e', 8), new Square('e', 7), ' ');	
		assertEquals("Ke7", PGNConverter.convertMoveToPGNString(wayward, ke7));
	}

	@Test
	void pinsTest(){
		Move oo = new Move(new Square('e', 1), new Square('g', 1), ' ');	
		assertEquals("O-O", PGNConverter.convertMoveToPGNString(pins, oo));

		Move ooo = new Move(new Square('e', 1), new Square('c', 1), ' ');	
		assertEquals("O-O-O", PGNConverter.convertMoveToPGNString(pins, ooo));
		
		Move q4xc4 = new Move(new Square('a', 4), new Square('c', 4), ' ');	
		assertEquals("Q4xc4", PGNConverter.convertMoveToPGNString(pins, q4xc4));
		
		Move qa2xc4 = new Move(new Square('a', 2), new Square('c', 4), ' ');	
		assertEquals("Qa2xc4", PGNConverter.convertMoveToPGNString(pins, qa2xc4));

		Move qcxc4 = new Move(new Square('c', 2), new Square('c', 4), ' ');	
		assertEquals("Qcxc4", PGNConverter.convertMoveToPGNString(pins, qcxc4));

		Move a8r = new Move(new Square('a', 7), new Square('a', 8), 'r');	
		assertEquals("a8=R", PGNConverter.convertMoveToPGNString(pins, a8r));

		Move b8b = new Move(new Square('a', 7), new Square('b', 8), 'b');	
		assertEquals("axb8=B", PGNConverter.convertMoveToPGNString(pins, b8b));

		pins.makeMove(ooo);
		Move ng7 = new Move(new Square('e', 8), new Square('g', 7), ' ');	
		assertEquals("Ng7", PGNConverter.convertMoveToPGNString(pins, ng7));

	}
	
	@Test
	void fullGameTest1() {
		GameController controller = new DefaultGameController(new GameManager());

		Player w = new HumanPlayer(Sides.WHITE, "White"); 
		Player b = new HumanPlayer(Sides.BLACK, "Black"); 

		assertDoesNotThrow(
			() -> controller.startGame(new GameConfiguration(
				"startpos",
				w, b,
				"N",
				new HeadlessTicker())));

		assertDoesNotThrow(
			() -> {
				Thread.sleep(100);
				w.submitMove(new Move(new Square('f', 2), new Square('f', 4), ' '));
				Thread.sleep(100);
				b.submitMove(new Move(new Square('e', 7), new Square('e', 6), ' '));
				Thread.sleep(100);
				w.submitMove(new Move(new Square('g', 2), new Square('g', 4), ' '));
				Thread.sleep(100);
				b.submitMove(new Move(new Square('d', 8), new Square('h', 4), ' '));
				Thread.sleep(100);
			}
		);

		String s = controller.getPGN();

		assertEquals("[Site \"HedgineGUI\"]\n"
				+ "[White \"White\"]\n"
				+ "[Black \"Black\"]\n"
				+ "[Variation \"Standard\"]\n"
				+ "[Result \"0-1\"]\n"
				+ "1. f4 e6 2. g4 Qh4# 0-1", s);
	}
	
	@Test
	void fullGameTest2() {
		Player w = new HumanPlayer(Sides.WHITE, "White");
		Player b = new HumanPlayer(Sides.BLACK, "Black");
		
		GameController controller = new DefaultGameController(new GameManager());

		assertDoesNotThrow(
			() -> controller.startGame(new GameConfiguration(
				"rnbqkbnr/pppppppp/8/8/5P2/8/PPPPP1PP/RNBQKBNR b KQkq - 0 1",
				w, b,
				"N",
				new HeadlessTicker())));
		
		assertDoesNotThrow(() -> {
			Thread.sleep(100);
			b.submitMove(new Move(new Square('e', 7), new Square('e', 6), ' '));
			Thread.sleep(100);
			w.submitMove(new Move(new Square('g', 2), new Square('g', 4), ' '));
			Thread.sleep(100);
			b.submitMove(new Move(new Square('d', 8), new Square('h', 4), ' '));
			Thread.sleep(100);
		});
		
		String s = controller.getPGN();

		assertEquals("""
[Site "HedgineGUI"]
[White "White"]
[Black "Black"]
[Variation "From Position"]
[FEN "rnbqkbnr/pppppppp/8/8/5P2/8/PPPPP1PP/RNBQKBNR b KQkq - 0 1"]
[Result "0-1"]
1... e6 2. g4 Qh4# 0-1""", s);
	}
	
	@Test
	void fullGameTest3() { //repetition
		Player w = new HumanPlayer(Sides.WHITE, "Alef");
		Player b = new HumanPlayer(Sides.BLACK, "Beta");
		
		GameController controller = new DefaultGameController(new GameManager());

		assertDoesNotThrow(
			() -> controller.startGame(new GameConfiguration(
				"startpos",
				w, b,
				"N",
				new HeadlessTicker())));
		
		assertDoesNotThrow(() -> {
			Thread.sleep(50);
			w.submitMove(new Move(new Square("g1"), new Square("f3"), ' '));
			Thread.sleep(50);
			b.submitMove(new Move(new Square("g8"), new Square("f6"), ' '));
			Thread.sleep(50);
			w.submitMove(new Move(new Square("f3"), new Square("g1"), ' '));
			Thread.sleep(50);
			b.submitMove(new Move(new Square("f6"), new Square("g8"), ' '));
			Thread.sleep(50);
			w.submitMove(new Move(new Square("g1"), new Square("f3"), ' '));
			Thread.sleep(50);
			b.submitMove(new Move(new Square("g8"), new Square("f6"), ' '));
			Thread.sleep(50);
			w.submitMove(new Move(new Square("f3"), new Square("g1"), ' '));
			Thread.sleep(50);
			b.submitMove(new Move(new Square("f6"), new Square("g8"), ' '));
		});
		
		String s = controller.getPGN();

		assertEquals("""
[Site "HedgineGUI"]
[White "Alef"]
[Black "Beta"]
[Variation "Standard"]
[Result "1/2-1/2"]
1. Nf3 Nf6 2. Ng1 Ng8 3. Nf3 Nf6 4. Ng1 Ng8 1/2-1/2""", s);
	}

	@Test
	void fullGameTest4() { //50 move rule
		Player w = new HumanPlayer(Sides.WHITE, "Alef");
		Player b = new HumanPlayer(Sides.BLACK, "Beta");
		
		GameController controller = new DefaultGameController(new GameManager());

		assertDoesNotThrow(
			() -> controller.startGame(new GameConfiguration(
				"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 48 60",
				w, b,
				"N",
				new HeadlessTicker())));
		
		assertDoesNotThrow(() -> {
			Thread.sleep(50);
			w.submitMove(new Move(new Square("g1"), new Square("f3"), ' '));
			Thread.sleep(50);
			b.submitMove(new Move(new Square("g8"), new Square("f6"), ' '));
			Thread.sleep(50);
			w.submitMove(new Move(new Square("f3"), new Square("g1"), ' '));
			Thread.sleep(50);
			b.submitMove(new Move(new Square("f6"), new Square("g8"), ' '));
			Thread.sleep(50);
			w.submitMove(new Move(new Square("g1"), new Square("f3"), ' '));
			Thread.sleep(50);
			b.submitMove(new Move(new Square("g8"), new Square("f6"), ' '));
		});
		
		String s = controller.getPGN();

		assertEquals("""
[Site "HedgineGUI"]
[White "Alef"]
[Black "Beta"]
[Variation "From Position"]
[FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 48 60"]
[Result "1/2-1/2"]
60. Nf3 Nf6 61. Ng1 Ng8 1/2-1/2""", s);
	}
}
