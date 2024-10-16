package chessLogicTests;

import static org.junit.jupiter.api.Assertions.*;
import chess.Board;
import chess.IO.FENException;

import org.junit.jupiter.api.Test;

class LegalMovesTest {
	Board b;
	
	//Source: https://www.chessprogramming.org/Perft_Results
	
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
			b = new Board("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");
		}
		catch (FENException e) {
			return;
		}
		assertEquals(1, b.perfTest(0));
		assertEquals(48, b.perfTest(1));
		assertEquals(2039, b.perfTest(2));
		assertEquals(97862, b.perfTest(3));
	}

}
