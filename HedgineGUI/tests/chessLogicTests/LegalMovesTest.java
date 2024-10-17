package chessLogicTests;

import static org.junit.jupiter.api.Assertions.*;
import chess.Board;
import chess.IO.FENException;

import org.junit.jupiter.api.Test;

class LegalMovesTest {
	Board b;
	
	//Source: https://www.chessprogramming.org/Perft_Results
	
	//Last runtime: 21 seconds
	
	@Test
	void startPos() {
		b = new Board();
		assertEquals(1, b.perfTest(0));
		assertEquals(20, b.perfTest(1));
		assertEquals(400, b.perfTest(2));
		assertEquals(8902, b.perfTest(3));
		assertEquals(197281, b.perfTest(4));
	}
	
	@Test
	void pos2() {
		try {
			b = new Board("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
		}
		catch (FENException e) {
			return;
		}

		assertEquals(48, b.perfTest(1));
		assertEquals(2039, b.perfTest(2));
		assertEquals(97862, b.perfTest(3));
		assertEquals(4085603, b.perfTest(4));
	}
	
	@Test
	void pos3() {
		try {
			b = new Board("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1");
		}
		catch (FENException e) {
			return;
		}
		assertEquals(14, b.perfTest(1));
		assertEquals(191, b.perfTest(2));
		assertEquals(2812, b.perfTest(3));
		assertEquals(43238, b.perfTest(4));
	}

	@Test
	void pos4() {
		try {
			b = new Board("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1");
		}
		catch (FENException e) {
			return;
		}
		assertEquals(6, b.perfTest(1));
		assertEquals(264, b.perfTest(2));
		assertEquals(9467, b.perfTest(3));
		assertEquals(422333, b.perfTest(4));
	}
	
	@Test
	void pos4b() {
		try {
			b = new Board("r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ - 0 1 ");
		}
		catch (FENException e) {
			return;
		}
		assertEquals(6, b.perfTest(1));
		assertEquals(264, b.perfTest(2));
		assertEquals(9467, b.perfTest(3));
		assertEquals(422333, b.perfTest(4));
	}
	
	@Test
	void pos5() {
		try {
			b = new Board("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8");
		}
		catch (FENException e) {
			return;
		}
		
		assertEquals(44, b.perfTest(1));
		assertEquals(1486, b.perfTest(2));
		assertEquals(62379, b.perfTest(3));
		assertEquals(2103487, b.perfTest(4));
	}
	
	@Test
	void pos6() {
		try {
			b = new Board("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10");
		}
		catch (FENException e) {
			return;
		}
		
		assertEquals(46, b.perfTest(1));
		assertEquals(2079, b.perfTest(2));
		assertEquals(89890, b.perfTest(3));
	}
	

}
